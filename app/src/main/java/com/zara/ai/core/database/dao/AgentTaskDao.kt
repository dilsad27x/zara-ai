package com.zara.ai.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zara.ai.core.database.entity.AgentTaskEntity
import com.zara.ai.core.database.entity.AgentTaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentTaskDao {

    @Query("SELECT * FROM agent_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<AgentTaskEntity>>

    @Query("SELECT * FROM agent_tasks WHERE status = :status ORDER BY createdAt DESC")
    fun getTasksByStatus(status: AgentTaskStatus): Flow<List<AgentTaskEntity>>

    @Query("SELECT * FROM agent_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): AgentTaskEntity?

    @Query("SELECT COUNT(*) FROM agent_tasks WHERE status = 'RUNNING'")
    fun getActiveTaskCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AgentTaskEntity)

    @Update
    suspend fun updateTask(task: AgentTaskEntity)

    @Delete
    suspend fun deleteTask(task: AgentTaskEntity)

    @Query("DELETE FROM agent_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)
}
