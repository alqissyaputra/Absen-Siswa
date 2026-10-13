package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY id ASC")
    suspend fun getAllAttendanceList(): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getAttendanceByDate(dateString: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString = :dateString AND classId = :classId ORDER BY timestamp DESC")
    fun getAttendanceByDateAndClass(dateString: String, classId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString = :dateString AND sessionName = :sessionName ORDER BY timestamp DESC")
    fun getAttendanceByDateAndSession(dateString: String, sessionName: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString = :dateString AND classId = :classId AND sessionName = :sessionName ORDER BY timestamp DESC")
    fun getAttendanceByDateClassAndSession(dateString: String, classId: Long, sessionName: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId AND dateString = :dateString AND sessionName = :sessionName LIMIT 1")
    suspend fun getTodaySessionRecord(studentId: Long, dateString: String, sessionName: String): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getStudentAttendanceHistory(studentId: Long): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecord>)

    @Update
    suspend fun updateAttendance(record: AttendanceRecord)

    @Delete
    suspend fun deleteAttendance(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteAttendanceById(id: Long)

    @Query("SELECT COUNT(*) FROM attendance_records WHERE dateString = :dateString AND status = :status")
    suspend fun countByDateAndStatus(dateString: String, status: String): Int
}
