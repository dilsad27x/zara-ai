package com.zara.ai.core.common.logger

import android.util.Log
import com.zara.ai.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

enum class LogLevel {
    VERBOSE, DEBUG, INFO, WARN, ERROR, AGENT
}

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
            return sdf.format(Date(timestamp))
        }
}

/**
 * High-performance, memory-buffered structured logger for ZARA AI.
 */
@Singleton
class ZaraLogger(private val defaultTag: String = "ZaraAI") {

    @Inject
    constructor() : this("ZaraAI")

    private val maxBufferCapacity = 200
    private val bufferQueue = ConcurrentLinkedQueue<LogEntry>()
    private val _logsState = MutableStateFlow<List<LogEntry>>(emptyList())
    val logsState: StateFlow<List<LogEntry>> = _logsState.asStateFlow()

    private val isLoggingActive: Boolean = BuildConfig.DEBUG || BuildConfig.LOGGING_ENABLED

    fun v(message: String) = v(defaultTag, message)
    fun d(message: String) = d(defaultTag, message)
    fun i(message: String) = i(defaultTag, message)
    fun w(message: String, throwable: Throwable? = null) = w(defaultTag, message, throwable)
    fun e(message: String, throwable: Throwable? = null) = e(defaultTag, message, throwable)
    fun agent(message: String) = agent(defaultTag, message)

    fun v(tag: String, message: String) {
        if (!isLoggingActive) return
        safeLog { Log.v(tag, message) }
        appendEntry(LogLevel.VERBOSE, tag, message)
    }

    fun d(tag: String, message: String) {
        if (!isLoggingActive) return
        safeLog { Log.d(tag, message) }
        appendEntry(LogLevel.DEBUG, tag, message)
    }

    fun i(tag: String, message: String) {
        safeLog { Log.i(tag, message) }
        appendEntry(LogLevel.INFO, tag, message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        safeLog { Log.w(tag, message, throwable) }
        appendEntry(LogLevel.WARN, tag, message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        safeLog { Log.e(tag, message, throwable) }
        appendEntry(LogLevel.ERROR, tag, message, throwable)
    }

    /**
     * Special log level specifically for AI agent chain-of-thought and actions.
     */
    fun agent(tag: String, message: String) {
        safeLog { Log.i("ZARA_AGENT/$tag", message) }
        appendEntry(LogLevel.AGENT, tag, message)
    }

    private inline fun safeLog(action: () -> Unit) {
        try {
            action()
        } catch (_: Throwable) {
            // JVM unit test environment where android.util.Log is not mocked
        }
    }

    private fun appendEntry(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        val entry = LogEntry(level = level, tag = tag, message = message, throwable = throwable)
        bufferQueue.offer(entry)
        while (bufferQueue.size > maxBufferCapacity) {
            bufferQueue.poll()
        }
        _logsState.value = bufferQueue.toList()
    }

    fun clearLogs() {
        bufferQueue.clear()
        _logsState.value = emptyList()
    }
}
