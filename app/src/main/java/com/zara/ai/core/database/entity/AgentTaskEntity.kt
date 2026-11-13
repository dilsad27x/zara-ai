package com.zara.ai.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class AgentTaskStatus {
    PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
}

@Entity(
    tableName = "agent_tasks",
    indices = [
        Index("status"),
        Index("createdAt")
    ]
)
data class AgentTaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val status: AgentTaskStatus = AgentTaskStatus.PENDING,
    val progress: Float = 0.0f,
    val executionLog: String = "",
    val resultJson: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
