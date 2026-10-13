package com.example.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceRepository
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.util.DatabaseDriveManager
import com.example.util.ExcelReportManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScanFeedback(
    val isSuccess: Boolean,
    val title: String,
    val subtitle: String,
    val student: Student? = null,
    val record: AttendanceRecord? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val classes: StateFlow<List<SchoolClass>>
    val students: StateFlow<List<Student>>
    val sessions: StateFlow<List<AttendanceSession>>
    val activeSessions: StateFlow<List<AttendanceSession>>

    private val _selectedClassId = MutableStateFlow<Long?>(null)
    val selectedClassId: StateFlow<Long?> = _selectedClassId.asStateFlow()

    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedSessionName = MutableStateFlow("Kehadiran Pagi")
    val selectedSessionName: StateFlow<String> = _selectedSessionName.asStateFlow()

    private val _scanDefaultStatus = MutableStateFlow("HADIR")
    val scanDefaultStatus: StateFlow<String> = _scanDefaultStatus.asStateFlow()

    private val _scanFeedback = MutableStateFlow<ScanFeedback?>(null)
    val scanFeedback: StateFlow<ScanFeedback?> = _scanFeedback.asStateFlow()

    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    private val _driveBackupStatus = MutableStateFlow<String?>(null)
    val driveBackupStatus: StateFlow<String?> = _driveBackupStatus.asStateFlow()

    private val _lastBackupTime = MutableStateFlow<Long?>(null)
    val lastBackupTime: StateFlow<Long?> = _lastBackupTime.asStateFlow()

    val attendanceRecords: StateFlow<List<AttendanceRecord>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AttendanceRepository(
            db.schoolClassDao(),
            db.studentDao(),
            db.attendanceDao(),
            db.attendanceSessionDao()
        )

        classes = repository.allClasses.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        students = repository.allStudents.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        sessions = repository.allSessions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        activeSessions = repository.activeSessions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        attendanceRecords = combine(
            _selectedDate,
            _selectedClassId
        ) { date, classId ->
            Pair(date, classId)
        }.combine(repository.getAttendanceByDate(_selectedDate.value)) { pair, _ ->
            pair
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Pair(_selectedDate.value, _selectedClassId.value)
        ).combine(repository.allStudents) { _, _ ->
            emptyList<AttendanceRecord>()
        }.combine(repository.getAttendanceByDate(_selectedDate.value)) { _, records ->
            val filterClassId = _selectedClassId.value
            if (filterClassId == null) {
                records
            } else {
                records.filter { it.classId == filterClassId }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Prepopulate with initial data, default sessions, and ensure Class XII 1 is loaded
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setSelectedClass(classId: Long?) {
        _selectedClassId.value = classId
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSelectedSessionName(name: String) {
        _selectedSessionName.value = name
    }

    fun setScanDefaultStatus(status: String) {
        _scanDefaultStatus.value = status
    }

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun clearScanFeedback() {
        _scanFeedback.value = null
    }

    /**
     * Process scanned barcode / QR code for the currently selected session
     */
    fun processBarcodeScan(rawCode: String, method: String = "BARCODE_SCAN") {
        val cleanCode = rawCode.trim().removePrefix("NIS:").trim()
        if (cleanCode.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val student = repository.getStudentByNis(cleanCode)
            if (student == null) {
                triggerHapticAndSound(false)
                _scanFeedback.value = ScanFeedback(
                    isSuccess = false,
                    title = "Siswa Tidak Ditemukan!",
                    subtitle = "Barcode / NIS '$cleanCode' belum terdaftar di sistem. Daftarkan di Dashboard Admin."
                )
                return@launch
            }

            val schoolClass = repository.getClassById(student.classId)
                ?: SchoolClass(id = student.classId, name = "Kelas Siswa")

            val todayDate = _selectedDate.value
            val currentSession = _selectedSessionName.value

            val record = repository.recordAttendance(
                student = student,
                schoolClass = schoolClass,
                status = _scanDefaultStatus.value,
                sessionName = currentSession,
                note = "Dipindai via $method",
                method = method,
                customDate = todayDate
            )

            triggerHapticAndSound(true)
            val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(record.timestamp))
            _scanFeedback.value = ScanFeedback(
                isSuccess = true,
                title = "Presensi Berhasil: ${record.status}",
                subtitle = "${student.name} • ${record.sessionName} • $timeString",
                student = student,
                record = record
            )
        }
    }

    fun updateManualAttendance(
        student: Student,
        schoolClass: SchoolClass,
        status: String,
        sessionName: String = _selectedSessionName.value,
        note: String = "",
        customDate: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordAttendance(
                student = student,
                schoolClass = schoolClass,
                status = status,
                sessionName = sessionName,
                note = note,
                method = "MANUAL",
                customDate = customDate ?: _selectedDate.value
            )
        }
    }

    /**
     * Mark or update student attendance for any session on any date
     */
    fun markStudentSession(
        student: Student,
        schoolClass: SchoolClass,
        sessionName: String,
        status: String,
        date: String = _selectedDate.value,
        note: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordAttendance(
                student = student,
                schoolClass = schoolClass,
                status = status,
                sessionName = sessionName,
                note = note,
                method = "MANUAL",
                customDate = date
            )
        }
    }

    /**
     * Quick mark all students in class (or all) for a specific session
     */
    fun quickMarkAllClassSession(
        classId: Long?,
        sessionName: String,
        status: String,
        date: String = _selectedDate.value
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val allStudentsList = repository.getAllStudentsSnapshot()
            val targetStudents = if (classId != null) allStudentsList.filter { it.classId == classId } else allStudentsList
            val allClassesList = repository.getAllClassesSnapshot()

            targetStudents.forEach { student ->
                val schoolClass = allClassesList.find { it.id == student.classId }
                    ?: SchoolClass(id = student.classId, name = "Kelas")
                repository.recordAttendance(
                    student = student,
                    schoolClass = schoolClass,
                    status = status,
                    sessionName = sessionName,
                    note = "Tandai Massal",
                    method = "MANUAL",
                    customDate = date
                )
            }
        }
    }

    fun deleteAttendanceRecord(record: AttendanceRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAttendance(record)
        }
    }

    // Class Management
    fun saveClass(schoolClass: SchoolClass) {
        viewModelScope.launch(Dispatchers.IO) {
            if (schoolClass.id == 0L) {
                repository.insertClass(schoolClass)
            } else {
                repository.updateClass(schoolClass)
            }
        }
    }

    fun deleteClass(schoolClass: SchoolClass) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteClass(schoolClass)
        }
    }

    // Student Management
    fun saveStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            if (student.id == 0L) {
                repository.insertStudent(student)
            } else {
                repository.updateStudent(student)
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudent(student)
        }
    }

    // Session Management (Kelola Sesi & Kategori Absensi)
    fun saveSession(session: AttendanceSession) {
        viewModelScope.launch(Dispatchers.IO) {
            if (session.id == 0L) {
                repository.insertSession(session)
            } else {
                repository.updateSession(session)
            }
        }
    }

    fun deleteSession(session: AttendanceSession) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSession(session)
        }
    }

    fun resetSessionsToDefault() {
        viewModelScope.launch(Dispatchers.IO) {
            val defaultSessions = listOf(
                AttendanceSession(name = "Kehadiran Pagi", description = "Presensi Masuk Pagi / Masuk Sekolah", timeHint = "07:00", isActive = true, orderIndex = 1),
                AttendanceSession(name = "Sholat Dzuhur", description = "Presensi Sholat Berjamaah Dzuhur", timeHint = "12:15", isActive = true, orderIndex = 2),
                AttendanceSession(name = "Sholat Ashar", description = "Presensi Sholat Berjamaah Ashar", timeHint = "15:30", isActive = true, orderIndex = 3),
                AttendanceSession(name = "Pulang", description = "Presensi Kepulangan Siswa", timeHint = "16:00", isActive = true, orderIndex = 4),
                AttendanceSession(name = "Ekstrakurikuler", description = "Kegiatan Ekstrakurikuler / Tambahan", timeHint = "16:30", isActive = false, orderIndex = 5)
            )
            repository.insertSessions(defaultSessions)
        }
    }

    // Google Drive Database Backup & Restore
    fun exportAndShareToGoogleDrive(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _driveBackupStatus.value = "Membuat cadangan database..."
                val json = DatabaseDriveManager.createDatabaseJson(repository)
                val file = DatabaseDriveManager.saveJsonToBackupFile(context, json)
                _lastBackupTime.value = System.currentTimeMillis()
                _driveBackupStatus.value = "Berhasil membuat file cadangan (${(file.length() / 1024L).coerceAtLeast(1L)} KB). Membuka Google Drive..."
                DatabaseDriveManager.shareDatabaseToGoogleDrive(context, file)
            } catch (e: Exception) {
                e.printStackTrace()
                _driveBackupStatus.value = "Gagal mencadangkan: ${e.message}"
            }
        }
    }

    fun restoreDatabase(jsonString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _driveBackupStatus.value = "Memulihkan data database..."
                val stats = DatabaseDriveManager.restoreDatabaseFromJson(repository, jsonString)
                _driveBackupStatus.value = "Pemulihan sukses! ${stats.classCount} Kelas, ${stats.studentCount} Siswa, dan ${stats.attendanceCount} Record Kehadiran."
            } catch (e: Exception) {
                e.printStackTrace()
                _driveBackupStatus.value = "Gagal memulihkan database: Pastikan format JSON valid."
            }
        }
    }

    fun clearDriveStatus() {
        _driveBackupStatus.value = null
    }

    // Bulk Student Import from Excel / List
    fun importStudentsBulk(studentsToImport: List<Student>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertStudents(studentsToImport)
                _driveBackupStatus.value = "Berhasil mengimpor ${studentsToImport.size} siswa ke database!"
            } catch (e: Exception) {
                e.printStackTrace()
                _driveBackupStatus.value = "Gagal mengimpor siswa: ${e.message}"
            }
        }
    }

    // Export Attendance Matrix (All sessions or Single Session) to Excel (.csv) and upload/share to Google Drive
    fun exportAndShareExcelReport(
        context: Context,
        classId: Long?,
        dateString: String,
        sessionName: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _driveBackupStatus.value = "Menyiapkan rekap format Excel..."
                val allClasses = repository.getAllClassesSnapshot()
                val targetClass = allClasses.find { it.id == classId }
                val allStudents = repository.getAllStudentsSnapshot()
                val targetStudents = if (classId != null) allStudents.filter { it.classId == classId } else allStudents
                val allAttendance = repository.getAllAttendanceSnapshot()
                val targetAttendance = allAttendance.filter { it.dateString == dateString && (classId == null || it.classId == classId) }
                val activeSessionsList = repository.getAllSessionsSnapshot().filter { it.isActive }

                val isMatrix = sessionName.isNullOrBlank() || sessionName == "Semua Sesi (Matriks)"

                val csvContent = if (isMatrix) {
                    ExcelReportManager.generateClassAttendanceMatrixCsv(
                        schoolClass = targetClass,
                        students = targetStudents,
                        sessions = activeSessionsList,
                        records = targetAttendance,
                        dateString = dateString
                    )
                } else {
                    ExcelReportManager.generateClassAttendanceCsv(
                        schoolClass = targetClass,
                        students = targetStudents,
                        records = targetAttendance,
                        dateString = dateString,
                        sessionName = sessionName
                    )
                }

                val classNameSanitized = targetClass?.name?.replace(" ", "_") ?: "Semua_Kelas"
                val sessionSanitized = if (isMatrix) "Matriks_Lengkap" else (sessionName?.replace(" ", "_") ?: "Semua_Sesi")
                val fileNamePrefix = "rekap_absen_${classNameSanitized}_${sessionSanitized}_${dateString}"

                val file = ExcelReportManager.saveCsvToCache(
                    context = context,
                    csvContent = csvContent,
                    fileNamePrefix = fileNamePrefix
                )

                _driveBackupStatus.value = "File Excel siap (${file.name}). Membuka Google Drive..."
                ExcelReportManager.shareExcelToGoogleDrive(
                    context = context,
                    csvFile = file,
                    title = "Rekap Absensi ${targetClass?.name ?: "Semua Kelas"} ($sessionSanitized) - $dateString"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _driveBackupStatus.value = "Gagal mengekspor Excel: ${e.message}"
            }
        }
    }

    fun exportAndShareStudentExcelReport(
        context: Context,
        student: Student
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _driveBackupStatus.value = "Menyiapkan rekap individual Excel..."
                val allClasses = repository.getAllClassesSnapshot()
                val targetClass = allClasses.find { it.id == student.classId }
                val allAttendance = repository.getAllAttendanceSnapshot()
                val studentRecords = allAttendance.filter { it.studentId == student.id }

                val csvContent = ExcelReportManager.generateStudentAttendanceHistoryCsv(
                    student = student,
                    schoolClass = targetClass,
                    records = studentRecords
                )

                val studentNameSanitized = student.name.replace(" ", "_")
                val fileNamePrefix = "rekap_siswa_${student.nis}_${studentNameSanitized}"

                val file = ExcelReportManager.saveCsvToCache(
                    context = context,
                    csvContent = csvContent,
                    fileNamePrefix = fileNamePrefix
                )

                _driveBackupStatus.value = "File Excel siap (${file.name}). Membuka Google Drive..."
                ExcelReportManager.shareExcelToGoogleDrive(
                    context = context,
                    csvFile = file,
                    title = "Rekap Absensi Siswa ${student.name} (${student.nis})"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _driveBackupStatus.value = "Gagal mengekspor Excel siswa: ${e.message}"
            }
        }
    }

    fun getStudentAttendanceHistory(studentId: Long) = repository.getStudentAttendanceHistory(studentId)

    private fun triggerHapticAndSound(success: Boolean) {
        try {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (success) {
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (success) 100L else 300L)
            }

            val toneType = if (success) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            toneGen.startTone(toneType, 150)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
