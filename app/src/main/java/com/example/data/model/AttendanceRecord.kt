package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["studentId", "dateString", "sessionName"], unique = true),
        Index(value = ["dateString"]),
        Index(value = ["classId"]),
        Index(value = ["sessionName"])
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val studentNis: String,
    val studentName: String,
    val classId: Long,
    val className: String,
    val dateString: String, // Format: yyyy-MM-dd
    val sessionName: String = "Kehadiran Pagi", // "Kehadiran Pagi", "Sholat Dzuhur", "Sholat Ashar", "Pulang", etc.
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "HADIR", "TERLAMBAT", "IZIN", "SAKIT", "ALPA"
    val note: String = "",
    val scanMethod: String = "BARCODE_SCAN" // "BARCODE_SCAN", "QR_SCAN", "MANUAL"
)
