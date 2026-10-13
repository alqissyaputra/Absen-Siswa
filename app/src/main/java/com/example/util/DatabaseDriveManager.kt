package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.AttendanceRepository
import com.example.data.model.AttendanceRecord
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupStats(
    val classCount: Int,
    val studentCount: Int,
    val attendanceCount: Int,
    val timestamp: Long,
    val fileSizeKb: Long
)

object DatabaseDriveManager {

    /**
     * Export all classes, students, and attendance records to a JSON String
     */
    suspend fun createDatabaseJson(repository: AttendanceRepository): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Absen Siswa")
        root.put("googleCloudProjectId", "gen-lang-client-0243411593")
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put(
            "exportDateFormatted",
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        // Classes
        val classes = repository.getAllClassesSnapshot()
        val classesArray = JSONArray()
        for (c in classes) {
            val classObj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("gradeLevel", c.gradeLevel)
                put("academicYear", c.academicYear)
                put("homeroomTeacher", c.homeroomTeacher)
            }
            classesArray.put(classObj)
        }
        root.put("classes", classesArray)

        // Students
        val students = repository.getAllStudentsSnapshot()
        val studentsArray = JSONArray()
        for (s in students) {
            val studentObj = JSONObject().apply {
                put("id", s.id)
                put("nis", s.nis)
                put("name", s.name)
                put("classId", s.classId)
                put("gender", s.gender)
                put("phone", s.phone)
                put("address", s.address)
                put("barcodeType", s.barcodeType)
            }
            studentsArray.put(studentObj)
        }
        root.put("students", studentsArray)

        // Attendance Records
        val attendance = repository.getAllAttendanceSnapshot()
        val attendanceArray = JSONArray()
        for (a in attendance) {
            val attObj = JSONObject().apply {
                put("id", a.id)
                put("studentId", a.studentId)
                put("studentNis", a.studentNis)
                put("studentName", a.studentName)
                put("classId", a.classId)
                put("className", a.className)
                put("dateString", a.dateString)
                put("timestamp", a.timestamp)
                put("status", a.status)
                put("note", a.note)
                put("scanMethod", a.scanMethod)
            }
            attendanceArray.put(attObj)
        }
        root.put("attendanceRecords", attendanceArray)

        return root.toString(2)
    }

    /**
     * Saves JSON to a local backup file and returns the File object
     */
    fun saveJsonToBackupFile(context: Context, jsonString: String): File {
        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "database_absen_siswa_$timeStamp.json"
        val file = File(backupDir, fileName)

        FileOutputStream(file).use { out ->
            out.write(jsonString.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    /**
     * Share database file directly to Google Drive ("Save to Drive" / Simpan ke Drive)
     */
    fun shareDatabaseToGoogleDrive(context: Context, backupFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Backup Database Absen Siswa (${backupFile.name})")
            putExtra(
                Intent.EXTRA_TEXT,
                "Cadangan Database Absen Siswa (Kelas, Siswa & Log Kehadiran) untuk Google Drive.\nProyek: gen-lang-client-0243411593"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Simpan / Unggah ke Google Drive")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Restore database from JSON String
     */
    suspend fun restoreDatabaseFromJson(repository: AttendanceRepository, jsonString: String): BackupStats {
        val root = JSONObject(jsonString)

        val classesArray = root.optJSONArray("classes") ?: JSONArray()
        val restoredClasses = mutableListOf<SchoolClass>()
        for (i in 0 until classesArray.length()) {
            val obj = classesArray.getJSONObject(i)
            restoredClasses.add(
                SchoolClass(
                    id = obj.optLong("id", 0L),
                    name = obj.getString("name"),
                    gradeLevel = obj.optString("gradeLevel", "10"),
                    academicYear = obj.optString("academicYear", "2026/2027"),
                    homeroomTeacher = obj.optString("homeroomTeacher", "")
                )
            )
        }

        val studentsArray = root.optJSONArray("students") ?: JSONArray()
        val restoredStudents = mutableListOf<Student>()
        for (i in 0 until studentsArray.length()) {
            val obj = studentsArray.getJSONObject(i)
            restoredStudents.add(
                Student(
                    id = obj.optLong("id", 0L),
                    nis = obj.getString("nis"),
                    name = obj.getString("name"),
                    classId = obj.getLong("classId"),
                    gender = obj.optString("gender", "L"),
                    phone = obj.optString("phone", ""),
                    address = obj.optString("address", ""),
                    barcodeType = obj.optString("barcodeType", "QR_CODE")
                )
            )
        }

        val attendanceArray = root.optJSONArray("attendanceRecords") ?: JSONArray()
        val restoredAttendance = mutableListOf<AttendanceRecord>()
        for (i in 0 until attendanceArray.length()) {
            val obj = attendanceArray.getJSONObject(i)
            restoredAttendance.add(
                AttendanceRecord(
                    id = obj.optLong("id", 0L),
                    studentId = obj.getLong("studentId"),
                    studentNis = obj.getString("studentNis"),
                    studentName = obj.getString("studentName"),
                    classId = obj.getLong("classId"),
                    className = obj.getString("className"),
                    dateString = obj.getString("dateString"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    status = obj.optString("status", "HADIR"),
                    note = obj.optString("note", ""),
                    scanMethod = obj.optString("scanMethod", "BARCODE_SCAN")
                )
            )
        }

        repository.restoreAllData(restoredClasses, restoredStudents, restoredAttendance)

        return BackupStats(
            classCount = restoredClasses.size,
            studentCount = restoredStudents.size,
            attendanceCount = restoredAttendance.size,
            timestamp = System.currentTimeMillis(),
            fileSizeKb = (jsonString.length / 1024L).coerceAtLeast(1L)
        )
    }
}
