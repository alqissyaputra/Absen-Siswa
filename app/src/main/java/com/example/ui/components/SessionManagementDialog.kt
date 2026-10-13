package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceSession

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionManagementDialog(
    initialSession: AttendanceSession? = null,
    onDismiss: () -> Unit,
    onSave: (AttendanceSession) -> Unit
) {
    var name by remember { mutableStateOf(initialSession?.name ?: "") }
    var description by remember { mutableStateOf(initialSession?.description ?: "") }
    var timeHint by remember { mutableStateOf(initialSession?.timeHint ?: "") }
    var isActive by remember { mutableStateOf(initialSession?.isActive ?: true) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val presetTemplates = listOf(
        Pair("Kehadiran Pagi", "07:00"),
        Pair("Sholat Dzuhur", "12:15"),
        Pair("Sholat Ashar", "15:30"),
        Pair("Pulang", "16:00"),
        Pair("Sholat Dhuha", "09:30"),
        Pair("Ekstrakurikuler", "16:30"),
        Pair("Upacara Bendera", "07:00"),
        Pair("Kajian / Rohis", "13:00")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialSession == null) "Tambah Sesi Presensi" else "Edit Sesi Presensi",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tentukan nama kegiatan / sesi presensi yang bisa dipilih saat absensi:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Nama Sesi / Kegiatan *") },
                    placeholder = { Text("Contoh: Sholat Dzuhur, Pulang") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_name_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Preset template chips
                Text(
                    text = "Pilihan Cepat Sesi:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetTemplates.forEach { (presetName, presetTime) ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (name == presetName)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clickable {
                                    name = presetName
                                    if (timeHint.isBlank()) timeHint = presetTime
                                    if (description.isBlank()) description = "Presensi $presetName"
                                }
                        ) {
                            Text(
                                text = presetName,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                fontWeight = if (name == presetName) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = timeHint,
                    onValueChange = { timeHint = it },
                    label = { Text("Waktu / Jam Patokan") },
                    placeholder = { Text("Contoh: 12:15 atau 16:00") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_time_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan Tambahan") },
                    placeholder = { Text("Contoh: Presensi Sholat Berjamaah di Masjid") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_desc_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Status Sesi Aktif", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Tampilkan di menu presensi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        modifier = Modifier.testTag("session_active_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Nama sesi tidak boleh kosong"
                        return@Button
                    }
                    val sessionToSave = (initialSession ?: AttendanceSession(name = name)).copy(
                        name = name.trim(),
                        description = description.trim(),
                        timeHint = timeHint.trim(),
                        isActive = isActive
                    )
                    onSave(sessionToSave)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_session_button")
            ) {
                Text("Simpan Sesi")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_session_button")
            ) {
                Text("Batal")
            }
        }
    )
}
