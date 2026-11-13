package com.zara.ai.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zara.ai.core.database.converter.Converters
import com.zara.ai.core.database.dao.AgentTaskDao
import com.zara.ai.core.database.dao.ConversationDao
import com.zara.ai.core.database.dao.MemoryDao
import com.zara.ai.core.database.dao.MessageDao
import com.zara.ai.core.database.entity.AgentTaskEntity
import com.zara.ai.core.database.entity.ConversationEntity
import com.zara.ai.core.database.entity.MemoryEntity
import com.zara.ai.core.database.entity.MessageEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        AgentTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ZaraDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun agentTaskDao(): AgentTaskDao

    companion object {
        const val DATABASE_NAME = "zara_ai_database"
    }
}
