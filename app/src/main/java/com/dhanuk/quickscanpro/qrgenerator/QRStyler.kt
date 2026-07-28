package com.dhanuk.quickscanpro.qrgenerator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object QRStyler {

    fun addLogo(qrBitmap: Bitmap, logoBitmap: Bitmap, logoSizeRatio: Float = 0.2f): Bitmap {
        val size = qrBitmap.width
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawBitmap(qrBitmap, 0f, 0f, null)

        val logoSize = (size * logoSizeRatio).toInt()
        val x = (size - logoSize) / 2f
        val y = (size - logoSize) / 2f

        val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoSize, logoSize, true)
        canvas.drawBitmap(scaledLogo, x, y, null)

        return output
    }

    fun addPadding(qrBitmap: Bitmap, paddingPx: Int, backgroundColor: Int = Color.WHITE): Bitmap {
        val size = qrBitmap.width
        val padded = Bitmap.createBitmap(size + paddingPx * 2, size + paddingPx * 2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(padded)
        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(qrBitmap, paddingPx.toFloat(), paddingPx.toFloat(), null)
        return padded
    }
}
