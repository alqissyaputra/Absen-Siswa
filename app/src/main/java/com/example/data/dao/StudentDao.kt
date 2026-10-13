package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY id ASC")
    suspend fun getAllStudentsList(): List<Student>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY name ASC")
    fun getStudentsByClass(classId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE nis = :nis LIMIT 1")
    suspend fun getStudentByNis(nis: String): Student?

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR nis LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchStudents(query: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int
}
