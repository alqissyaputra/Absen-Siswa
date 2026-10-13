package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileOutputStream
import java.util.EnumMap

object BarcodeGenerator {

    /**
     * Generate QR Code Bitmap with customizable size and colors
     */
    fun generateQrCode(
        content: String,
        width: Int = 512,
        height: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
            }
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
            )
            createBitmapFromBitMatrix(bitMatrix, foregroundColor, backgroundColor)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate 1D Barcode (Code 128) Bitmap
     */
    fun generateBarcode128(
        content: String,
        width: Int = 600,
        height: Int = 200,
        includeText: Boolean = true,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 10)
            }
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.CODE_128,
                width,
                height,
                hints
            )
            val barcodeBitmap = createBitmapFromBitMatrix(bitMatrix, foregroundColor, backgroundColor)

            if (includeText) {
                // Add human-readable text below the barcode lines
                addTextToBarcode(barcodeBitmap, content, backgroundColor, foregroundColor)
            } else {
                barcodeBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createBitmapFromBitMatrix(
        matrix: BitMatrix,
        fgColor: Int,
        bgColor: Int
    ): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix.get(x, y)) fgColor else bgColor
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun addTextToBarcode(
        barcodeBitmap: Bitmap,
        text: String,
        bgColor: Int,
        textColor: Int
    ): Bitmap {
        val textHeight = 48
        val combinedHeight = barcodeBitmap.height + textHeight
        val combinedBitmap = Bitmap.createBitmap(barcodeBitmap.width, combinedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combinedBitmap)
        canvas.drawColor(bgColor)
        canvas.drawBitmap(barcodeBitmap, 0f, 0f, null)

        val paint = Paint().apply {
            color = textColor
            textSize = 28f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val xPos = combinedBitmap.width / 2f
        val yPos = barcodeBitmap.height + (textHeight / 2f) + (textBounds.height() / 2f) - 4
        canvas.drawText(text, xPos, yPos, paint)

        return combinedBitmap
    }

    /**
     * Generate an official-looking Student ID Card (Kartu Pelajar) bitmap
     */
    fun generateStudentIdCard(
        schoolName: String = "SMA NEGERI 1 INDONESIA",
        studentName: String,
        studentNis: String,
        className: String,
        gender: String,
        qrBitmap: Bitmap?,
        barcodeBitmap: Bitmap?
    ): Bitmap {
        val width = 800
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        // Header Background
        val headerPaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 110f, headerPaint)

        // Accent gold line
        val accentPaint = Paint().apply {
            color = Color.parseColor("#F59E0B")
            strokeWidth = 6f
        }
        canvas.drawLine(0f, 110f, width.toFloat(), 110f, accentPaint)

        // Header Text
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("KARTU IDENTITAS SISWA / PRESENSI", width / 2f, 50f, titlePaint)

        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#E0E7FF")
            textSize = 20f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(schoolName, width / 2f, 85f, subTitlePaint)

        // Student Info
        val labelPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 22f
            isAntiAlias = true
        }

        val valuePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }

        var startY = 170f
        val lineSpacing = 42f

        canvas.drawText("NAMA", 40f, startY, labelPaint)
        canvas.drawText(": $studentName", 160f, startY, valuePaint)

        startY += lineSpacing
        canvas.drawText("NIS", 40f, startY, labelPaint)
        canvas.drawText(": $studentNis", 160f, startY, valuePaint)

        startY += lineSpacing
        canvas.drawText("KELAS", 40f, startY, labelPaint)
        canvas.drawText(": $className", 160f, startY, valuePaint)

        startY += lineSpacing
        canvas.drawText("GENDER", 40f, startY, labelPaint)
        val genderStr = if (gender == "L") "Laki-laki" else "Perempuan"
        canvas.drawText(": $genderStr", 160f, startY, valuePaint)

        // Draw QR Code on the right side
        if (qrBitmap != null) {
            val qrSize = 180
            val scaledQr = Bitmap.createScaledBitmap(qrBitmap, qrSize, qrSize, true)
            canvas.drawBitmap(scaledQr, width - qrSize - 40f, 140f, null)
        }

        // Draw 1D Barcode on the bottom center
        if (barcodeBitmap != null) {
            val barWidth = 600
            val barHeight = 110
            val scaledBar = Bitmap.createScaledBitmap(barcodeBitmap, barWidth, barHeight, true)
            canvas.drawBitmap(scaledBar, (width - barWidth) / 2f, height - barHeight - 25f, null)
        }

        // Border around card
        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(2f, 2f, width - 2f, height - 2f, borderPaint)

        return bitmap
    }

    /**
     * Share bitmap via Android Intent
     */
    fun shareBitmap(context: Context, bitmap: Bitmap, title: String) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "barcode_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title\nDigenerate oleh Aplikasi Absen Siswa")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Barcode / Kartu"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
