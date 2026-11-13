package com.zara.ai.ai.engine.provider.gemini

import com.zara.ai.ai.engine.model.AIMessage
import com.zara.ai.ai.engine.model.AIRequest
import com.zara.ai.ai.engine.model.AIResponse
import com.zara.ai.ai.engine.model.AIRole
import com.zara.ai.ai.engine.model.AIStreamChunk
import com.zara.ai.ai.engine.model.AIToolCall
import com.zara.ai.ai.engine.model.ProviderHealth
import com.zara.ai.ai.engine.model.TokenUsage
import com.zara.ai.ai.engine.provider.AIProvider
import com.zara.ai.ai.engine.tools.ToolRegistry
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.security.SecurityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiProvider @Inject constructor(
    private val securityManager: SecurityManager,
    private val toolRegistry: ToolRegistry,
    private val logger: ZaraLogger,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : AIProvider {

    override val name: String = "Google Gemini"

    private val defaultModel = "gemini-1.5-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getApiKey(): String? {
        return securityManager.getSecureString("master_api_key")?.takeIf { it.isNotBlank() }
    }

    override suspend fun checkHealth(): ProviderHealth = withContext(ioDispatcher) {
        val apiKey = getApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext ProviderHealth(
                providerName = name,
                isAvailable = false,
                message = "API key not configured in Settings"
            )
        }

        val startTime = System.currentTimeMillis()
        try {
            val endpoint = "$baseUrl?key=$apiKey"
            val request = Request.Builder()
                .url(endpoint)
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val elapsed = System.currentTimeMillis() - startTime
                if (response.isSuccessful) {
                    ProviderHealth(
                        providerName = name,
                        isAvailable = true,
                        latencyMs = elapsed,
                        message = "Connected ($elapsed ms)"
                    )
                } else {
                    ProviderHealth(
                        providerName = name,
                        isAvailable = false,
                        latencyMs = elapsed,
                        message = "HTTP ${response.code}: ${response.message}"
                    )
                }
            }
        } catch (e: Exception) {
            ProviderHealth(
                providerName = name,
                isAvailable = false,
                message = "Connection failed: ${e.localizedMessage}"
            )
        }
    }

    override suspend fun chat(request: AIRequest): Result<AIResponse> = withContext(ioDispatcher) {
        val apiKey = getApiKey()
            ?: return@withContext Result.failure(IllegalStateException("Gemini API key is not configured. Please add your API key in Settings."))

        val model = request.model ?: defaultModel
        val endpoint = "$baseUrl/$model:generateContent?key=$apiKey"
        val payload = buildRequestBody(request)

        logger.d("GeminiProvider", "Sending chat request to model: $model [tools=${toolRegistry.getAllTools().size}]")

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val httpRequest = Request.Builder()
            .url(endpoint)
            .post(payload.toString().toRequestBody(mediaType))
            .build()

        try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = parseErrorMessage(response.code, responseBody)
                    logger.e("GeminiProvider", "API call failed with status ${response.code}: $errorMsg")
                    return@withContext Result.failure(RuntimeException(errorMsg))
                }

                val parsed = parseGenerateContentResponse(responseBody, model)
                Result.success(parsed)
            }
        } catch (e: Exception) {
            logger.e("GeminiProvider", "Network exception during chat", e)
            Result.failure(e)
        }
    }

    override fun streamChat(request: AIRequest): Flow<AIStreamChunk> = callbackFlow {
        val apiKey = getApiKey()
        if (apiKey.isNullOrBlank()) {
            trySend(
                AIStreamChunk(
                    textChunk = "⚠️ Gemini API key is missing. Please open Settings → General & API Key to configure your Google Gemini API key.",
                    isFinished = true
                )
            )
            close()
            return@callbackFlow
        }

        val model = request.model ?: defaultModel
        val endpoint = "$baseUrl/$model:streamGenerateContent?alt=sse&key=$apiKey"
        val payload = buildRequestBody(request)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val httpRequest = Request.Builder()
            .url(endpoint)
            .post(payload.toString().toRequestBody(mediaType))
            .build()

        val call = httpClient.newCall(httpRequest)

        try {
            call.execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    val errorMsg = parseErrorMessage(response.code, errBody)
                    trySend(
                        AIStreamChunk(
                            textChunk = "⚠️ $errorMsg",
                            isFinished = true
                        )
                    )
                    close()
                    return@use
                }

                val source = response.body?.source()
                if (source == null) {
                    close()
                    return@use
                }

                val reader = BufferedReader(InputStreamReader(source.inputStream()))
                var line: String?
                var totalUsage: TokenUsage? = null

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.startsWith("data: ")) {
                        val jsonStr = currentLine.removePrefix("data: ").trim()
                        if (jsonStr.isEmpty() || jsonStr == "[DONE]") continue

                        try {
                            val jsonObj = JSONObject(jsonStr)
                            val candidates = jsonObj.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val firstCandidate = candidates.getJSONObject(0)
                                val content = firstCandidate.optJSONObject("content")
                                val parts = content?.optJSONArray("parts")

                                if (parts != null) {
                                    for (i in 0 until parts.length()) {
                                        val part = parts.getJSONObject(i)
                                        if (part.has("text")) {
                                            val text = part.getString("text")
                                            trySend(AIStreamChunk(textChunk = text))
                                        } else if (part.has("functionCall")) {
                                            val fc = part.getJSONObject("functionCall")
                                            val name = fc.getString("name")
                                            val argsJson = fc.optJSONObject("args")
                                            val argsMap = mutableMapOf<String, Any?>()
                                            if (argsJson != null) {
                                                val keys = argsJson.keys()
                                                while (keys.hasNext()) {
                                                    val key = keys.next()
                                                    argsMap[key] = argsJson.opt(key)
                                                }
                                            }
                                            val toolCall = AIToolCall(
                                                id = UUID.randomUUID().toString(),
                                                name = name,
                                                arguments = argsMap
                                            )
                                            trySend(AIStreamChunk(toolCallChunk = toolCall))
                                        }
                                    }
                                }
                            }

                            val usage = jsonObj.optJSONObject("usageMetadata")
                            if (usage != null) {
                                totalUsage = TokenUsage(
                                    promptTokens = usage.optInt("promptTokenCount", 0),
                                    completionTokens = usage.optInt("candidatesTokenCount", 0),
                                    totalTokens = usage.optInt("totalTokenCount", 0)
                                )
                            }
                        } catch (e: Exception) {
                            logger.w("GeminiProvider", "Error parsing SSE chunk: ${e.message}")
                        }
                    }
                }

                trySend(AIStreamChunk(isFinished = true, tokenUsage = totalUsage))
                close()
            }
        } catch (e: Exception) {
            logger.e("GeminiProvider", "SSE streaming connection exception", e)
            trySend(
                AIStreamChunk(
                    textChunk = "\n\n*[Connection interrupted: ${e.localizedMessage ?: "Unknown network error"} - please retry]*",
                    isFinished = true
                )
            )
            close()
        }

        awaitClose {
            call.cancel()
        }
    }.flowOn(ioDispatcher)

    private fun buildRequestBody(request: AIRequest): JSONObject {
        val root = JSONObject()

        // System Instruction
        val systemText = request.systemInstruction
        if (!systemText.isNullOrBlank()) {
            val systemObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemText))
            systemObj.put("parts", sysParts)
            root.put("systemInstruction", systemObj)
        }

        // Messages / Contents
        val contentsArray = JSONArray()
        for (msg in request.messages) {
            val msgObj = JSONObject()
            val partsArray = JSONArray()

            when (msg.role) {
                AIRole.USER -> {
                    msgObj.put("role", "user")
                    partsArray.put(JSONObject().put("text", msg.content))
                }
                AIRole.ASSISTANT -> {
                    msgObj.put("role", "model")
                    if (msg.content.isNotBlank()) {
                        partsArray.put(JSONObject().put("text", msg.content))
                    }
                    for (toolCall in msg.toolCalls) {
                        val fcObj = JSONObject()
                        fcObj.put("name", toolCall.name)
                        fcObj.put("args", JSONObject(toolCall.arguments))
                        partsArray.put(JSONObject().put("functionCall", fcObj))
                    }
                }
                AIRole.SYSTEM -> {
                    // Gemini handles system in systemInstruction, but if in messages, append as user context
                    msgObj.put("role", "user")
                    partsArray.put(JSONObject().put("text", "[System Context: ${msg.content}]"))
                }
                AIRole.TOOL -> {
                    msgObj.put("role", "function")
                    for (toolRes in msg.toolResults) {
                        val frObj = JSONObject()
                        frObj.put("name", toolRes.toolName)
                        val respObj = JSONObject()
                        respObj.put("output", toolRes.result)
                        frObj.put("response", respObj)
                        partsArray.put(JSONObject().put("functionResponse", frObj))
                    }
                }
            }

            if (partsArray.length() > 0) {
                msgObj.put("parts", partsArray)
                contentsArray.put(msgObj)
            }
        }
        root.put("contents", contentsArray)

        // Tools (Function Declarations)
        val toolsJson = toolRegistry.buildGeminiToolsJson()
        if (toolsJson.length() > 0) {
            val toolsArray = JSONArray()
            val toolsContainer = JSONObject()
            toolsContainer.put("functionDeclarations", toolsJson)
            toolsArray.put(toolsContainer)
            root.put("tools", toolsArray)
        }

        // Generation Config
        val genConfig = JSONObject()
        genConfig.put("temperature", request.temperature)
        genConfig.put("topP", request.topP)
        genConfig.put("maxOutputTokens", request.maxOutputTokens)
        root.put("generationConfig", genConfig)

        return root
    }

    private fun parseGenerateContentResponse(jsonString: String, model: String): AIResponse {
        val root = JSONObject(jsonString)
        val candidates = root.optJSONArray("candidates")

        if (candidates == null || candidates.length() == 0) {
            val promptFeedback = root.optJSONObject("promptFeedback")
            val blockReason = promptFeedback?.optString("blockReason")
            return AIResponse(
                text = if (blockReason != null) "Response blocked by safety policy: $blockReason" else "No response generated.",
                finishReason = blockReason ?: "EMPTY",
                modelName = model,
                isSuccess = false,
                errorMessage = blockReason
            )
        }

        val candidate = candidates.getJSONObject(0)
        val finishReason = candidate.optString("finishReason", "STOP")
        val content = candidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")

        val textBuilder = StringBuilder()
        val toolCalls = mutableListOf<AIToolCall>()

        if (parts != null) {
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("text")) {
                    textBuilder.append(part.getString("text"))
                }
                if (part.has("functionCall")) {
                    val fc = part.getJSONObject("functionCall")
                    val name = fc.getString("name")
                    val argsJson = fc.optJSONObject("args")
                    val argsMap = mutableMapOf<String, Any?>()
                    if (argsJson != null) {
                        val keys = argsJson.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            argsMap[key] = argsJson.opt(key)
                        }
                    }
                    toolCalls.add(
                        AIToolCall(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            arguments = argsMap
                        )
                    )
                }
            }
        }

        val usage = root.optJSONObject("usageMetadata")
        val tokenUsage = TokenUsage(
            promptTokens = usage?.optInt("promptTokenCount", 0) ?: 0,
            completionTokens = usage?.optInt("candidatesTokenCount", 0) ?: 0,
            totalTokens = usage?.optInt("totalTokenCount", 0) ?: 0
        )

        return AIResponse(
            text = textBuilder.toString(),
            toolCalls = toolCalls,
            finishReason = finishReason,
            tokenUsage = tokenUsage,
            modelName = model,
            isSuccess = true
        )
    }

    private fun parseErrorMessage(code: Int, responseBody: String): String {
        return try {
            val json = JSONObject(responseBody)
            val error = json.optJSONObject("error")
            val message = error?.optString("message") ?: "HTTP error $code"
            when (code) {
                400 -> "Invalid Request: $message"
                401, 403 -> "Authentication Error: Please verify your Gemini API key in Settings ($message)"
                429 -> "Gemini API Rate limit exceeded. Please wait a moment and try again."
                500, 503 -> "Google Gemini server unavailable ($code). Please try again later."
                else -> "Error ($code): $message"
            }
        } catch (_: Exception) {
            when (code) {
                401, 403 -> "Authentication Failed: Check your Gemini API key in Settings."
                429 -> "Rate limit reached. Please wait a few seconds before sending another message."
                503 -> "Gemini service temporarily overloaded. Please retry."
                else -> "Network request failed with HTTP $code."
            }
        }
    }
}
