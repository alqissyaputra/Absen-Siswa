package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import java.util.Locale

@Composable
fun BulkImportStudentDialog(
    classes: List<SchoolClass>,
    defaultClassId: Long? = null,
    onDismiss: () -> Unit,
    onImportStudents: (List<Student>) -> Unit
) {
    var selectedClassId by remember {
        mutableStateOf(defaultClassId ?: classes.firstOrNull()?.id ?: 1L)
    }
    var isClassDropdownExpanded by remember { mutableStateOf(false) }
    var startingNis by remember { mutableStateOf("0000") }
    var inputText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val currentClassName = classes.find { it.id == selectedClassId }?.name ?: "Pilih Kelas"

    // Parse input text lines
    val parsedLines = remember(inputText, startingNis, selectedClassId) {
        val lines = inputText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val startNumber = startingNis.toIntOrNull() ?: 0
        val padLength = startingNis.length.coerceAtLeast(4)

        lines.mapIndexed { index, line ->
            // Check if line contains delimiter (tab, comma, or semicolon) for custom NIS
            val parts = if (line.contains("\t")) {
                line.split("\t")
            } else if (line.contains(";")) {
                line.split(";")
            } else if (line.contains(",")) {
                line.split(",")
            } else {
                listOf()
            }

            if (parts.size >= 2 && parts[0].trim().all { it.isDigit() || it.isLetter() }) {
                val customNis = parts[0].trim()
                val name = parts.drop(1).joinToString(" ").trim()
                Student(
                    nis = customNis,
                    name = name,
                    classId = selectedClassId,
                    gender = "L",
                    barcodeType = "QR_CODE"
                )
            } else {
                val generatedNis = String.format(Locale.US, "%0${padLength}d", startNumber + index)
                Student(
                    nis = generatedNis,
                    name = line.replace("\"", "").trim(),
                    classId = selectedClassId,
                    gender = "L",
                    barcodeType = "QR_CODE"
                )
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Upload / Impor Siswa Massal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Salin dari Excel atau ketik nama siswa baris demi baris. NIS akan diurutkan otomatis:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Select Target Class
                Text(text = "Kelas Tujuan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isClassDropdownExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bulk_target_class_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentClassName, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = isClassDropdownExpanded,
                        onDismissRequest = { isClassDropdownExpanded = false }
                    ) {
                        classes.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedClassId = c.id
                                    isClassDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Starting NIS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = startingNis,
                        onValueChange = { startingNis = it },
                        label = { Text("Mulai NIS Urut") },
                        placeholder = { Text("0000") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("starting_nis_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preset button for XII 1
                OutlinedButton(
                    onClick = {
                        val sampleNames = """
ABID SYAF'IUL HIDAYAT
ABIYYU RACHMAN FACHSAN
ADIB ZADA
AHZA WIZARSYA PRIYAMA
AKHYAR RASYID WIBOWO
ALIF ZHAFRAN FARID
ASYROF ALHATIM
DAFFA ARKANA IBNI
DHIYA' ABD. RAHMAN EL FAIZ
ERLAND ARYASATYA RACHMAN HARAHAP
FADHIL AL ANSORI
FARRAS AHMAD FAIZ
HANIF ABDURRAHMAN
HELMI MUSTAFA ALI
KEANDRE NAUFAL ADIWITYA
M. FATHI ALVARO
MOHAMMAD ROFIQ AL QOWIYY
MUHAMMAD ANAS FIHIFDZILLAH
MUHAMMAD RAYYAN ATHAILLAH
MUHAMMAD SETYA ADINATA
MUHAMMAD ZUHDI FAYYADH
NAWABIL ALNAZURA
RANU HANDIKA
RAZIB ARYAFITRAH
RAZIN ZHAFRAN KHAIRY
SYATHIR ATHALLAH ALVIS
THAQIF ALIY SULTHAN NANOS
FADHLI AKBAR CHANDRA
                        """.trimIndent()
                        inputText = sampleNames
                        startingNis = "0000"
                    },
                    modifier = Modifier.fillMaxWidth().testTag("load_preset_xii1_button")
                ) {
                    Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tempel 28 Siswa Kelas XII 1", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Text field for student names
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        errorMessage = ""
                    },
                    label = { Text("Daftar Nama Siswa (1 per baris)") },
                    placeholder = { Text("Nama Siswa 1\nNama Siswa 2\natau NIS, Nama") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag("bulk_names_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Detection status pill
                Surface(
                    color = if (parsedLines.isNotEmpty()) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (parsedLines.isNotEmpty()) Color(0xFF15803D) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (parsedLines.isNotEmpty()) {
                                "Terdeteksi: ${parsedLines.size} siswa (NIS ${parsedLines.firstOrNull()?.nis ?: ""} s/d ${parsedLines.lastOrNull()?.nis ?: ""})"
                            } else {
                                "Ketik atau tempel nama siswa di atas"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (parsedLines.isNotEmpty()) Color(0xFF166534) else Color(0xFF475569)
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedLines.isEmpty()) {
                        errorMessage = "Belum ada nama siswa yang dimasukkan!"
                    } else {
                        onImportStudents(parsedLines)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("confirm_bulk_import_button")
            ) {
                Text("Impor ${parsedLines.size} Siswa")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_bulk_import_button")
            ) {
                Text("Batal")
            }
        }
    )
}
