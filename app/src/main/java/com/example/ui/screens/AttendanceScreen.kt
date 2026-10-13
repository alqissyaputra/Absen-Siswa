package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.ui.AttendanceViewModel
import com.example.ui.components.CameraScannerView
import com.example.ui.components.ManualScanDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val classes by viewModel.classes.collectAsState()
    val students by viewModel.students.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()
    val selectedSessionName by viewModel.selectedSessionName.collectAsState()
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()
    val selectedClassId by viewModel.selectedClassId.collectAsState()
    val scanDefaultStatus by viewModel.scanDefaultStatus.collectAsState()
    val scanFeedback by viewModel.scanFeedback.collectAsState()

    var showManualDialog by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }
    var filterByCurrentSessionOnly by remember { mutableStateOf(false) }

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
    }

    val selectedClassName = classes.find { it.id == selectedClassId }?.name ?: "Semua Kelas"

    val displayedRecords = remember(attendanceRecords, filterByCurrentSessionOnly, selectedSessionName) {
        if (filterByCurrentSessionOnly) {
            attendanceRecords.filter { it.sessionName.equals(selectedSessionName, ignoreCase = true) }
        } else {
            attendanceRecords
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
                    text = "Presensi Barcode Siswa",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = todayFormatted,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick manual input button
            OutlinedButton(
                onClick = { showManualDialog = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("open_manual_dialog_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tes / Manual", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ==================== SESI PRESENSI SELECTOR ====================
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PILIH SESI PRESENSI:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Aktif: $selectedSessionName",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val sessionList = if (activeSessions.isNotEmpty()) activeSessions else listOf(
                    com.example.data.model.AttendanceSession(name = "Kehadiran Pagi"),
                    com.example.data.model.AttendanceSession(name = "Sholat Dzuhur"),
                    com.example.data.model.AttendanceSession(name = "Sholat Ashar"),
                    com.example.data.model.AttendanceSession(name = "Pulang")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sessionList.forEach { sess ->
                        val isSelected = selectedSessionName.equals(sess.name, ignoreCase = true)
                        Surface(
                            color = if (isSelected) Color(0xFF1E3A8A) else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .clickable { viewModel.setSelectedSessionName(sess.name) }
                                .testTag("session_chip_${sess.name.replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val iconStr = when {
                                    sess.name.contains("Pagi", ignoreCase = true) -> "🌅"
                                    sess.name.contains("Dzuhur", ignoreCase = true) -> "🕌"
                                    sess.name.contains("Ashar", ignoreCase = true) -> "🕌"
                                    sess.name.contains("Pulang", ignoreCase = true) -> "🏠"
                                    sess.name.contains("Dhuha", ignoreCase = true) -> "☀️"
                                    else -> "📌"
                                }
                                Text(iconStr, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = sess.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (sess.timeHint.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${sess.timeHint})",
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color(0xFF93C5FD) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scanner Component
        CameraScannerView(
            onBarcodeScanned = { rawText, _ ->
                viewModel.processBarcodeScan(rawText, "KAMERA_SCAN")
            },
            onOpenManualDialog = { showManualDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Scan Mode Status Selector & Class Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Status:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = scanDefaultStatus == "HADIR",
                    onClick = { viewModel.setScanDefaultStatus("HADIR") },
                    label = { Text("Hadir", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF10B981),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("status_chip_hadir")
                )
                Spacer(modifier = Modifier.width(4.dp))
                FilterChip(
                    selected = scanDefaultStatus == "TERLAMBAT",
                    onClick = { viewModel.setScanDefaultStatus("TERLAMBAT") },
                    label = { Text("Telat", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF59E0B),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("status_chip_terlambat")
                )
            }

            // Filter Class Dropdown
            Box {
                OutlinedButton(
                    onClick = { showClassDropdown = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("filter_class_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(selectedClassName, fontSize = 11.sp)
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
                    classes.forEach { schoolClass ->
                        DropdownMenuItem(
                            text = { Text(schoolClass.name) },
                            onClick = {
                                viewModel.setSelectedClass(schoolClass.id)
                                showClassDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Scan Feedback Notification Banner
        AnimatedVisibility(
            visible = scanFeedback != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            scanFeedback?.let { feedback ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (feedback.isSuccess) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("scan_feedback_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (feedback.isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (feedback.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = feedback.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (feedback.isSuccess) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                                Text(
                                    text = feedback.subtitle,
                                    fontSize = 11.sp,
                                    color = if (feedback.isSuccess) Color(0xFF047857) else Color(0xFFB91C1C)
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.clearScanFeedback() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Recent Scans Feed Header with filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Aktivitas Hari Ini (${displayedRecords.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Surface(
                color = if (filterByCurrentSessionOnly) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.clickable { filterByCurrentSessionOnly = !filterByCurrentSessionOnly }
            ) {
                Text(
                    text = if (filterByCurrentSessionOnly) "Filter: $selectedSessionName" else "Semua Sesi",
                    fontSize = 11.sp,
                    color = if (filterByCurrentSessionOnly) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Records List
        if (displayedRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Belum ada catatan presensi untuk sesi ini.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scan barcode siswa atau gunakan tombol Tes / Manual",
                        fontSize = 11.sp,
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
                items(displayedRecords, key = { it.id }) { record ->
                    AttendanceRecordItem(
                        record = record,
                        onDelete = { viewModel.deleteAttendanceRecord(record) }
                    )
                }
            }
        }
    }

    if (showManualDialog) {
        val filteredStudents = if (selectedClassId != null) {
            students.filter { it.classId == selectedClassId }
        } else {
            students
        }
        ManualScanDialog(
            students = filteredStudents,
            currentSessionName = selectedSessionName,
            sessions = activeSessions,
            onDismiss = { showManualDialog = false },
            onSubmitNis = { nis ->
                viewModel.processBarcodeScan(nis, "MANUAL_INPUT")
            },
            onSelectSession = { sessionName ->
                viewModel.setSelectedSessionName(sessionName)
            }
        )
    }
}

@Composable
fun AttendanceRecordItem(
    record: AttendanceRecord,
    onDelete: () -> Unit
) {
    val timeFormatted = remember(record.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(record.timestamp))
    }

    val (badgeColor, textColor) = when (record.status) {
        "HADIR" -> Pair(Color(0xFF10B981), Color.White)
        "TERLAMBAT" -> Pair(Color(0xFFF59E0B), Color.White)
        "IZIN" -> Pair(Color(0xFF0284C7), Color.White)
        "SAKIT" -> Pair(Color(0xFF8B5CF6), Color.White)
        else -> Pair(Color(0xFFEF4444), Color.White)
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .testTag("attendance_item_${record.id}")
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
                    text = record.studentName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NIS: ${record.studentNis}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•  ${record.className}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Session badge
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = record.sessionName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = record.status,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
