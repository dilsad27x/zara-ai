package com.zara.ai.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Universal Result representation for asynchronous operations across ZARA AI.
 */
sealed interface ZaraResult<out T> {
    data class Success<T>(val data: T) : ZaraResult<T>
    data class Error(val exception: Throwable, val message: String? = exception.message) : ZaraResult<Nothing>
    data object Loading : ZaraResult<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
}

inline fun <T, R> ZaraResult<T>.map(transform: (T) -> R): ZaraResult<R> {
    return when (this) {
        is ZaraResult.Success -> ZaraResult.Success(transform(data))
        is ZaraResult.Error -> ZaraResult.Error(exception, message)
        is ZaraResult.Loading -> ZaraResult.Loading
    }
}

inline fun <T> ZaraResult<T>.onSuccess(action: (T) -> Unit): ZaraResult<T> {
    if (this is ZaraResult.Success) action(data)
    return this
}

inline fun <T> ZaraResult<T>.onError(action: (Throwable, String?) -> Unit): ZaraResult<T> {
    if (this is ZaraResult.Error) action(exception, message)
    return this
}

/**
 * Wraps a Flow of items into a Flow of ZaraResult states.
 */
fun <T> Flow<T>.asZaraResult(): Flow<ZaraResult<T>> = this
    .map<T, ZaraResult<T>> { ZaraResult.Success(it) }
    .onStart { emit(ZaraResult.Loading) }
    .catch { emit(ZaraResult.Error(it, it.localizedMessage)) }
