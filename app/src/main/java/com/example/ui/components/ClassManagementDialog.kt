package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.SchoolClass

@Composable
fun ClassManagementDialog(
    schoolClass: SchoolClass?, // null for create, non-null for edit
    onDismiss: () -> Unit,
    onSave: (SchoolClass) -> Unit,
    onDelete: ((SchoolClass) -> Unit)? = null
) {
    var name by remember { mutableStateOf(schoolClass?.name ?: "") }
    var gradeLevel by remember { mutableStateOf(schoolClass?.gradeLevel ?: "10") }
    var academicYear by remember { mutableStateOf(schoolClass?.academicYear ?: "2026/2027") }
    var homeroomTeacher by remember { mutableStateOf(schoolClass?.homeroomTeacher ?: "") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (schoolClass == null) "Tambah Kelas Baru" else "Edit Kelas",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = ""
                    },
                    label = { Text("Nama Kelas *") },
                    placeholder = { Text("Contoh: X MIPA 1") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gradeLevel,
                        onValueChange = { gradeLevel = it },
                        label = { Text("Tingkat") },
                        placeholder = { Text("10 / 11 / 12") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("class_grade_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text("Th. Ajaran") },
                        placeholder = { Text("2026/2027") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("class_academic_year_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = homeroomTeacher,
                    onValueChange = { homeroomTeacher = it },
                    label = { Text("Wali Kelas (Opsional)") },
                    placeholder = { Text("Contoh: Dra. Siti Rahayu") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_teacher_input")
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
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
                if (schoolClass != null && onDelete != null) {
                    OutlinedButton(
                        onClick = {
                            onDelete(schoolClass)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("delete_class_button")
                    ) {
                        Text("Hapus")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_class_button")
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Nama kelas wajib diisi!"
                            } else {
                                onSave(
                                    SchoolClass(
                                        id = schoolClass?.id ?: 0L,
                                        name = name.trim(),
                                        gradeLevel = gradeLevel.trim(),
                                        academicYear = academicYear.trim(),
                                        homeroomTeacher = homeroomTeacher.trim()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("save_class_button")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    )
}
