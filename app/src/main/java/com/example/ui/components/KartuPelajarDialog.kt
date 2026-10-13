package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.util.BarcodeGenerator

@Composable
fun KartuPelajarDialog(
    student: Student,
    className: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var bar128Bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cardBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(student) {
        val qr = BarcodeGenerator.generateQrCode(student.nis, 300, 300)
        val bar128 = BarcodeGenerator.generateBarcode128(student.nis, 500, 150)
        qrBitmap = qr
        bar128Bitmap = bar128

        cardBitmap = BarcodeGenerator.generateStudentIdCard(
            schoolName = "KARTU PRESENSI RESMI",
            studentName = student.name,
            studentNis = student.nis,
            className = className,
            gender = student.gender,
            qrBitmap = qr,
            barcodeBitmap = bar128
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Kartu Pelajar & Barcode", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card View Preview
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Card Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E3A8A))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "KARTU IDENTITAS PRESENSI",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = className,
                                    color = Color(0xFFF59E0B),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Card Body
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "NIS: ${student.nis}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = "Gender: ${if (student.gender == "L") "Laki-laki" else "Perempuan"}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                if (student.phone.isNotBlank()) {
                                    Text(
                                        text = "Telp: ${student.phone}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            qrBitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                )
                            }
                        }

                        // Barcode 1D at bottom
                        bar128Bitmap?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Barcode 128",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Gunakan barcode atau QR Code di atas saat presensi kehadiran.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    cardBitmap?.let {
                        BarcodeGenerator.shareBitmap(
                            context = context,
                            bitmap = it,
                            title = "Kartu Pelajar - ${student.name} (${student.nis})"
                        )
                    }
                },
                modifier = Modifier.testTag("share_id_card_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bagikan / Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_id_card_button")
            ) {
                Text("Tutup")
            }
        }
    )
}
