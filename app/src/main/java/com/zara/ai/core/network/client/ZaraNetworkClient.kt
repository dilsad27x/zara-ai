package com.zara.ai.core.network.client

import com.zara.ai.BuildConfig
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.error.ZaraError
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.common.result.ZaraResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZaraNetworkClient @Inject constructor(
    private val logger: ZaraLogger,
    @Dispatcher(ZaraDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    val baseUrl: String = BuildConfig.BASE_URL

    suspend fun <T> executeRequest(
        endpoint: String,
        action: suspend () -> T
    ): ZaraResult<T> = withContext(ioDispatcher) {
        try {
            logger.d("ZaraNetworkClient", "Executing network request to: $baseUrl$endpoint")
            val result = action()
            ZaraResult.Success(result)
        } catch (e: Exception) {
            logger.e("ZaraNetworkClient", "Network request failed: $endpoint", e)
            ZaraResult.Error(
                ZaraError.NetworkError(
                    endpoint = endpoint,
                    message = e.localizedMessage ?: "Network call failed",
                    cause = e
                )
            )
        }
    }
}
