package com.zara.ai.core.network.model

sealed interface NetworkStatus {
    data object Available : NetworkStatus
    data object Unavailable : NetworkStatus
    data object Losing : NetworkStatus
    data object Lost : NetworkStatus

    val isConnected: Boolean get() = this is Available
}

data class ZaraApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val errorMessage: String? = null,
    val statusCode: Int = 200
)
