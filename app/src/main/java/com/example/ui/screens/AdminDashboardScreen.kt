package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Category
import com.example.data.model.AttendanceSession
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.ui.AttendanceViewModel
import com.example.ui.components.BulkImportStudentDialog
import com.example.ui.components.ClassManagementDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.KartuPelajarDialog
import com.example.ui.components.RestoreDatabaseDialog
import com.example.ui.components.SessionManagementDialog
import com.example.ui.components.StudentManagementDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: AttendanceViewModel,
    onNavigateToBarcodeGen: ((Student) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val classes by viewModel.classes.collectAsState()
    val students by viewModel.students.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()
    val driveStatus by viewModel.driveBackupStatus.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Siswa, 1: Kelas, 2: Sesi Presensi, 3: Database & Drive

    // Dialog states
    var editingClass by remember { mutableStateOf<SchoolClass?>(null) }
    var classToDelete by remember { mutableStateOf<SchoolClass?>(null) }
    var showAddClassDialog by remember { mutableStateOf(false) }

    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    var showAddStudentDialog by remember { mutableStateOf(false) }

    var editingSession by remember { mutableStateOf<AttendanceSession?>(null) }
    var sessionToDelete by remember { mutableStateOf<AttendanceSession?>(null) }
    var showAddSessionDialog by remember { mutableStateOf(false) }

    var viewingCardStudent by remember { mutableStateOf<Student?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showBulkImportDialog by remember { mutableStateOf(false) }

    // Search & Filter state for students
    var searchQuery by remember { mutableStateOf("") }
    var filterClassId by remember { mutableStateOf<Long?>(null) }
    var showClassDropdown by remember { mutableStateOf(false) }

    val filteredStudents = remember(students, searchQuery, filterClassId) {
        students.filter { student ->
            val matchQuery = searchQuery.isBlank() ||
                    student.name.contains(searchQuery, ignoreCase = true) ||
                    student.nis.contains(searchQuery, ignoreCase = true)
            val matchClass = filterClassId == null || student.classId == filterClassId
            matchQuery && matchClass
        }
    }

    val selectedFilterClassName = classes.find { it.id == filterClassId }?.name ?: "Semua Kelas"

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
                        text = "Dashboard Admin",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Kelola data siswa, kelas, & database Google Drive",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Stats Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Total Siswa",
                    value = "${students.size}",
                    color = Color(0xFF1E3A8A),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Kelas",
                    value = "${classes.size}",
                    color = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Database",
                    value = "Google Drive",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Drive notification status banner
            AnimatedVisibility(visible = driveStatus != null) {
                driveStatus?.let { status ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = status,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E40AF)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearDriveStatus() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Tutup",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Tabs: Siswa vs Kelas vs Sesi Presensi vs Database & Drive
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Siswa (${students.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    },
                    modifier = Modifier.testTag("admin_tab_students")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Class,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kelas (${classes.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    },
                    modifier = Modifier.testTag("admin_tab_classes")
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sesi Absen (${sessions.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    },
                    modifier = Modifier.testTag("admin_tab_sessions")
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Cloud,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Drive DB", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    },
                    modifier = Modifier.testTag("admin_tab_drive")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTabIndex) {
                0 -> {
                    // STUDENTS TAB
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari nama / NIS...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_students_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            OutlinedButton(
                                onClick = { showClassDropdown = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("admin_class_filter_button")
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(selectedFilterClassName, fontSize = 11.sp)
                            }

                            DropdownMenu(
                                expanded = showClassDropdown,
                                onDismissRequest = { showClassDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Semua Kelas") },
                                    onClick = {
                                        filterClassId = null
                                        showClassDropdown = false
                                    }
                                )
                                classes.forEach { schoolClass ->
                                    DropdownMenuItem(
                                        text = { Text(schoolClass.name) },
                                        onClick = {
                                            filterClassId = schoolClass.id
                                            showClassDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Menampilkan ${filteredStudents.size} siswa",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = { showBulkImportDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_bulk_import_button")
                        ) {
                            Icon(
                                Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload Massal (Excel)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (filteredStudents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Tidak ada siswa cocok pencarian" else "Belum ada siswa terdaftar",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ketuk tombol '+ Siswa' di bawah untuk menambah data siswa",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(filteredStudents, key = { it.id }) { student ->
                                val studentClass = classes.find { it.id == student.classId }
                                StudentAdminCard(
                                    student = student,
                                    className = studentClass?.name ?: "-",
                                    onEdit = { editingStudent = student },
                                    onViewCard = { viewingCardStudent = student },
                                    onDelete = { studentToDelete = student }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                }
                1 -> {
                    // CLASSES TAB
                    if (classes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada kelas terdaftar. Tambahkan kelas baru.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(classes, key = { it.id }) { schoolClass ->
                                val enrolledCount = students.count { it.classId == schoolClass.id }
                                ClassAdminCard(
                                    schoolClass = schoolClass,
                                    studentCount = enrolledCount,
                                    onEdit = { editingClass = schoolClass },
                                    onDelete = { classToDelete = schoolClass }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                }
                2 -> {
                    // SESI PRESENSI TAB
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Kelola Sesi Presensi (${sessions.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Siswa bisa presensi >1x per hari (misal: Pagi, Dzuhur, Ashar, Pulang)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { viewModel.resetSessionsToDefault() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("reset_default_sessions_button")
                            ) {
                                Text("Reset Standar", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(sessions, key = { it.id }) { sessionItem ->
                                SessionAdminCard(
                                    session = sessionItem,
                                    onEdit = { editingSession = sessionItem },
                                    onDelete = { sessionToDelete = sessionItem },
                                    onToggleActive = { isAct ->
                                        viewModel.saveSession(sessionItem.copy(isActive = isAct))
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                }
                3 -> {
                    // DATABASE & GOOGLE DRIVE TAB
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        item {
                            // Google Drive Connection Status Card
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color(0xFFE0F2FE), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.CloudDone,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0284C7),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Google Drive Database",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "Status: Terhubung & Aktif",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF10B981),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        Surface(
                                            color = Color(0xFFDCFCE7),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "OAUTH OK",
                                                color = Color(0xFF15803D),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Cloud Project: gen-lang-client-0243411593\nScope: drive.file (Pencadangan & Penyimpanan Database)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    if (lastBackupTime != null) {
                                        val backupDateStr = SimpleDateFormat(
                                            "dd MMM yyyy, HH:mm",
                                            Locale.getDefault()
                                        ).format(Date(lastBackupTime!!))
                                        Text(
                                            text = "Cadangan Terakhir: $backupDateStr",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1E3A8A)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Database Statistics Summary
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Storage,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ringkasan Data Database",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Kelas Terdaftar:", fontSize = 13.sp, color = Color(0xFF475569))
                                        Text("${classes.size} Kelas", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Siswa Terdaftar:", fontSize = 13.sp, color = Color(0xFF475569))
                                        Text("${students.size} Siswa", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Log Presensi Kehadiran:", fontSize = 13.sp, color = Color(0xFF475569))
                                        Text("${attendanceRecords.size} Catatan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action: Create Database in Drive
                            Button(
                                onClick = {
                                    viewModel.exportAndShareToGoogleDrive(context)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("create_database_in_drive_button")
                            ) {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Simpan Database ke Google Drive",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action: Upload Excel to Google Drive
                            Button(
                                onClick = {
                                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                    viewModel.exportAndShareExcelReport(context, null, today)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("export_excel_drive_admin_button")
                            ) {
                                Icon(
                                    Icons.Default.FileUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Upload Rekap Absensi Excel ke Drive",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action: Restore from Backup
                            OutlinedButton(
                                onClick = { showRestoreDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("restore_database_button")
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pulihkan Database dari Cadangan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        if (selectedTabIndex == 0 || selectedTabIndex == 1 || selectedTabIndex == 2) {
            ExtendedFloatingActionButton(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> showAddStudentDialog = true
                        1 -> showAddClassDialog = true
                        2 -> showAddSessionDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "Tambah Siswa"
                            1 -> "Tambah Kelas"
                            else -> "Tambah Sesi"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("admin_fab_add")
            )
        }
    }

    // Session Dialogs
    if (showAddSessionDialog) {
        SessionManagementDialog(
            initialSession = null,
            onDismiss = { showAddSessionDialog = false },
            onSave = { viewModel.saveSession(it) }
        )
    }

    editingSession?.let { currentSession ->
        SessionManagementDialog(
            initialSession = currentSession,
            onDismiss = { editingSession = null },
            onSave = { viewModel.saveSession(it) }
        )
    }

    sessionToDelete?.let { targetSession ->
        ConfirmDeleteDialog(
            title = "Hapus Sesi '${targetSession.name}'?",
            message = "Apakah Anda yakin ingin menghapus sesi presensi '${targetSession.name}'? Siswa tidak akan melihat opsi ini lagi saat scan.",
            onConfirm = {
                viewModel.deleteSession(targetSession)
                sessionToDelete = null
            },
            onDismiss = { sessionToDelete = null }
        )
    }

    // Class Dialogs
    if (showAddClassDialog) {
        ClassManagementDialog(
            schoolClass = null,
            onDismiss = { showAddClassDialog = false },
            onSave = { viewModel.saveClass(it) }
        )
    }

    editingClass?.let { currentClass ->
        ClassManagementDialog(
            schoolClass = currentClass,
            onDismiss = { editingClass = null },
            onSave = { viewModel.saveClass(it) },
            onDelete = { classToDelete = it }
        )
    }

    classToDelete?.let { targetClass ->
        val countInClass = students.count { it.classId == targetClass.id }
        ConfirmDeleteDialog(
            title = "Hapus Kelas ${targetClass.name}?",
            message = if (countInClass > 0) {
                "Peringatan: Terdapat $countInClass siswa di kelas ini. Menghapus kelas akan menghapus seluruh data siswa di dalamnya!"
            } else {
                "Apakah Anda yakin ingin menghapus kelas '${targetClass.name}'?"
            },
            onConfirm = {
                viewModel.deleteClass(targetClass)
                classToDelete = null
            },
            onDismiss = { classToDelete = null }
        )
    }

    // Student Dialogs
    if (showAddStudentDialog) {
        StudentManagementDialog(
            student = null,
            classes = classes,
            defaultClassId = filterClassId,
            onDismiss = { showAddStudentDialog = false },
            onSave = { viewModel.saveStudent(it) }
        )
    }

    editingStudent?.let { currentStudent ->
        StudentManagementDialog(
            student = currentStudent,
            classes = classes,
            onDismiss = { editingStudent = null },
            onSave = { viewModel.saveStudent(it) },
            onDelete = { studentToDelete = it }
        )
    }

    studentToDelete?.let { targetStudent ->
        ConfirmDeleteDialog(
            title = "Hapus Data Siswa?",
            message = "Apakah Anda yakin ingin menghapus siswa '${targetStudent.name}' (NIS: ${targetStudent.nis})? Tindakan ini tidak dapat dibatalkan.",
            onConfirm = {
                viewModel.deleteStudent(targetStudent)
                studentToDelete = null
            },
            onDismiss = { studentToDelete = null }
        )
    }

    viewingCardStudent?.let { student ->
        val studentClassName = classes.find { it.id == student.classId }?.name ?: "-"
        KartuPelajarDialog(
            student = student,
            className = studentClassName,
            onDismiss = { viewingCardStudent = null }
        )
    }

    if (showRestoreDialog) {
        RestoreDatabaseDialog(
            onDismiss = { showRestoreDialog = false },
            onRestore = { json ->
                viewModel.restoreDatabase(json)
            }
        )
    }

    if (showBulkImportDialog) {
        BulkImportStudentDialog(
            classes = classes,
            defaultClassId = filterClassId,
            onDismiss = { showBulkImportDialog = false },
            onImportStudents = { studentsToImport ->
                viewModel.importStudentsBulk(studentsToImport)
            }
        )
    }
}

@Composable
fun StudentAdminCard(
    student: Student,
    className: String,
    onEdit: () -> Unit,
    onViewCard: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("admin_student_card_${student.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (student.gender == "L") Color(0xFFDBEAFE) else Color(0xFFFCE7F3),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (student.gender == "L") "L" else "P",
                        fontWeight = FontWeight.Bold,
                        color = if (student.gender == "L") Color(0xFF1E3A8A) else Color(0xFFBE185D)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "NIS: ${student.nis}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "•  $className",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (student.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = student.phone,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // View Barcode & Digital Card
                IconButton(
                    onClick = onViewCard,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("view_barcode_student_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Lihat Barcode",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Edit Student
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("edit_student_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Siswa",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Delete Student
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("delete_student_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Siswa",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ClassAdminCard(
    schoolClass: SchoolClass,
    studentCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("admin_class_card_${schoolClass.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFE0F2FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = schoolClass.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tingkat ${schoolClass.gradeLevel}  •  Th. Ajaran ${schoolClass.academicYear}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (schoolClass.homeroomTeacher.isNotBlank()) {
                        Text(
                            text = "Wali Kelas: ${schoolClass.homeroomTeacher}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$studentCount Siswa",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("edit_class_${schoolClass.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Kelas",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("delete_class_${schoolClass.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Kelas",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SessionAdminCard(
    session: AttendanceSession,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    val iconEmoji = when {
        session.name.contains("Pagi", ignoreCase = true) -> "🌅"
        session.name.contains("Dzuhur", ignoreCase = true) -> "🕌"
        session.name.contains("Ashar", ignoreCase = true) -> "🕌"
        session.name.contains("Pulang", ignoreCase = true) -> "🏠"
        session.name.contains("Dhuha", ignoreCase = true) -> "☀️"
        else -> "📌"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("admin_session_card_${session.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFEFF6FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = session.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (session.timeHint.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = session.timeHint,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (session.description.isNotBlank()) session.description else "Sesi Presensi",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = if (session.isActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (session.isActive) "AKTIF DI MENU" else "NONAKTIF",
                            color = if (session.isActive) Color(0xFF15803D) else Color(0xFF64748B),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).testTag("edit_session_${session.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Sesi",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_session_${session.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Sesi",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

