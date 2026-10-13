package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RestoreDatabaseDialog(
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit
) {
    var jsonText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = "Pulihkan Database", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tempel teks cadangan database JSON yang telah diunduh dari Google Drive:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = jsonText,
                    onValueChange = {
                        jsonText = it
                        errorMessage = ""
                    },
                    label = { Text("Isi JSON Cadangan") },
                    placeholder = { Text("{\"version\": 1, \"classes\": [...], ...}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("restore_json_input")
                )
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jsonText.isBlank()) {
                        errorMessage = "Teks JSON tidak boleh kosong!"
                    } else {
                        onRestore(jsonText.trim())
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("confirm_restore_button")
            ) {
                Text("Pulihkan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_restore_button")
            ) {
                Text("Batal")
            }
        }
    )
}
