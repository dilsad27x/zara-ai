package com.zara.ai.core.voice.provider.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.voice.model.VoiceLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-quality on-device Text-To-Speech engine utilizing Android's native TTS subsystem.
 * Supports streaming sentence-by-sentence synthesis for low time-to-first-audio latency,
 * multilingual voices (Hindi, Hinglish, English), and instant cancellation for conversational barge-in.
 */
@Singleton
class AndroidTtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private val logger = ZaraLogger("AndroidTtsManager")
    private var tts: TextToSpeech? = null

    private val isInitialized = AtomicBoolean(false)
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val sentenceQueue = ConcurrentLinkedQueue<String>()
    private var utteranceCounter = 0L

    var onSpeechStarted: (() -> Unit)? = null
    var onSpeechCompleted: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            logger.e("Failed to instantiate TextToSpeech: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized.set(true)
            logger.i("TextToSpeech initialized successfully")

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    onSpeechStarted?.invoke()
                }

                override fun onDone(utteranceId: String?) {
                    processNextInQueue()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onError?.invoke("TTS playback error for utterance $utteranceId")
                    processNextInQueue()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    logger.w("TTS error code $errorCode for utterance $utteranceId")
                    onError?.invoke("TTS error code $errorCode")
                    processNextInQueue()
                }
            })

            // Default to Hinglish / Indian English locale
            setLanguage(VoiceLanguage.HINGLISH)
        } else {
            logger.e("TextToSpeech initialization failed with status: $status")
        }
    }

    /**
     * Configure the active voice language for TTS.
     */
    fun setLanguage(language: VoiceLanguage): Boolean {
        val targetLocale = when (language) {
            VoiceLanguage.HINDI -> Locale("hi", "IN")
            VoiceLanguage.HINGLISH -> Locale("en", "IN")
            VoiceLanguage.ENGLISH -> Locale.US
        }

        return try {
            val result = tts?.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                logger.w("Locale $targetLocale not fully supported, falling back to US English")
                tts?.setLanguage(Locale.US)
                false
            } else {
                logger.d("TTS language set to ${targetLocale.displayName}")
                true
            }
        } catch (e: Exception) {
            logger.e("Exception setting TTS language: ${e.message}")
            false
        }
    }

    /**
     * Configure pitch (0.5 - 2.0) and speech rate (0.5 - 2.0).
     */
    fun configureVoice(pitch: Float = 1.0f, speechRate: Float = 1.05f) {
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speechRate)
    }

    /**
     * Synthesize and speak a full text passage immediately.
     */
    @Synchronized
    fun speak(text: String, flushCurrent: Boolean = true) {
        if (!isInitialized.get() || text.isBlank()) return

        if (flushCurrent) {
            stop()
        }

        val sentences = splitIntoSentences(text)
        if (sentences.isEmpty()) return

        sentenceQueue.addAll(sentences)
        if (!_isSpeaking.value) {
            processNextInQueue()
        }
    }

    /**
     * Stream a chunk of text from an ongoing AI response stream.
     * Extracts completed sentences and starts speaking immediately.
     */
    private val streamingBuffer = StringBuilder()

    @Synchronized
    fun appendStreamChunk(chunk: String) {
        if (!isInitialized.get() || chunk.isEmpty()) return

        streamingBuffer.append(chunk)
        val text = streamingBuffer.toString()

        // Check for sentence boundaries: '.', '?', '!', '।', or newline
        val regex = Regex("([.?!।\\n])\\s*")
        val match = regex.find(text)

        if (match != null) {
            val sentenceEndIndex = match.range.last + 1
            val completeSentence = text.substring(0, sentenceEndIndex).trim()
            streamingBuffer.delete(0, sentenceEndIndex)

            if (completeSentence.isNotEmpty()) {
                sentenceQueue.offer(completeSentence)
                if (!_isSpeaking.value) {
                    processNextInQueue()
                }
            }
        }
    }

    /**
     * Flush any remaining text in the streaming buffer.
     */
    @Synchronized
    fun finishStream() {
        val remaining = streamingBuffer.toString().trim()
        streamingBuffer.clear()
        if (remaining.isNotEmpty()) {
            sentenceQueue.offer(remaining)
            if (!_isSpeaking.value) {
                processNextInQueue()
            }
        }
    }

    /**
     * Stop speech synthesis immediately and clear queued sentences.
     * Essential for barge-in / user interruption.
     */
    @Synchronized
    fun stop() {
        sentenceQueue.clear()
        streamingBuffer.clear()
        _isSpeaking.value = false

        try {
            tts?.stop()
            logger.d("TTS stopped and queued utterances cleared")
        } catch (e: Exception) {
            logger.e("Error stopping TTS: ${e.message}")
        }
    }

    private fun processNextInQueue() {
        val nextSentence = sentenceQueue.poll()
        if (nextSentence != null) {
            _isSpeaking.value = true
            val utteranceId = "zara_utt_${++utteranceCounter}"
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            tts?.speak(nextSentence, TextToSpeech.QUEUE_ADD, params, utteranceId)
        } else {
            _isSpeaking.value = false
            onSpeechCompleted?.invoke()
        }
    }

    private fun splitIntoSentences(text: String): List<String> {
        val delimiters = Regex("(?<=[.?!।\\n])\\s+")
        return text.split(delimiters).map { it.trim() }.filter { it.isNotEmpty() }
    }

    /**
     * Release TTS resources on shutdown.
     */
    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            logger.e("Error shutting down TextToSpeech: ${e.message}")
        } finally {
            tts = null
            isInitialized.set(false)
        }
    }
}
