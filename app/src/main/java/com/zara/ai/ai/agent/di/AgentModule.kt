package com.zara.ai.ai.agent.di

import com.zara.ai.ai.agent.ZaraAgentEngine
import com.zara.ai.ai.integrations.IntegrationRegistry
import com.zara.ai.core.common.dispatcher.Dispatcher
import com.zara.ai.core.common.dispatcher.ZaraDispatchers
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.database.dao.AgentTaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgentModule {

    @Provides
    @Singleton
    fun provideZaraAgentEngine(
        logger: ZaraLogger,
        agentTaskDao: AgentTaskDao,
        integrationRegistry: IntegrationRegistry,
        @Dispatcher(ZaraDispatchers.Default) defaultDispatcher: CoroutineDispatcher
    ): ZaraAgentEngine = ZaraAgentEngine(logger, agentTaskDao, integrationRegistry, defaultDispatcher)
}
