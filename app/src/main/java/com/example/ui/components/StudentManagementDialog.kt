package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudentManagementDialog(
    student: Student?,
    classes: List<SchoolClass>,
    defaultClassId: Long? = null,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit,
    onDelete: ((Student) -> Unit)? = null
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var nis by remember {
        mutableStateOf(
            student?.nis ?: ("2026" + (1000..9999).random().toString())
        )
    }
    var selectedClassId by remember {
        mutableStateOf(
            student?.classId ?: defaultClassId ?: classes.firstOrNull()?.id ?: 1L
        )
    }
    var gender by remember { mutableStateOf(student?.gender ?: "L") }
    var phone by remember { mutableStateOf(student?.phone ?: "") }
    var barcodeType by remember { mutableStateOf(student?.barcodeType ?: "QR_CODE") }
    var errorMessage by remember { mutableStateOf("") }
    var isClassDropdownExpanded by remember { mutableStateOf(false) }

    val currentClassName = classes.find { it.id == selectedClassId }?.name ?: "Pilih Kelas"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (student == null) "Tambah Siswa Baru" else "Edit Siswa",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = ""
                    },
                    label = { Text("Nama Lengkap Siswa *") },
                    placeholder = { Text("Contoh: Aditya Pratama") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nis,
                        onValueChange = {
                            nis = it
                            errorMessage = ""
                        },
                        label = { Text("Nomor Induk Siswa (NIS) *") },
                        placeholder = { Text("Contoh: 20261001") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_nis_input")
                    )

                    IconButton(
                        onClick = {
                            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
                            nis = year + (1000..9999).random().toString()
                        },
                        modifier = Modifier.testTag("generate_random_nis_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Generate Random NIS",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Class Selector Dropdown
                Text(
                    text = "Kelas:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isClassDropdownExpanded = true }
                        .testTag("student_class_selector"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = currentClassName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Kelas") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = isClassDropdownExpanded,
                        onDismissRequest = { isClassDropdownExpanded = false }
                    ) {
                        classes.forEach { schoolClass ->
                            DropdownMenuItem(
                                text = { Text(schoolClass.name) },
                                onClick = {
                                    selectedClassId = schoolClass.id
                                    isClassDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gender Radio Buttons
                Text(
                    text = "Jenis Kelamin:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { gender = "L" }
                    ) {
                        RadioButton(
                            selected = gender == "L",
                            onClick = { gender = "L" }
                        )
                        Text("Laki-laki", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { gender = "P" }
                    ) {
                        RadioButton(
                            selected = gender == "P",
                            onClick = { gender = "P" }
                        )
                        Text("Perempuan", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. HP / WhatsApp (Opsional)") },
                    placeholder = { Text("Contoh: 08123456789") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Preferred Barcode Type
                Text(
                    text = "Format Kode Bawaan:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { barcodeType = "QR_CODE" }
                    ) {
                        RadioButton(
                            selected = barcodeType == "QR_CODE",
                            onClick = { barcodeType = "QR_CODE" }
                        )
                        Text("QR Code (2D)", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { barcodeType = "CODE_128" }
                    ) {
                        RadioButton(
                            selected = barcodeType == "CODE_128",
                            onClick = { barcodeType = "CODE_128" }
                        )
                        Text("Barcode (1D)", fontSize = 13.sp)
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (student != null && onDelete != null) {
                    OutlinedButton(
                        onClick = {
                            onDelete(student)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("delete_student_button")
                    ) {
                        Text("Hapus")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_student_button")
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Nama siswa wajib diisi!"
                            } else if (nis.isBlank()) {
                                errorMessage = "NIS wajib diisi!"
                            } else {
                                onSave(
                                    Student(
                                        id = student?.id ?: 0L,
                                        nis = nis.trim(),
                                        name = name.trim(),
                                        classId = selectedClassId,
                                        gender = gender,
                                        phone = phone.trim(),
                                        barcodeType = barcodeType
                                    )
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("save_student_button")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    )
}
