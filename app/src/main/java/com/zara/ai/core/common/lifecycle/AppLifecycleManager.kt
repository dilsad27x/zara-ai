package com.zara.ai.core.common.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.zara.ai.core.common.logger.ZaraLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class AppLifecycleState {
    FOREGROUND,
    BACKGROUND,
    TERMINATING
}

/**
 * Monitors and emits application-wide lifecycle changes.
 */
@Singleton
class AppLifecycleManager @Inject constructor(
    private val logger: ZaraLogger
) : DefaultLifecycleObserver {

    private val _lifecycleState = MutableStateFlow(AppLifecycleState.BACKGROUND)
    val lifecycleState: StateFlow<AppLifecycleState> = _lifecycleState.asStateFlow()

    private var appStartTime: Long = 0

    fun initialize() {
        appStartTime = System.currentTimeMillis()
        logger.i("AppLifecycle", "Initializing AppLifecycleManager at $appStartTime")
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        logger.i("AppLifecycle", "App transitioned to FOREGROUND")
        _lifecycleState.value = AppLifecycleState.FOREGROUND
    }

    override fun onStop(owner: LifecycleOwner) {
        logger.i("AppLifecycle", "App transitioned to BACKGROUND")
        _lifecycleState.value = AppLifecycleState.BACKGROUND
    }

    fun getUptimeMillis(): Long {
        return if (appStartTime == 0L) 0L else System.currentTimeMillis() - appStartTime
    }
}
