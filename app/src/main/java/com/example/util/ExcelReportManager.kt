package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelReportManager {

    /**
     * Generate an Excel-compatible multi-session matrix CSV file (with UTF-8 BOM so Excel opens it with perfect columns)
     * Header: No;NIS;Nama Siswa;Kelas;Jenis Kelamin;[Sesi 1];[Sesi 2];...;Hadir;Terlambat;Izin;Sakit;Alpa;Catatan
     */
    fun generateClassAttendanceMatrixCsv(
        schoolClass: SchoolClass?,
        students: List<Student>,
        sessions: List<AttendanceSession>,
        records: List<AttendanceRecord>,
        dateString: String
    ): String {
        val sb = StringBuilder()
        // UTF-8 BOM for Microsoft Excel compatibility
        sb.append("\uFEFF")

        val activeSessionsList = if (sessions.isNotEmpty()) sessions else listOf(
            AttendanceSession(name = "Kehadiran Pagi"),
            AttendanceSession(name = "Sholat Dzuhur"),
            AttendanceSession(name = "Sholat Ashar"),
            AttendanceSession(name = "Pulang")
        )

        // Title and Header metadata
        sb.appendLine("REKAPITULASI PRESENSI SISWA (MULTI-SESI / HARIAN)")
        sb.appendLine("Kelas;${schoolClass?.name ?: "Semua Kelas"}")
        sb.appendLine("Wali Kelas;${schoolClass?.homeroomTeacher ?: "-"}")
        sb.appendLine("Tanggal;${dateString}")
        sb.appendLine("Tanggal Ekspor;${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        sb.appendLine()

        // Sesi Info
        sb.appendLine("SESI PRESENSI AKTIF: ${activeSessionsList.joinToString(", ") { it.name }}")
        sb.appendLine()

        // Detailed Matrix Header
        sb.append("No;NIS;Nama Siswa;Kelas;Jenis Kelamin")
        for (session in activeSessionsList) {
            sb.append(";\"${session.name}\"")
        }
        sb.appendLine(";Total Hadir;Total Terlambat;Total Izin/Sakit;Total Alpa")

        // Rows
        students.sortedBy { it.name }.forEachIndexed { index, student ->
            val studentRecords = records.filter { it.studentId == student.id }
            val gender = if (student.gender == "L") "Laki-laki" else "Perempuan"
            val className = schoolClass?.name ?: "Kelas ${student.classId}"

            sb.append("${index + 1};\"${student.nis}\";\"${student.name}\";\"$className\";\"$gender\"")

            var hadirCount = 0
            var terlambatCount = 0
            var izinSakitCount = 0
            var alpaCount = 0

            for (session in activeSessionsList) {
                val rec = studentRecords.find { it.sessionName.equals(session.name, ignoreCase = true) }
                val cellVal = if (rec != null) {
                    when (rec.status) {
                        "HADIR" -> { hadirCount++; "HADIR" }
                        "TERLAMBAT" -> { terlambatCount++; "TERLAMBAT" }
                        "IZIN" -> { izinSakitCount++; "IZIN" }
                        "SAKIT" -> { izinSakitCount++; "SAKIT" }
                        "ALPA" -> { alpaCount++; "ALPA" }
                        else -> rec.status
                    }
                } else {
                    "-"
                }
                sb.append(";\"$cellVal\"")
            }

            sb.appendLine(";$hadirCount;$terlambatCount;$izinSakitCount;$alpaCount")
        }

        return sb.toString()
    }

    /**
     * Generate an Excel-compatible CSV file for a specific session or general list
     */
    fun generateClassAttendanceCsv(
        schoolClass: SchoolClass?,
        students: List<Student>,
        records: List<AttendanceRecord>,
        dateString: String,
        sessionName: String? = null
    ): String {
        val sb = StringBuilder()
        // UTF-8 BOM for Microsoft Excel compatibility
        sb.append("\uFEFF")

        // Title and Header metadata
        sb.appendLine("REKAPITULASI PRESENSI SISWA")
        sb.appendLine("Kelas;${schoolClass?.name ?: "Semua Kelas"}")
        sb.appendLine("Wali Kelas;${schoolClass?.homeroomTeacher ?: "-"}")
        sb.appendLine("Sesi / Kegiatan;${sessionName ?: "Semua Sesi"}")
        sb.appendLine("Tanggal;${dateString}")
        sb.appendLine("Tanggal Ekspor;${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        sb.appendLine()

        val filteredRecords = if (sessionName.isNullOrBlank() || sessionName == "Semua Sesi") {
            records
        } else {
            records.filter { it.sessionName.equals(sessionName, ignoreCase = true) }
        }

        // Statistics Summary
        val hadir = filteredRecords.count { it.status == "HADIR" }
        val terlambat = filteredRecords.count { it.status == "TERLAMBAT" }
        val izin = filteredRecords.count { it.status == "IZIN" }
        val sakit = filteredRecords.count { it.status == "SAKIT" }
        val alpa = filteredRecords.count { it.status == "ALPA" }
        val belumAbsen = (students.size - (hadir + terlambat + izin + sakit + alpa)).coerceAtLeast(0)

        sb.appendLine("RINGKASAN KEHADIRAN")
        sb.appendLine("Total Siswa;${students.size}")
        sb.appendLine("Hadir;${hadir}")
        sb.appendLine("Terlambat;${terlambat}")
        sb.appendLine("Izin;${izin}")
        sb.appendLine("Sakit;${sakit}")
        sb.appendLine("Alpa;${alpa}")
        sb.appendLine("Belum Absen;${belumAbsen}")
        sb.appendLine()

        // Detailed Table Header
        sb.appendLine("No;NIS;Nama Siswa;Kelas;Jenis Kelamin;Sesi;Status;Jam Scan;Metode;Catatan")

        // Table Rows
        students.sortedBy { it.name }.forEachIndexed { index, student ->
            val record = filteredRecords.find { it.studentId == student.id }
            val status = record?.status ?: "BELUM ABSEN"
            val currentSession = record?.sessionName ?: (sessionName ?: "-")
            val scanTime = if (record != null) {
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(record.timestamp))
            } else {
                "-"
            }
            val method = record?.scanMethod ?: "-"
            val note = record?.note ?: ""
            val gender = if (student.gender == "L") "Laki-laki" else "Perempuan"
            val className = schoolClass?.name ?: "Kelas ${student.classId}"

            sb.appendLine(
                "${index + 1};\"${student.nis}\";\"${student.name}\";\"$className\";\"$gender\";\"$currentSession\";\"$status\";\"$scanTime\";\"$method\";\"$note\""
            )
        }

        return sb.toString()
    }

    /**
     * Generate an Excel-compatible CSV file for an individual student's full attendance history
     */
    fun generateStudentAttendanceHistoryCsv(
        student: Student,
        schoolClass: SchoolClass?,
        records: List<AttendanceRecord>
    ): String {
        val sb = StringBuilder()
        sb.append("\uFEFF")

        sb.appendLine("REKAPITULASI PRESENSI INDIVIDUAL SISWA")
        sb.appendLine("NIS;\"${student.nis}\"")
        sb.appendLine("Nama Siswa;\"${student.name}\"")
        sb.appendLine("Kelas;${schoolClass?.name ?: "Kelas ${student.classId}"}")
        sb.appendLine("Jenis Kelamin;${if (student.gender == "L") "Laki-laki" else "Perempuan"}")
        sb.appendLine("Tanggal Ekspor;${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        sb.appendLine()

        val hadir = records.count { it.status == "HADIR" }
        val terlambat = records.count { it.status == "TERLAMBAT" }
        val izin = records.count { it.status == "IZIN" }
        val sakit = records.count { it.status == "SAKIT" }
        val alpa = records.count { it.status == "ALPA" }

        sb.appendLine("RINGKASAN KEHADIRAN SISWA")
        sb.appendLine("Total Catatan;${records.size}")
        sb.appendLine("Hadir;${hadir}")
        sb.appendLine("Terlambat;${terlambat}")
        sb.appendLine("Izin;${izin}")
        sb.appendLine("Sakit;${sakit}")
        sb.appendLine("Alpa;${alpa}")
        sb.appendLine()

        sb.appendLine("No;Tanggal;Sesi / Kegiatan;Status;Jam Presensi;Metode;Catatan")

        records.sortedByDescending { it.dateString + it.timestamp }.forEachIndexed { index, rec ->
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(rec.timestamp))
            sb.appendLine(
                "${index + 1};\"${rec.dateString}\";\"${rec.sessionName}\";\"${rec.status}\";\"$timeStr\";\"${rec.scanMethod}\";\"${rec.note}\""
            )
        }

        return sb.toString()
    }

    /**
     * Save the CSV to a cache file and return File
     */
    fun saveCsvToCache(context: Context, csvContent: String, fileNamePrefix: String): File {
        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${fileNamePrefix}_$timeStamp.csv"
        val file = File(backupDir, fileName)

        FileOutputStream(file).use { out ->
            out.write(csvContent.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    /**
     * Share/Upload Excel CSV directly to Google Drive
     */
    fun shareExcelToGoogleDrive(context: Context, csvFile: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            csvFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(
                Intent.EXTRA_TEXT,
                "Rekapitulasi Kehadiran Siswa Format Excel (CSV) untuk Google Drive.\nNama File: ${csvFile.name}"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Simpan Rekap Excel ke Google Drive")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
