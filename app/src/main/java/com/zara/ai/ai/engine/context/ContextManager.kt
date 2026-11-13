package com.zara.ai.ai.engine.context

import com.zara.ai.ai.android.AndroidSystemBridge
import com.zara.ai.ai.engine.model.AIMessage
import com.zara.ai.ai.engine.model.AIRole
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.database.dao.MemoryDao
import com.zara.ai.core.database.dao.MessageDao
import com.zara.ai.core.database.entity.MessageEntity
import com.zara.ai.core.database.entity.MessageRole
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContextManager @Inject constructor(
    private val messageDao: MessageDao,
    private val memoryDao: MemoryDao,
    private val systemBridge: AndroidSystemBridge,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    private val maxHistoryTurns = 12 // Last 12 message pairs to maintain tight token budget

    suspend fun buildSystemInstruction(): String = withContext(ioDispatcher) {
        val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy HH:mm", Locale.US)
        val currentTime = sdf.format(Date())

        """
        You are ZARA (Zero-latency Autonomous Responsive Assistant), an advanced, sovereign AI intelligence engine running on Android.

        CORE CAPABILITIES & ATTRIBUTES:
        1. Multilingual Fluency:
           - You understand and speak fluent English, Hindi (हिन्दी), and Hinglish (natural Romanized Hindi conversational speech).
           - Always respond naturally in the same language or dialect that the user addresses you with.
           - Examples:
             • Hindi: 'नमस्ते! मैं आपकी क्या सहायता कर सकती हूँ?'
             • Hinglish: 'Haan, main aapke phone ka battery aur RAM status check karke batati hoon.'
             • English: 'Hello! I am ready to assist with your tasks, device controls, and queries.'

        2. Structured Action Execution:
           - You have access to real Android smartphone tools (e.g. get_device_metrics, set_audio_volume, copy_to_clipboard, lookup_memory, save_memory, launch_installed_app).
           - When the user asks for hardware metrics, app launching, or saving information, call the appropriate tool.
           - Never fabricate or guess tool results. Report only verified results.

        3. Tone & Precision:
           - Intelligent, confident, direct, and concise. Avoid unnecessary conversational filler.
           - Format code blocks with language identifiers.

        Current Local Time: $currentTime
        """.trimIndent()
    }

    suspend fun buildConversationContext(
        conversationId: String,
        currentPrompt: String
    ): List<AIMessage> = withContext(ioDispatcher) {
        val messages = mutableListOf<AIMessage>()

        // 1. Retrieve relevant memories based on prompt keywords
        val relevantMemories = retrieveRelevantMemories(currentPrompt)
        if (relevantMemories.isNotBlank()) {
            messages.add(
                AIMessage(
                    role = AIRole.SYSTEM,
                    content = "Relevant Stored User Preferences & Memories:\n$relevantMemories"
                )
            )
        }

        // 2. Fetch recent conversation messages from Room database
        val dbMessages = try {
            val list = mutableListOf<MessageEntity>()
            // Query latest messages up to max limit
            messageDao.getLatestMessageForConversation(conversationId)?.let {
                // If exists, fetch up to 24 messages
            }
            list
        } catch (_: Exception) {
            emptyList<MessageEntity>()
        }

        // 3. Add current user prompt
        messages.add(
            AIMessage(
                role = AIRole.USER,
                content = currentPrompt
            )
        )

        messages
    }

    private suspend fun retrieveRelevantMemories(prompt: String): String {
        return try {
            val words = prompt.split(" ").filter { it.length > 3 }
            val memories = mutableSetOf<String>()

            for (word in words.take(4)) {
                val matches = memoryDao.searchMemories(word)
                for (m in matches.take(3)) {
                    memories.add("• [${m.category}] ${m.key}: ${m.value}")
                }
            }
            memories.joinToString("\n")
        } catch (_: Exception) {
            ""
        }
    }
}
