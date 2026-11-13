package com.zara.ai.core.voice.provider.gemini

import android.util.Base64
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.security.SecurityManager
import com.zara.ai.core.voice.model.AudioFrame
import com.zara.ai.core.voice.model.VoiceConfiguration
import com.zara.ai.core.voice.model.VoiceEvent
import com.zara.ai.core.voice.provider.RealtimeVoiceProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real-time bidirectional streaming voice provider using the Gemini Live API over WebSocket.
 * Streams 16kHz PCM audio upstream and receives 24kHz low-latency PCM audio downstream.
 */
@Singleton
class GeminiRealtimeVoiceProvider @Inject constructor(
    private val securityManager: SecurityManager,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : RealtimeVoiceProvider {

    private val logger = ZaraLogger("GeminiRealtimeVoiceProvider")
    private val scope = CoroutineScope(ioDispatcher)

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _events = MutableSharedFlow<VoiceEvent>(extraBufferCapacity = 128)
    override val events: SharedFlow<VoiceEvent> = _events.asSharedFlow()

    private var webSocket: WebSocket? = null
    private var activeConfig: VoiceConfiguration = VoiceConfiguration()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS) // Keep-alive for streaming WebSocket
            .writeTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun connect(config: VoiceConfiguration): Result<Unit> = withContext(ioDispatcher) {
        if (_isConnected.value) {
            logger.d("Gemini Live session already active")
            return@withContext Result.success(Unit)
        }

        activeConfig = config
        val apiKey = securityManager.getSecureString("master_api_key")
            ?: System.getenv("GEMINI_API_KEY")

        if (apiKey.isNullOrBlank()) {
            val errorMsg = "No Gemini API key found in SecureStorage or environment"
            logger.w(errorMsg)
            _events.tryEmit(VoiceEvent.Error(errorMsg, isFatal = false))
            return@withContext Result.failure(IllegalStateException(errorMsg))
        }

        try {
            val wsUrl = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=$apiKey"
            val request = Request.Builder()
                .url(wsUrl)
                .build()

            webSocket = client.newWebSocket(request, createWebSocketListener())
            logger.i("Connecting to Gemini Live WebSocket: $wsUrl")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e("Failed to initiate Gemini Live WebSocket connection: ${e.message}")
            _events.tryEmit(VoiceEvent.Error("Connection failure: ${e.message}"))
            Result.failure(e)
        }
    }

    override suspend fun disconnect(): Unit = withContext(ioDispatcher) {
        logger.d("Disconnecting Gemini Live session")
        try {
            webSocket?.close(1000, "Session closed normally by client")
        } catch (e: Exception) {
            logger.e("Error closing WebSocket: ${e.message}")
        } finally {
            webSocket = null
            _isConnected.value = false
        }
    }

    override suspend fun sendAudioFrame(frame: AudioFrame) = withContext(ioDispatcher) {
        val ws = webSocket ?: return@withContext
        if (!_isConnected.value) return@withContext

        try {
            val base64Pcm = Base64.encodeToString(frame.data, Base64.NO_WRAP)
            val realtimeInputJson = JSONObject().apply {
                put("realtimeInput", JSONObject().apply {
                    put("mediaChunks", JSONArray().apply {
                        put(JSONObject().apply {
                            put("mimeType", "audio/pcm;rate=${frame.sampleRate}")
                            put("data", base64Pcm)
                        })
                    })
                })
            }
            ws.send(realtimeInputJson.toString())
        } catch (e: Exception) {
            logger.e("Error sending audio frame: ${e.message}")
        }
    }

    override suspend fun sendText(text: String) = withContext(ioDispatcher) {
        val ws = webSocket ?: return@withContext
        if (!_isConnected.value) return@withContext

        try {
            val textTurnJson = JSONObject().apply {
                put("clientContent", JSONObject().apply {
                    put("turns", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", text)
                                })
                            })
                        })
                    })
                    put("turnComplete", true)
                })
            }
            ws.send(textTurnJson.toString())
            logger.d("Sent text turn to Gemini Live: $text")
        } catch (e: Exception) {
            logger.e("Error sending text to Gemini Live: ${e.message}")
        }
    }

    override fun interrupt() {
        logger.d("Sending interruption signal to Gemini Live")
        try {
            val interruptJson = JSONObject().apply {
                put("clientContent", JSONObject().apply {
                    put("interrupted", true)
                })
            }
            webSocket?.send(interruptJson.toString())
        } catch (e: Exception) {
            logger.w("Failed to send interruption message: ${e.message}")
        }
    }

    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                logger.i("Gemini Live WebSocket connection established")
                _isConnected.value = true

                // Send initial setup payload
                val systemPrompt = "You are ZARA, an advanced personal AI assistant. " +
                        "Speak naturally, clearly and concisely in Hindi, Hinglish, or English. " +
                        "Adapt immediately to whatever language or mix of languages the user uses. " +
                        "Keep your spoken replies direct and conversational (1-3 sentences max unless explained)."

                val setupPayload = JSONObject().apply {
                    put("setup", JSONObject().apply {
                        put("model", "models/gemini-2.0-flash-exp")
                        put("generationConfig", JSONObject().apply {
                            put("responseModalities", JSONArray().apply {
                                put("AUDIO")
                            })
                            put("speechConfig", JSONObject().apply {
                                put("voiceConfig", JSONObject().apply {
                                    put("prebuiltVoiceConfig", JSONObject().apply {
                                        put("voiceName", activeConfig.voiceName)
                                    })
                                })
                            })
                        })
                        put("systemInstruction", JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", systemPrompt)
                                })
                            })
                        })
                    })
                }

                ws.send(setupPayload.toString())
                logger.d("Sent Gemini Live setup payload with voice ${activeConfig.voiceName}")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                scope.launch {
                    parseServerMessage(text)
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                logger.w("Gemini Live WebSocket failure: ${t.message} (code=${response?.code})")
                _isConnected.value = false
                _events.tryEmit(VoiceEvent.Error("Gemini Live connection issue: ${t.message ?: "Network error"}"))
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                logger.i("Gemini Live WebSocket closed (code=$code, reason=$reason)")
                _isConnected.value = false
            }
        }
    }

    private fun parseServerMessage(jsonString: String) {
        try {
            val json = JSONObject(jsonString)

            if (json.has("serverContent")) {
                val serverContent = json.getJSONObject("serverContent")

                if (serverContent.optBoolean("interrupted", false)) {
                    _events.tryEmit(VoiceEvent.Interrupted)
                    return
                }

                if (serverContent.has("modelTurn")) {
                    val modelTurn = serverContent.getJSONObject("modelTurn")
                    val parts = modelTurn.optJSONArray("parts")

                    if (parts != null) {
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)

                            // Assistant transcript chunk
                            if (part.has("text")) {
                                val transcriptText = part.getString("text")
                                if (transcriptText.isNotEmpty()) {
                                    _events.tryEmit(VoiceEvent.AssistantTranscriptChunk(transcriptText))
                                }
                            }

                            // Assistant audio chunk (24kHz PCM)
                            if (part.has("inlineData")) {
                                val inlineData = part.getJSONObject("inlineData")
                                val base64Audio = inlineData.optString("data")
                                if (base64Audio.isNotEmpty()) {
                                    val pcmBytes = Base64.decode(base64Audio, Base64.DEFAULT)
                                    _events.tryEmit(VoiceEvent.AssistantAudioChunk(pcmBytes))
                                }
                            }
                        }
                    }
                }

                if (serverContent.optBoolean("turnComplete", false)) {
                    _events.tryEmit(VoiceEvent.AssistantSpeechEnded(System.currentTimeMillis()))
                }
            }
        } catch (e: Exception) {
            logger.e("Error parsing Gemini Live server message: ${e.message}")
        }
    }
}
