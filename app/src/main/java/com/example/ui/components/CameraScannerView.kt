package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.util.BarcodeAnalyzer
import java.util.concurrent.Executors

@Composable
fun CameraScannerView(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String, String) -> Unit,
    onOpenManualDialog: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {
            val previewView = remember {
                PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            }

            DisposableEffect(lifecycleOwner, lensFacing) {
                val cameraExecutor = Executors.newSingleThreadExecutor()
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(
                                    cameraExecutor,
                                    BarcodeAnalyzer { rawText, format ->
                                        onBarcodeScanned(rawText, format)
                                    }
                                )
                            }

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))

                onDispose {
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        cameraProvider.unbindAll()
                    } catch (_: Exception) {}
                    cameraExecutor.shutdown()
                    camera = null
                }
            }

            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // Laser scan animated overlay
            ScannerOverlay()

            // Top action buttons: Torch & Flip camera
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val newTorch = !isTorchOn
                        camera?.cameraControl?.enableTorch(newTorch)
                        isTorchOn = newTorch
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isTorchOn) Color(0xFFF59E0B) else Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("torch_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Torch"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalIconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("flip_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip Camera"
                    )
                }
            }

            // Overlay hint at bottom
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Arahkan kamera ke Barcode / QR Siswa",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        } else {
            // Camera permission request fallback
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Izin Kamera Diperlukan untuk Scan",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Button(
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("request_camera_permission_button")
                    ) {
                        Text("Izinkan Kamera")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenManualDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.testTag("manual_input_button")
                    ) {
                        Text("Ketik NIS / Tes")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val boxWidth = size.width * 0.75f
        val boxHeight = size.height * 0.75f
        val left = (size.width - boxWidth) / 2f
        val top = (size.height - boxHeight) / 2f
        val right = left + boxWidth
        val bottom = top + boxHeight

        // Corner bracket lines
        val cornerLength = 40f
        val strokeW = 7f
        val bracketColor = Color(0xFF10B981) // Emerald Green

        // Top-Left
        drawLine(bracketColor, Offset(left, top), Offset(left + cornerLength, top), strokeW)
        drawLine(bracketColor, Offset(left, top), Offset(left, top + cornerLength), strokeW)

        // Top-Right
        drawLine(bracketColor, Offset(right, top), Offset(right - cornerLength, top), strokeW)
        drawLine(bracketColor, Offset(right, top), Offset(right, top + cornerLength), strokeW)

        // Bottom-Left
        drawLine(bracketColor, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeW)
        drawLine(bracketColor, Offset(left, bottom), Offset(left, bottom - cornerLength), strokeW)

        // Bottom-Right
        drawLine(bracketColor, Offset(right, bottom), Offset(right - cornerLength, bottom), strokeW)
        drawLine(bracketColor, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeW)

        // Animated red/emerald laser scan line
        val laserY = top + (boxHeight * laserProgress)
        drawLine(
            color = Color(0xFFEF4444),
            start = Offset(left + 15f, laserY),
            end = Offset(right - 15f, laserY),
            strokeWidth = 4f
        )
    }
}
