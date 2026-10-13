package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceSession
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceSessionDao {
    @Query("SELECT * FROM attendance_sessions ORDER BY orderIndex ASC, id ASC")
    fun getAllSessions(): Flow<List<AttendanceSession>>

    @Query("SELECT * FROM attendance_sessions ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllSessionsList(): List<AttendanceSession>

    @Query("SELECT * FROM attendance_sessions WHERE isActive = 1 ORDER BY orderIndex ASC, id ASC")
    fun getActiveSessions(): Flow<List<AttendanceSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<AttendanceSession>)

    @Update
    suspend fun updateSession(session: AttendanceSession)

    @Delete
    suspend fun deleteSession(session: AttendanceSession)

    @Query("DELETE FROM attendance_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT COUNT(*) FROM attendance_sessions")
    suspend fun getSessionCount(): Int
}
