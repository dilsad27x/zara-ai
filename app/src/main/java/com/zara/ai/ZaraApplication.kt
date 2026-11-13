package com.zara.ai

import android.app.Application
import com.zara.ai.core.common.error.ErrorHandler
import com.zara.ai.core.common.lifecycle.AppLifecycleManager
import com.zara.ai.core.common.logger.ZaraLogger
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class ZaraApplication : Application() {

    @Inject
    lateinit var logger: ZaraLogger

    @Inject
    lateinit var lifecycleManager: AppLifecycleManager

    @Inject
    lateinit var errorHandler: ErrorHandler

    override fun onCreate() {
        super.onCreate()

        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        logger.i("ZaraApplication", "ZARA AI Engine initialized. Version: ${BuildConfig.VERSION_NAME} [${BuildConfig.BUILD_TYPE}]")

        // Initialize lifecycle tracking
        lifecycleManager.initialize()

        // Centralized global uncaught exception boundary
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logger.e("ZaraApplication", "Uncaught exception on thread: ${thread.name}", throwable)
            errorHandler.handleThrowable(throwable, "UncaughtThreadException")
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
