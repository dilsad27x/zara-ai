package com.zara.ai.core.database.converter

import androidx.room.TypeConverter
import com.zara.ai.core.database.entity.AgentTaskStatus
import com.zara.ai.core.database.entity.MemoryCategory
import com.zara.ai.core.database.entity.MessageRole

class Converters {

    @TypeConverter
    fun fromMessageRole(role: MessageRole): String = role.name

    @TypeConverter
    fun toMessageRole(value: String): MessageRole = try {
        MessageRole.valueOf(value)
    } catch (_: Exception) {
        MessageRole.USER
    }

    @TypeConverter
    fun fromMemoryCategory(category: MemoryCategory): String = category.name

    @TypeConverter
    fun toMemoryCategory(value: String): MemoryCategory = try {
        MemoryCategory.valueOf(value)
    } catch (_: Exception) {
        MemoryCategory.FACT
    }

    @TypeConverter
    fun fromAgentTaskStatus(status: AgentTaskStatus): String = status.name

    @TypeConverter
    fun toAgentTaskStatus(value: String): AgentTaskStatus = try {
        AgentTaskStatus.valueOf(value)
    } catch (_: Exception) {
        AgentTaskStatus.PENDING
    }
}
