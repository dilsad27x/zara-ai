package com.zara.ai.core.common.dispatcher

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Dispatcher(val dispatcher: ZaraDispatchers)

enum class ZaraDispatchers {
    IO,
    Default,
    Main,
    Unconfined
}
