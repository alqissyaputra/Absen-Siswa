package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["nis"], unique = true),
        Index(value = ["classId"])
    ]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nis: String, // Unique student ID for barcode/QR
    val name: String,
    val classId: Long,
    val gender: String = "L", // "L" (Laki-laki) or "P" (Perempuan)
    val phone: String = "",
    val address: String = "",
    val barcodeType: String = "QR_CODE" // "QR_CODE" or "CODE_128"
)
