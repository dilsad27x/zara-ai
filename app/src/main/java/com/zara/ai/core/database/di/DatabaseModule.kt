package com.zara.ai.core.database.di

import android.content.Context
import androidx.room.Room
import com.zara.ai.core.database.ZaraDatabase
import com.zara.ai.core.database.dao.AgentTaskDao
import com.zara.ai.core.database.dao.ConversationDao
import com.zara.ai.core.database.dao.MemoryDao
import com.zara.ai.core.database.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideZaraDatabase(
        @ApplicationContext context: Context
    ): ZaraDatabase {
        return Room.databaseBuilder(
            context,
            ZaraDatabase::class.java,
            ZaraDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideConversationDao(database: ZaraDatabase): ConversationDao = database.conversationDao()

    @Provides
    fun provideMessageDao(database: ZaraDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideMemoryDao(database: ZaraDatabase): MemoryDao = database.memoryDao()

    @Provides
    fun provideAgentTaskDao(database: ZaraDatabase): AgentTaskDao = database.agentTaskDao()
}
