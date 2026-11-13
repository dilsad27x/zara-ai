package com.zara.ai.ai.engine.tools.builtin

import android.content.Context
import android.content.Intent
import com.zara.ai.ai.android.AndroidSystemBridge
import com.zara.ai.ai.engine.tools.ToolExecutionResult
import com.zara.ai.ai.engine.tools.ToolParameter
import com.zara.ai.ai.engine.tools.ToolParameterType
import com.zara.ai.ai.engine.tools.ToolRegistry
import com.zara.ai.ai.engine.tools.ZaraTool
import com.zara.ai.core.database.dao.MemoryDao
import com.zara.ai.core.database.entity.MemoryCategory
import com.zara.ai.core.database.entity.MemoryEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZaraBuiltinTools @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemBridge: AndroidSystemBridge,
    private val memoryDao: MemoryDao,
    private val toolRegistry: ToolRegistry
) {
    fun registerAll() {
        toolRegistry.register(createDeviceMetricsTool())
        toolRegistry.register(createVolumeControlTool())
        toolRegistry.register(createGetVolumeTool())
        toolRegistry.register(createClipboardTool())
        toolRegistry.register(createToastTool())
        toolRegistry.register(createTimeTool())
        toolRegistry.register(createMemoryLookupTool())
        toolRegistry.register(createMemorySaveTool())
        toolRegistry.register(createAppLauncherTool())
    }

    private fun createDeviceMetricsTool() = ZaraTool(
        name = "get_device_metrics",
        description = "Retrieves live hardware and system telemetry from the Android device, including battery percentage, charging status, free RAM, OS version, and model.",
        category = "System",
        parameters = emptyList(),
        execute = {
            val metrics = systemBridge.getDeviceMetrics()
            val text = """
                Device: ${metrics.deviceModel}
                OS: ${metrics.osVersion}
                Battery: ${metrics.batteryLevel}% (${if (metrics.isCharging) "Charging" else "Discharging"})
                RAM Available: ${metrics.availableRamMb} MB / ${metrics.totalRamMb} MB
                WiFi Connected: ${metrics.isWifiConnected}
            """.trimIndent()
            ToolExecutionResult(success = true, output = text)
        }
    )

    private fun createVolumeControlTool() = ZaraTool(
        name = "set_audio_volume",
        description = "Sets the media audio volume level of the Android smartphone.",
        category = "Hardware",
        parameters = listOf(
            ToolParameter(
                name = "level",
                type = ToolParameterType.INTEGER,
                description = "Volume index from 0 (mute) to 15 (max)",
                isRequired = true
            )
        ),
        execute = { args ->
            val level = (args["level"] as? Number)?.toInt() ?: 5
            systemBridge.setVolumeLevel(level)
            ToolExecutionResult(success = true, output = "Volume set to $level")
        }
    )

    private fun createGetVolumeTool() = ZaraTool(
        name = "get_audio_volume",
        description = "Retrieves the current media volume level of the device.",
        category = "Hardware",
        parameters = emptyList(),
        execute = {
            val current = systemBridge.getVolumeLevel()
            ToolExecutionResult(success = true, output = "Current media volume level is $current")
        }
    )

    private fun createClipboardTool() = ZaraTool(
        name = "copy_to_clipboard",
        description = "Copies the provided text or code snippet to the Android system clipboard.",
        category = "Productivity",
        parameters = listOf(
            ToolParameter(
                name = "text",
                type = ToolParameterType.STRING,
                description = "Text or code to copy to clipboard",
                isRequired = true
            ),
            ToolParameter(
                name = "label",
                type = ToolParameterType.STRING,
                description = "Optional label for the clipboard item",
                isRequired = false
            )
        ),
        execute = { args ->
            val text = args["text"]?.toString() ?: ""
            val label = args["label"]?.toString() ?: "ZARA Output"
            systemBridge.copyToClipboard(label, text)
            ToolExecutionResult(success = true, output = "Copied to clipboard successfully (${text.length} characters).")
        }
    )

    private fun createToastTool() = ZaraTool(
        name = "show_notification_toast",
        description = "Displays a brief on-screen toast popup to notify the user on their phone.",
        category = "System",
        parameters = listOf(
            ToolParameter(
                name = "message",
                type = ToolParameterType.STRING,
                description = "Message text to display in toast",
                isRequired = true
            )
        ),
        execute = { args ->
            val msg = args["message"]?.toString() ?: "Notification from ZARA AI"
            systemBridge.showToast(msg)
            ToolExecutionResult(success = true, output = "Toast displayed: '$msg'")
        }
    )

    private fun createTimeTool() = ZaraTool(
        name = "get_current_time",
        description = "Returns the current local date, time, and timezone of the user's Android phone.",
        category = "Utility",
        parameters = emptyList(),
        execute = {
            val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy HH:mm:ss z", Locale.US)
            sdf.timeZone = TimeZone.getDefault()
            val formatted = sdf.format(Date())
            ToolExecutionResult(success = true, output = "Current time is $formatted")
        }
    )

    private fun createMemoryLookupTool() = ZaraTool(
        name = "lookup_memory",
        description = "Searches long-term and short-term memory records for saved facts, user preferences, or rules.",
        category = "Memory",
        parameters = listOf(
            ToolParameter(
                name = "query",
                type = ToolParameterType.STRING,
                description = "Search keyword or key to look up",
                isRequired = true
            )
        ),
        execute = { args ->
            val query = args["query"]?.toString() ?: ""
            val memories = memoryDao.searchMemories(query)
            if (memories.isEmpty()) {
                ToolExecutionResult(success = true, output = "No memories found matching '$query'.")
            } else {
                val formatted = memories.joinToString("\n") { "• [${it.category}] ${it.key}: ${it.value}" }
                ToolExecutionResult(success = true, output = "Retrieved memories:\n$formatted")
            }
        }
    )

    private fun createMemorySaveTool() = ZaraTool(
        name = "save_memory",
        description = "Persists a piece of information or preference to ZARA's memory database.",
        category = "Memory",
        parameters = listOf(
            ToolParameter(
                name = "key",
                type = ToolParameterType.STRING,
                description = "Unique memory key/topic (e.g. user_favorite_language)",
                isRequired = true
            ),
            ToolParameter(
                name = "value",
                type = ToolParameterType.STRING,
                description = "Detailed memory content or preference",
                isRequired = true
            ),
            ToolParameter(
                name = "category",
                type = ToolParameterType.STRING,
                description = "Memory category: FACT, PREFERENCE, IDENTITY, RULE",
                isRequired = false,
                enumValues = listOf("FACT", "PREFERENCE", "IDENTITY", "RULE")
            )
        ),
        execute = { args ->
            val key = args["key"]?.toString()?.trim() ?: ""
            val value = args["value"]?.toString()?.trim() ?: ""
            val catStr = args["category"]?.toString()?.uppercase() ?: "FACT"
            val category = try {
                MemoryCategory.valueOf(catStr)
            } catch (_: Exception) {
                MemoryCategory.FACT
            }

            if (key.isBlank() || value.isBlank()) {
                return@ZaraTool ToolExecutionResult(success = false, output = "", errorMessage = "Key and value cannot be blank.")
            }

            val entity = MemoryEntity(
                key = key,
                value = value,
                category = category,
                source = "user_chat"
            )
            memoryDao.insertMemory(entity)
            ToolExecutionResult(success = true, output = "Saved to memory: [$category] $key = $value")
        }
    )

    private fun createAppLauncherTool() = ZaraTool(
        name = "launch_installed_app",
        description = "Launches an installed Android app on the phone using package manager intent.",
        category = "System",
        parameters = listOf(
            ToolParameter(
                name = "package_name",
                type = ToolParameterType.STRING,
                description = "Package name of the application (e.g. com.android.chrome)",
                isRequired = true
            )
        ),
        execute = { args ->
            val pkg = args["package_name"]?.toString()?.trim() ?: ""
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                ToolExecutionResult(success = true, output = "Launched app: $pkg")
            } else {
                ToolExecutionResult(success = false, output = "", errorMessage = "App package '$pkg' is not installed or cannot be launched.")
            }
        }
    )
}
