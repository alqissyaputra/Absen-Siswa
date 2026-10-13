package com.example.util

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import java.util.EnumMap

class BarcodeAnalyzer(
    private val onBarcodeScanned: (String, String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.TRY_HARDER, true)
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }
        setHints(hints)
    }

    private var lastScanTime = 0L

    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        // Throttle scans to avoid duplicate rapid detections within 1.2 seconds
        if (currentTime - lastScanTime < 1200) {
            imageProxy.close()
            return
        }

        val buffer: ByteBuffer = imageProxy.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val width = imageProxy.width
        val height = imageProxy.height

        val source = PlanarYUVLuminanceSource(
            data,
            width,
            height,
            0,
            0,
            width,
            height,
            false
        )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decodeWithState(binaryBitmap)
            lastScanTime = currentTime
            onBarcodeScanned(result.text, result.barcodeFormat.name)
        } catch (_: Exception) {
            // No barcode detected in this frame, try rotated if necessary
            try {
                // Try with rotated source for portrait mode
                val rotatedData = rotateYUV420Degree90(data, width, height)
                val rotatedSource = PlanarYUVLuminanceSource(
                    rotatedData,
                    height,
                    width,
                    0,
                    0,
                    height,
                    width,
                    false
                )
                val rotatedBinaryBitmap = BinaryBitmap(HybridBinarizer(rotatedSource))
                val result = reader.decodeWithState(rotatedBinaryBitmap)
                lastScanTime = currentTime
                onBarcodeScanned(result.text, result.barcodeFormat.name)
            } catch (_: Exception) {
                // Ignore frames with no barcode
            }
        } finally {
            reader.reset()
            imageProxy.close()
        }
    }

    private fun rotateYUV420Degree90(data: ByteArray, imageWidth: Int, imageHeight: Int): ByteArray {
        val yuv = ByteArray(imageWidth * imageHeight)
        var i = 0
        for (x in 0 until imageWidth) {
            for (y in imageHeight - 1 downTo 0) {
                yuv[i] = data[y * imageWidth + x]
                i++
            }
        }
        return yuv
    }
}
