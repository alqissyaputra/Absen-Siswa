package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SchoolClass
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolClassDao {
    @Query("SELECT * FROM classes ORDER BY gradeLevel ASC, name ASC")
    fun getAllClasses(): Flow<List<SchoolClass>>

    @Query("SELECT * FROM classes ORDER BY id ASC")
    suspend fun getAllClassesList(): List<SchoolClass>

    @Query("SELECT * FROM classes WHERE id = :id LIMIT 1")
    suspend fun getClassById(id: Long): SchoolClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<SchoolClass>)

    @Update
    suspend fun updateClass(schoolClass: SchoolClass)

    @Delete
    suspend fun deleteClass(schoolClass: SchoolClass)

    @Query("DELETE FROM classes WHERE id = :id")
    suspend fun deleteClassById(id: Long)

    @Query("SELECT COUNT(*) FROM classes")
    suspend fun getClassCount(): Int
}
