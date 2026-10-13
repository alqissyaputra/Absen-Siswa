package com.example.data

import com.example.data.dao.AttendanceDao
import com.example.data.dao.AttendanceSessionDao
import com.example.data.dao.SchoolClassDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceRepository(
    private val classDao: SchoolClassDao,
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val sessionDao: AttendanceSessionDao
) {
    val allClasses: Flow<List<SchoolClass>> = classDao.getAllClasses()
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val allSessions: Flow<List<AttendanceSession>> = sessionDao.getAllSessions()
    val activeSessions: Flow<List<AttendanceSession>> = sessionDao.getActiveSessions()

    fun getStudentsByClass(classId: Long): Flow<List<Student>> = studentDao.getStudentsByClass(classId)
    fun searchStudents(query: String): Flow<List<Student>> = studentDao.searchStudents(query)

    fun getAttendanceByDate(dateString: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByDate(dateString)

    fun getAttendanceByDateAndClass(dateString: String, classId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByDateAndClass(dateString, classId)

    fun getAttendanceByDateAndSession(dateString: String, sessionName: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByDateAndSession(dateString, sessionName)

    fun getAttendanceByDateClassAndSession(dateString: String, classId: Long, sessionName: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceByDateClassAndSession(dateString, classId, sessionName)

    suspend fun getStudentByNis(nis: String): Student? = studentDao.getStudentByNis(nis)
    suspend fun getClassById(id: Long): SchoolClass? = classDao.getClassById(id)

    suspend fun getAllClassesSnapshot(): List<SchoolClass> = classDao.getAllClassesList()
    suspend fun getAllStudentsSnapshot(): List<Student> = studentDao.getAllStudentsList()
    suspend fun getAllAttendanceSnapshot(): List<AttendanceRecord> = attendanceDao.getAllAttendanceList()
    suspend fun getAllSessionsSnapshot(): List<AttendanceSession> = sessionDao.getAllSessionsList()

    suspend fun restoreAllData(
        classes: List<SchoolClass>,
        students: List<Student>,
        attendance: List<AttendanceRecord>
    ) {
        if (classes.isNotEmpty()) {
            classDao.insertClasses(classes)
        }
        if (students.isNotEmpty()) {
            studentDao.insertStudents(students)
        }
        if (attendance.isNotEmpty()) {
            attendanceDao.insertAttendanceRecords(attendance)
        }
    }

    // Class CRUD
    suspend fun insertClass(schoolClass: SchoolClass): Long = classDao.insertClass(schoolClass)
    suspend fun updateClass(schoolClass: SchoolClass) = classDao.updateClass(schoolClass)
    suspend fun deleteClass(schoolClass: SchoolClass) = classDao.deleteClass(schoolClass)

    // Student CRUD
    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = studentDao.deleteStudent(student)
    suspend fun insertStudents(students: List<Student>) = studentDao.insertStudents(students)

    // Session CRUD (Manage Attendance Categories / Sesi Presensi)
    suspend fun insertSession(session: AttendanceSession): Long = sessionDao.insertSession(session)
    suspend fun insertSessions(sessions: List<AttendanceSession>) = sessionDao.insertSessions(sessions)
    suspend fun updateSession(session: AttendanceSession) = sessionDao.updateSession(session)
    suspend fun deleteSession(session: AttendanceSession) = sessionDao.deleteSession(session)
    suspend fun deleteSessionById(id: Long) = sessionDao.deleteSessionById(id)

    // Attendance Operations
    suspend fun recordAttendance(
        student: Student,
        schoolClass: SchoolClass,
        status: String = "HADIR",
        sessionName: String = "Kehadiran Pagi",
        note: String = "",
        method: String = "BARCODE_SCAN",
        customDate: String? = null
    ): AttendanceRecord {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = customDate ?: dateFormat.format(Date())

        val record = AttendanceRecord(
            studentId = student.id,
            studentNis = student.nis,
            studentName = student.name,
            classId = schoolClass.id,
            className = schoolClass.name,
            dateString = dateString,
            sessionName = sessionName,
            timestamp = System.currentTimeMillis(),
            status = status,
            note = note,
            scanMethod = method
        )
        val id = attendanceDao.insertOrUpdateAttendance(record)
        return record.copy(id = id)
    }

    suspend fun deleteAttendance(record: AttendanceRecord) = attendanceDao.deleteAttendance(record)

    fun getStudentAttendanceHistory(studentId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.getStudentAttendanceHistory(studentId)

    suspend fun seedInitialDataIfEmpty() {
        ensureDefaultSessionsLoaded()
        ensureClassXii1Loaded()
    }

    suspend fun ensureDefaultSessionsLoaded() {
        if (sessionDao.getSessionCount() == 0) {
            val defaultSessions = listOf(
                AttendanceSession(name = "Kehadiran Pagi", description = "Presensi Masuk Pagi / Masuk Sekolah", timeHint = "07:00", isActive = true, orderIndex = 1),
                AttendanceSession(name = "Sholat Dzuhur", description = "Presensi Sholat Berjamaah Dzuhur", timeHint = "12:15", isActive = true, orderIndex = 2),
                AttendanceSession(name = "Sholat Ashar", description = "Presensi Sholat Berjamaah Ashar", timeHint = "15:30", isActive = true, orderIndex = 3),
                AttendanceSession(name = "Pulang", description = "Presensi Kepulangan Siswa", timeHint = "16:00", isActive = true, orderIndex = 4),
                AttendanceSession(name = "Ekstrakurikuler", description = "Kegiatan Ekstrakurikuler / Tambahan", timeHint = "16:30", isActive = false, orderIndex = 5)
            )
            sessionDao.insertSessions(defaultSessions)
        }
    }

    suspend fun ensureClassXii1Loaded() {
        ensureDefaultSessionsLoaded()
        val existingStudents = studentDao.getAllStudentsList()
        val hasXii1 = existingStudents.any { it.nis == "0000" }
        if (!hasXii1) {
            val allClasses = classDao.getAllClassesList()
            var classXii1 = allClasses.find { it.name.equals("XII 1", ignoreCase = true) }
            val classId = if (classXii1 == null) {
                classDao.insertClass(
                    SchoolClass(
                        name = "XII 1",
                        gradeLevel = "12",
                        academicYear = "2026/2027",
                        homeroomTeacher = "Drs. H. Mulyono, M.Pd"
                    )
                )
            } else {
                classXii1.id
            }

            val namesXii1 = listOf(
                "ABID SYAF'IUL HIDAYAT",
                "ABIYYU RACHMAN FACHSAN",
                "ADIB ZADA",
                "AHZA WIZARSYA PRIYAMA",
                "AKHYAR RASYID WIBOWO",
                "ALIF ZHAFRAN FARID",
                "ASYROF ALHATIM",
                "DAFFA ARKANA IBNI",
                "DHIYA' ABD. RAHMAN EL FAIZ",
                "ERLAND ARYASATYA RACHMAN HARAHAP",
                "FADHIL AL ANSORI",
                "FARRAS AHMAD FAIZ",
                "HANIF ABDURRAHMAN",
                "HELMI MUSTAFA ALI",
                "KEANDRE NAUFAL ADIWITYA",
                "M. FATHI ALVARO",
                "MOHAMMAD ROFIQ AL QOWIYY",
                "MUHAMMAD ANAS FIHIFDZILLAH",
                "MUHAMMAD RAYYAN ATHAILLAH",
                "MUHAMMAD SETYA ADINATA",
                "MUHAMMAD ZUHDI FAYYADH",
                "NAWABIL ALNAZURA",
                "RANU HANDIKA",
                "RAZIB ARYAFITRAH",
                "RAZIN ZHAFRAN KHAIRY",
                "SYATHIR ATHALLAH ALVIS",
                "THAQIF ALIY SULTHAN NANOS",
                "FADHLI AKBAR CHANDRA"
            )

            val studentList = namesXii1.mapIndexed { index, name ->
                val nisFormatted = String.format(Locale.US, "%04d", index)
                Student(
                    nis = nisFormatted,
                    name = name,
                    classId = classId,
                    gender = "L",
                    phone = "",
                    barcodeType = "QR_CODE"
                )
            }
            studentDao.insertStudents(studentList)
        }
    }
}
