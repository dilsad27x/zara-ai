package com.zara.ai.core.common.error

import com.zara.ai.core.common.logger.ZaraLogger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class UiErrorEvent(
    val id: Long = System.currentTimeMillis(),
    val error: ZaraError,
    val userFriendlyMessage: String = error.message,
    val isFatal: Boolean = false,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

/**
 * Central event bus and coordinator for handling application-wide errors.
 */
@Singleton
class ErrorHandler @Inject constructor(
    private val logger: ZaraLogger
) {
    private val _errorEvents = MutableSharedFlow<UiErrorEvent>(extraBufferCapacity = 32)
    val errorEvents: SharedFlow<UiErrorEvent> = _errorEvents.asSharedFlow()

    fun emitError(
        error: ZaraError,
        isFatal: Boolean = false,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        logger.e("ErrorHandler", "[${error.code}] ${error.message}", error.cause)
        _errorEvents.tryEmit(
            UiErrorEvent(
                error = error,
                userFriendlyMessage = error.message,
                isFatal = isFatal,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }

    fun handleThrowable(throwable: Throwable, contextTag: String = "General") {
        logger.e(contextTag, "Unhandled exception captured", throwable)
        val zaraError = when (throwable) {
            is ZaraError -> throwable
            else -> ZaraError.UnknownError(
                message = throwable.localizedMessage ?: "Unexpected internal error",
                cause = throwable
            )
        }
        emitError(zaraError)
    }
}
