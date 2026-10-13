package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_sessions")
data class AttendanceSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g. "Kehadiran Pagi", "Sholat Dzuhur", "Sholat Ashar", "Pulang"
    val description: String = "",
    val timeHint: String = "", // e.g. "07:00", "12:15", "15:30", "16:00"
    val isActive: Boolean = true,
    val orderIndex: Int = 0
)
