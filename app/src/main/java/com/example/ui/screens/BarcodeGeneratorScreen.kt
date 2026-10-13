package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.AttendanceViewModel
import com.example.ui.components.KartuPelajarDialog
import com.example.util.BarcodeGenerator

@Composable
fun BarcodeGeneratorScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val classes by viewModel.classes.collectAsState()
    val students by viewModel.students.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Siswa, 1: Custom, 2: Batch Kelas

    // Siswa Mode States
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var showStudentDropdown by remember { mutableStateOf(false) }
    var viewingFullIdCard by remember { mutableStateOf(false) }

    LaunchedEffect(students) {
        if (selectedStudent == null && students.isNotEmpty()) {
            selectedStudent = students.first()
        }
    }

    // Custom Mode States
    var customContent by remember { mutableStateOf("20269999") }
    var customBarcodeFormat by remember { mutableStateOf("QR_CODE") } // "QR_CODE" or "CODE_128"

    // Batch Class Mode States
    var batchClassId by remember { mutableStateOf<Long?>(null) }
    var showBatchClassDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(classes) {
        if (batchClassId == null && classes.isNotEmpty()) {
            batchClassId = classes.first().id
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Screen Header
        Text(
            text = "Pembuat Barcode & QR Code",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Generate barcode siswa, custom code, & kartu pelajar digital",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Barcode Siswa", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("barcode_tab_student")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Custom / Bebas", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("barcode_tab_custom")
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("Cetak Sekelas", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("barcode_tab_batch")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTabIndex) {
            0 -> {
                // TAB 1: BARCODE SISWA
                if (students.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada data siswa. Daftarkan di Dashboard Admin.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        item {
                            // Student Selector Dropdown
                            Text(
                                text = "Pilih Siswa:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Box(modifier = Modifier.fillMaxWidth()) {
                                val currentClass = classes.find { it.id == selectedStudent?.classId }
                                OutlinedButton(
                                    onClick = { showStudentDropdown = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("select_student_barcode_button")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedStudent?.let { "${it.name} (${it.nis})" } ?: "Pilih Siswa",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = currentClass?.name ?: "",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
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
                                                    Text(s.name, fontWeight = FontWeight.Bold)
                                                    Text("NIS: ${s.nis} • ${sClass?.name ?: ""}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                            },
                                            onClick = {
                                                selectedStudent = s
                                                showStudentDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            selectedStudent?.let { s ->
                                val sClass = classes.find { it.id == s.classId }
                                val sClassName = sClass?.name ?: "Kelas"

                                val qrBitmap = remember(s.nis) {
                                    BarcodeGenerator.generateQrCode(s.nis, 400, 400)
                                }
                                val bar128Bitmap = remember(s.nis) {
                                    BarcodeGenerator.generateBarcode128(s.nis, 600, 180)
                                }

                                // Interactive Barcode Card
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Student info header
                                        Text(
                                            text = s.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "NIS: ${s.nis}  •  $sClassName",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E3A8A)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // QR Code Preview
                                        Text(
                                            text = "QR CODE 2D",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        qrBitmap?.let {
                                            Image(
                                                bitmap = it.asImageBitmap(),
                                                contentDescription = "QR Code Preview",
                                                modifier = Modifier
                                                    .size(160.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // 1D Barcode Preview
                                        Text(
                                            text = "BARCODE 1D (CODE 128)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        bar128Bitmap?.let {
                                            Image(
                                                bitmap = it.asImageBitmap(),
                                                contentDescription = "Barcode 128 Preview",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(80.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Actions Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(s.nis))
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.testTag("copy_nis_button")
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Salin NIS", fontSize = 12.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    qrBitmap?.let {
                                                        BarcodeGenerator.shareBitmap(
                                                            context,
                                                            it,
                                                            "Barcode Presensi - ${s.name} (${s.nis})"
                                                        )
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.testTag("share_barcode_image_button")
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Bagikan QR", fontSize = 12.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = { viewingFullIdCard = true },
                                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF0284C7)
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("view_full_id_card_button")
                                        ) {
                                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Cetak / Lihat Kartu Pelajar Lengkap")
                                        }
                                    }
                                }

                                if (viewingFullIdCard) {
                                    KartuPelajarDialog(
                                        student = s,
                                        className = sClassName,
                                        onDismiss = { viewingFullIdCard = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // TAB 2: CUSTOM / BEBAS BARCODE GENERATOR
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    item {
                        OutlinedTextField(
                            value = customContent,
                            onValueChange = { customContent = it },
                            label = { Text("Isi Barcode / Teks / NIS") },
                            placeholder = { Text("Ketik apa saja untuk dijadikan barcode...") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_barcode_text_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Barcode Format Radio
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Tipe Format: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { customBarcodeFormat = "QR_CODE" }
                            ) {
                                RadioButton(
                                    selected = customBarcodeFormat == "QR_CODE",
                                    onClick = { customBarcodeFormat = "QR_CODE" }
                                )
                                Text("QR Code 2D", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { customBarcodeFormat = "CODE_128" }
                            ) {
                                RadioButton(
                                    selected = customBarcodeFormat == "CODE_128",
                                    onClick = { customBarcodeFormat = "CODE_128" }
                                )
                                Text("Barcode 1D (Code 128)", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Render Preview
                        val generatedBitmap = remember(customContent, customBarcodeFormat) {
                            if (customBarcodeFormat == "QR_CODE") {
                                BarcodeGenerator.generateQrCode(customContent, 450, 450)
                            } else {
                                BarcodeGenerator.generateBarcode128(customContent, 650, 200)
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (generatedBitmap != null) {
                                    Image(
                                        bitmap = generatedBitmap.asImageBitmap(),
                                        contentDescription = "Custom Barcode",
                                        modifier = if (customBarcodeFormat == "QR_CODE") {
                                            Modifier.size(200.dp)
                                        } else {
                                            Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = customContent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            BarcodeGenerator.shareBitmap(
                                                context,
                                                generatedBitmap,
                                                "Barcode: $customContent"
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("share_custom_barcode_button")
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Bagikan / Simpan Barcode")
                                    }
                                } else {
                                    Text("Masukkan teks di atas untuk men-generate barcode.")
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // TAB 3: BATCH PRINT SATU KELAS
                val currentBatchClass = classes.find { it.id == batchClassId }
                val batchStudents = remember(students, batchClassId) {
                    students.filter { it.classId == batchClassId }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kelas: ${currentBatchClass?.name ?: "Pilih"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Box {
                        OutlinedButton(
                            onClick = { showBatchClassDropdown = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("batch_class_selector_button")
                        ) {
                            Text("Ganti Kelas", fontSize = 11.sp)
                        }

                        DropdownMenu(
                            expanded = showBatchClassDropdown,
                            onDismissRequest = { showBatchClassDropdown = false }
                        ) {
                            classes.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        batchClassId = c.id
                                        showBatchClassDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (batchStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada siswa di kelas ini.")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(batchStudents, key = { it.id }) { s ->
                            val sQr = remember(s.nis) {
                                BarcodeGenerator.generateQrCode(s.nis, 220, 220)
                            }
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                                    .padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = s.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = s.nis,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    sQr?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "QR ${s.name}",
                                            modifier = Modifier.size(90.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
