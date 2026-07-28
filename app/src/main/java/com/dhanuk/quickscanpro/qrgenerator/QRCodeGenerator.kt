package com.dhanuk.quickscanpro.qrgenerator

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder

object QRCodeGenerator {

    fun generate(
        content: String,
        size: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = mutableMapOf<com.google.zxing.EncodeHintType, Any>(
                com.google.zxing.EncodeHintType.ERROR_CORRECTION to errorCorrectionLevel,
                com.google.zxing.EncodeHintType.CHARACTER_SET to "UTF-8",
                com.google.zxing.EncodeHintType.MARGIN to 2
            )

            val writer = QRCodeWriter()
            val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) foregroundColor else backgroundColor)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }

    fun estimateCapacity(content: String): Boolean {
        // QR code can hold up to 4296 alphanumeric chars (version 40, L correction)
        return content.length <= 3000
    }
}
