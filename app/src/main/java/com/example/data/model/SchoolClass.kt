package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class SchoolClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gradeLevel: String = "10",
    val academicYear: String = "2026/2027",
    val homeroomTeacher: String = ""
)
