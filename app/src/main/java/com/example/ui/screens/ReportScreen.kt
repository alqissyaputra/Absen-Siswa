package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.ui.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val classes by viewModel.classes.collectAsState()
    val students by viewModel.students.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()
    val selectedClassId by viewModel.selectedClassId.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Rekap Per Kelas, 1: Rekap Per Siswa

    // Class Mode state
    var showClassDropdown by remember { mutableStateOf(false) }
    var selectedSessionFilter by remember { mutableStateOf("Semua Sesi (Matriks)") }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Quick Marking Dialog State (Tandai status siswa untuk sesi tertentu)
    var markingTarget by remember { mutableStateOf<Triple<Student, String, String>?>(null) } // Student, SessionName, CurrentStatus

    // Student Mode state
    var selectedStudentId by remember { mutableStateOf<Long?>(null) }
    var studentSearchQuery by remember { mutableStateOf("") }
    var showStudentDropdown by remember { mutableStateOf(false) }
    var studentFilterCurrentDateOnly by remember { mutableStateOf(false) }

    val currentClass = classes.find { it.id == selectedClassId }
    val classStudents = remember(students, selectedClassId) {
        if (selectedClassId != null) {
            students.filter { it.classId == selectedClassId }
        } else {
            students
        }
    }

    val activeStudent = remember(students, selectedStudentId) {
        students.find { it.id == selectedStudentId } ?: students.firstOrNull()
    }

    // Sessions list fallback
    val sessionList = if (activeSessions.isNotEmpty()) activeSessions else listOf(
        AttendanceSession(name = "Kehadiran Pagi"),
        AttendanceSession(name = "Sholat Dzuhur"),
        AttendanceSession(name = "Sholat Ashar"),
        AttendanceSession(name = "Pulang")
    )

    // Formatted date string for header
    val formattedDateHeader = remember(selectedDate) {
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (parsed != null) {
                SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(parsed)
            } else {
                selectedDate
            }
        } catch (e: Exception) {
            selectedDate
        }
    }

    // Date navigation helpers
    fun changeDateOffset(days: Int) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            val parsed = sdf.parse(selectedDate) ?: Date()
            cal.time = parsed
            cal.add(Calendar.DAY_OF_YEAR, days)
            viewModel.setSelectedDate(sdf.format(cal.time))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Rekapitulasi Kehadiran",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Laporan multi-sesi per kelas & persiswa • Terhubung Google Drive & Excel",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ==================== DATE NAVIGATOR BAR ====================
        Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { changeDateOffset(-1) },
                    modifier = Modifier.size(32.dp).testTag("date_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Hari Sebelumnya",
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showDatePickerDialog = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF1E3A8A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedDateHeader,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "Ubah Tanggal",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { changeDateOffset(1) },
                        modifier = Modifier.size(32.dp).testTag("date_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Hari Berikutnya",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    if (selectedDate != todayIso) {
                        Surface(
                            color = Color(0xFF2563EB),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { viewModel.setSelectedDate(todayIso) }
                        ) {
                            Text(
                                text = "Hari Ini",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tabs: Rekap Per Kelas vs Rekap Per Siswa
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Class, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rekap Per Kelas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("report_tab_class")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rekap Per Siswa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("report_tab_student")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTab == 0) {
            // ==================== TAB 1: REKAP PER KELAS ====================
            // Class selector and session mode bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Class filter button
                Box {
                    OutlinedButton(
                        onClick = { showClassDropdown = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("report_class_filter_button")
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(currentClass?.name ?: "Semua Kelas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = showClassDropdown,
                        onDismissRequest = { showClassDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Kelas") },
                            onClick = {
                                viewModel.setSelectedClass(null)
                                showClassDropdown = false
                            }
                        )
                        classes.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    viewModel.setSelectedClass(c.id)
                                    showClassDropdown = false
                                }
                            )
                        }
                    }
                }

                // Upload Excel to Drive Button
                Button(
                    onClick = {
                        val sessionParam = if (selectedSessionFilter == "Semua Sesi (Matriks)") null else selectedSessionFilter
                        viewModel.exportAndShareExcelReport(
                            context = context,
                            classId = selectedClassId,
                            dateString = selectedDate,
                            sessionName = sessionParam
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("upload_excel_drive_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (selectedSessionFilter == "Semua Sesi (Matriks)") "Export Matriks ke Drive" else "Export Excel ke Drive",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Session Filter Chips Row (Semua Sesi vs Sesi Tertentu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filterOptions = listOf("Semua Sesi (Matriks)") + sessionList.map { it.name }
                filterOptions.forEach { opt ->
                    val isSelected = selectedSessionFilter.equals(opt, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSessionFilter = opt },
                        label = {
                            val iconPrefix = when {
                                opt.contains("Matriks", ignoreCase = true) -> "📊 "
                                opt.contains("Pagi", ignoreCase = true) -> "🌅 "
                                opt.contains("Dzuhur", ignoreCase = true) -> "🕌 "
                                opt.contains("Ashar", ignoreCase = true) -> "🕌 "
                                opt.contains("Pulang", ignoreCase = true) -> "🏠 "
                                else -> "📌 "
                            }
                            Text("$iconPrefix$opt", fontSize = 11.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E3A8A),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Batch Action or Stats Summary
            val isMatrixMode = selectedSessionFilter == "Semua Sesi (Matriks)"
            val activeSessionRecords = remember(attendanceRecords, selectedSessionFilter) {
                if (isMatrixMode) {
                    attendanceRecords
                } else {
                    attendanceRecords.filter { it.sessionName.equals(selectedSessionFilter, ignoreCase = true) }
                }
            }

            val totalStudents = classStudents.size
            val hadirCount = activeSessionRecords.count { it.status == "HADIR" }
            val terlambatCount = activeSessionRecords.count { it.status == "TERLAMBAT" }
            val izinCount = activeSessionRecords.count { it.status == "IZIN" }
            val sakitCount = activeSessionRecords.count { it.status == "SAKIT" }
            val alpaCount = activeSessionRecords.count { it.status == "ALPA" }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatItem(label = "Hadir", count = hadirCount, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                StatItem(label = "Telat", count = terlambatCount, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                StatItem(label = "Izin", count = izinCount, color = Color(0xFF0284C7), modifier = Modifier.weight(1f))
                StatItem(label = "Sakit", count = sakitCount, color = Color(0xFF8B5CF6), modifier = Modifier.weight(1f))
                StatItem(label = "Alpa", count = alpaCount, color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // If a specific session is selected, offer "Tandai Semua Hadir untuk Sesi Ini"
            if (!isMatrixMode) {
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sesi: $selectedSessionFilter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1E40AF)
                        )

                        TextButton(
                            onClick = {
                                viewModel.quickMarkAllClassSession(
                                    classId = selectedClassId,
                                    sessionName = selectedSessionFilter,
                                    status = "HADIR",
                                    date = selectedDate
                                )
                            },
                            modifier = Modifier.testTag("mark_all_session_hadir_button")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tandai Semua Hadir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = if (isMatrixMode)
                    "Daftar Siswa & Matriks Seluruh Sesi (Ketuk badge untuk tandai):"
                else
                    "Daftar Siswa untuk Sesi $selectedSessionFilter (Ketuk untuk ubah status):",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Student Attendance List / Matrix List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(classStudents, key = { it.id }) { student ->
                    val sClass = classes.find { it.id == student.classId }
                        ?: SchoolClass(id = student.classId, name = "-")

                    if (isMatrixMode) {
                        // Matrix row with badges for each session
                        StudentMatrixRow(
                            student = student,
                            className = sClass.name,
                            sessions = sessionList,
                            records = attendanceRecords.filter { it.studentId == student.id },
                            onSessionBadgeClick = { sessionName, currentStatus ->
                                markingTarget = Triple(student, sessionName, currentStatus)
                            }
                        )
                    } else {
                        // Single session detail row
                        val sessionRecord = activeSessionRecords.find { it.studentId == student.id }
                        StudentSingleSessionRow(
                            student = student,
                            className = sClass.name,
                            sessionName = selectedSessionFilter,
                            record = sessionRecord,
                            onSetStatus = { newStatus ->
                                viewModel.markStudentSession(
                                    student = student,
                                    schoolClass = sClass,
                                    sessionName = selectedSessionFilter,
                                    status = newStatus,
                                    date = selectedDate
                                )
                            }
                        )
                    }
                }
            }
        } else {
            // ==================== TAB 2: REKAP PER SISWA ====================
            // Student Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                val studentClass = classes.find { it.id == activeStudent?.classId }
                OutlinedButton(
                    onClick = { showStudentDropdown = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_select_student_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = activeStudent?.name ?: "Pilih Siswa",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "NIS: ${activeStudent?.nis ?: "-"} • ${studentClass?.name ?: "-"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text("Ganti", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                DropdownMenu(
                    expanded = showStudentDropdown,
                    onDismissRequest = { showStudentDropdown = false }
                ) {
                    students.forEach { s ->
                        val sClass = classes.find { it.id == s.classId }
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(s.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("NIS: ${s.nis} • ${sClass?.name ?: "-"}", fontSize = 11.sp, color = Color.Gray)
                                }
                            },
                            onClick = {
                                selectedStudentId = s.id
                                showStudentDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activeStudent != null) {
                val sClass = classes.find { it.id == activeStudent.classId }
                val sClassName = sClass?.name ?: "-"

                val studentHistoryFlow by viewModel.getStudentAttendanceHistory(activeStudent.id).collectAsState(initial = emptyList())
                val displayedHistory = remember(studentHistoryFlow, studentFilterCurrentDateOnly, selectedDate) {
                    if (studentFilterCurrentDateOnly) {
                        studentHistoryFlow.filter { it.dateString == selectedDate }
                    } else {
                        studentHistoryFlow
                    }
                }

                // Student Stats Accumulation
                val sHadir = studentHistoryFlow.count { it.status == "HADIR" }
                val sTelat = studentHistoryFlow.count { it.status == "TERLAMBAT" }
                val sIzin = studentHistoryFlow.count { it.status == "IZIN" }
                val sSakit = studentHistoryFlow.count { it.status == "SAKIT" }
                val sAlpa = studentHistoryFlow.count { it.status == "ALPA" }
                val sTotal = studentHistoryFlow.size
                val sHadirTotal = sHadir + sTelat
                val sPercentage = if (sTotal > 0) (sHadirTotal * 100) / sTotal else 0

                // Student Summary Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeStudent.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "NIS: ${activeStudent.nis}  •  $sClassName",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1E3A8A),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = { viewModel.exportAndShareStudentExcelReport(context, activeStudent) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("export_student_excel_button")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Excel ke Drive", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Mini Stat Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StatItem(label = "Hadir", count = sHadir, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                            StatItem(label = "Telat", count = sTelat, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                            StatItem(label = "Izin", count = sIzin, color = Color(0xFF0284C7), modifier = Modifier.weight(1f))
                            StatItem(label = "Sakit", count = sSakit, color = Color(0xFF8B5CF6), modifier = Modifier.weight(1f))
                            StatItem(label = "Alpa", count = sAlpa, color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter & History Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Absensi (${displayedHistory.size} Log):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Surface(
                        color = if (studentFilterCurrentDateOnly) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { studentFilterCurrentDateOnly = !studentFilterCurrentDateOnly }
                    ) {
                        Text(
                            text = if (studentFilterCurrentDateOnly) "Filter: $selectedDate Saja" else "Semua Tanggal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (studentFilterCurrentDateOnly) MaterialTheme.colorScheme.primary else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (displayedHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat absensi untuk siswa ini pada periode ini.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(displayedHistory, key = { it.id }) { record ->
                            StudentHistoryRecordItem(
                                record = record,
                                onSetStatus = { newStatus ->
                                    val sc = classes.find { it.id == record.classId }
                                        ?: SchoolClass(id = record.classId, name = record.className)
                                    viewModel.markStudentSession(
                                        student = activeStudent,
                                        schoolClass = sc,
                                        sessionName = record.sessionName,
                                        status = newStatus,
                                        date = record.dateString
                                    )
                                },
                                onDelete = { viewModel.deleteAttendanceRecord(record) }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada data siswa.")
                }
            }
        }
    }

    // ==================== QUICK STATUS MARKING DIALOG ====================
    markingTarget?.let { (student, sessionName, currentStatus) ->
        val sClass = classes.find { it.id == student.classId }
            ?: SchoolClass(id = student.classId, name = "Kelas")

        AlertDialog(
            onDismissRequest = { markingTarget = null },
            title = {
                Column {
                    Text("Tandai Presensi Siswa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "${student.name} • Sesi: $sessionName",
                        fontSize = 12.sp,
                        color = Color(0xFF1E3A8A)
                    )
                    Text(
                        text = "Tanggal: $selectedDate",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Pilih status kehadiran:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))

                    val statuses = listOf(
                        Pair("HADIR", Color(0xFF10B981)),
                        Pair("TERLAMBAT", Color(0xFFF59E0B)),
                        Pair("IZIN", Color(0xFF0284C7)),
                        Pair("SAKIT", Color(0xFF8B5CF6)),
                        Pair("ALPA", Color(0xFFEF4444))
                    )

                    statuses.forEach { (statusName, statusColor) ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentStatus.equals(statusName, ignoreCase = true))
                                    statusColor.copy(alpha = 0.2f)
                                else
                                    Color(0xFFF8FAFC)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    viewModel.markStudentSession(
                                        student = student,
                                        schoolClass = sClass,
                                        sessionName = sessionName,
                                        status = statusName,
                                        date = selectedDate
                                    )
                                    markingTarget = null
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = statusColor
                                )
                                if (currentStatus.equals(statusName, ignoreCase = true)) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { markingTarget = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // ==================== MANUAL DATE INPUT DIALOG ====================
    if (showDatePickerDialog) {
        var tempDate by remember { mutableStateOf(selectedDate) }
        AlertDialog(
            onDismissRequest = { showDatePickerDialog = false },
            title = { Text("Pilih Tanggal Presensi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Format tanggal: YYYY-MM-DD",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempDate,
                        onValueChange = { tempDate = it },
                        label = { Text("Tanggal") },
                        placeholder = { Text("2026-10-13") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("date_manual_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempDate.isNotBlank()) {
                            viewModel.setSelectedDate(tempDate.trim())
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ==================== HELPER COMPOSABLES ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentMatrixRow(
    student: Student,
    className: String,
    sessions: List<AttendanceSession>,
    records: List<AttendanceRecord>,
    onSessionBadgeClick: (sessionName: String, currentStatus: String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .testTag("report_matrix_student_${student.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = student.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = student.nis,
                    fontSize = 11.sp,
                    color = Color(0xFF1E3A8A),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Multi-session status pill row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sessions.forEach { sess ->
                    val rec = records.find { it.sessionName.equals(sess.name, ignoreCase = true) }
                    val status = rec?.status ?: "BELUM"

                    val (pillBg, pillText) = when (status) {
                        "HADIR" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
                        "TERLAMBAT" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
                        "IZIN" -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
                        "SAKIT" -> Pair(Color(0xFFF3E8FF), Color(0xFF7E22CE))
                        "ALPA" -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
                        else -> Pair(Color(0xFFF1F5F9), Color(0xFF64748B))
                    }

                    Surface(
                        color = pillBg,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable {
                            onSessionBadgeClick(sess.name, status)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${sess.name}: ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = pillText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentSingleSessionRow(
    student: Student,
    className: String,
    sessionName: String,
    record: AttendanceRecord?,
    onSetStatus: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .testTag("report_session_row_${student.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "NIS: ${student.nis}  •  $className",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                if (record != null) {
                    val (badgeBg, badgeText) = when (record.status) {
                        "HADIR" -> Pair(Color(0xFF10B981), Color.White)
                        "TERLAMBAT" -> Pair(Color(0xFFF59E0B), Color.White)
                        "IZIN" -> Pair(Color(0xFF0284C7), Color.White)
                        "SAKIT" -> Pair(Color(0xFF8B5CF6), Color.White)
                        else -> Pair(Color(0xFFEF4444), Color.White)
                    }

                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { showPicker = true }
                    ) {
                        Text(
                            text = record.status,
                            color = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { showPicker = true }
                    ) {
                        Text(
                            text = "BELUM ABSEN",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showPicker,
                    onDismissRequest = { showPicker = false }
                ) {
                    listOf("HADIR", "TERLAMBAT", "IZIN", "SAKIT", "ALPA").forEach { st ->
                        DropdownMenuItem(
                            text = { Text(st, fontWeight = FontWeight.Bold) },
                            onClick = {
                                onSetStatus(st)
                                showPicker = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentHistoryRecordItem(
    record: AttendanceRecord,
    onSetStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    val (badgeBg, badgeText) = when (record.status) {
        "HADIR" -> Pair(Color(0xFF10B981), Color.White)
        "TERLAMBAT" -> Pair(Color(0xFFF59E0B), Color.White)
        "IZIN" -> Pair(Color(0xFF0284C7), Color.White)
        "SAKIT" -> Pair(Color(0xFF8B5CF6), Color.White)
        else -> Pair(Color(0xFFEF4444), Color.White)
    }

    val timeFormatted = remember(record.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(record.timestamp))
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.dateString,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = record.sessionName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Pukul $timeFormatted • ${record.scanMethod}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Box {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { showPicker = true }
                ) {
                    Text(
                        text = record.status,
                        color = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                DropdownMenu(
                    expanded = showPicker,
                    onDismissRequest = { showPicker = false }
                ) {
                    listOf("HADIR", "TERLAMBAT", "IZIN", "SAKIT", "ALPA").forEach { st ->
                        DropdownMenuItem(
                            text = { Text(st, fontWeight = FontWeight.Bold) },
                            onClick = {
                                onSetStatus(st)
                                showPicker = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

