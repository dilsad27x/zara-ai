package com.zara.ai.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class MemoryCategory {
    FACT, PREFERENCE, IDENTITY, RULE, SYSTEM_CAPABILITY
}

@Entity(
    tableName = "memories",
    indices = [
        Index("category"),
        Index("key", unique = true)
    ]
)
data class MemoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val category: MemoryCategory,
    val confidence: Float = 1.0f,
    val source: String = "interaction",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
