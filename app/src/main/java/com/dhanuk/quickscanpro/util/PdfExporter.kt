package com.dhanuk.quickscanpro.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    private const val PAGE_WIDTH = 595   // A4 at 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40
    private const val LINE_HEIGHT = 16

    data class PdfLine(val text: String, val bold: Boolean = false, val size: Float = 11f)

    /** Render lines into a real multi-page PDF file; returns the written file. */
    fun writePdf(context: Context, fileName: String, lines: List<PdfLine>): File {
        val doc = PdfDocument()
        val titlePaint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            textSize = 11f
            isAntiAlias = true
        }
        val boldPaint = Paint(bodyPaint).apply { isFakeBoldText = true }

        var pageNum = 1
        var page = doc.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
        )
        var canvas = page.canvas
        var y = MARGIN

        for (line in lines) {
            if (y > PAGE_HEIGHT - MARGIN) {
                doc.finishPage(page)
                pageNum++
                page = doc.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                )
                canvas = page.canvas
                y = MARGIN
            }
            val paint = when {
                line.size >= 15f -> titlePaint
                line.bold -> boldPaint.apply { textSize = line.size }
                else -> bodyPaint.apply { textSize = line.size }
            }
            // Wrap long lines
            var remaining = line.text
            while (remaining.isNotEmpty()) {
                val maxWidth = (PAGE_WIDTH - 2 * MARGIN).toFloat()
                var count = paint.breakText(remaining, true, maxWidth, null)
                if (count <= 0) count = 1
                canvas.drawText(remaining.substring(0, count), MARGIN.toFloat(), y.toFloat(), paint)
                remaining = remaining.substring(count)
                y += LINE_HEIGHT
                if (y > PAGE_HEIGHT - MARGIN && remaining.isNotEmpty()) {
                    doc.finishPage(page)
                    pageNum++
                    page = doc.startPage(
                        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                    )
                    canvas = page.canvas
                    y = MARGIN
                }
            }
        }
        doc.finishPage(page)

        val dir = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), "QuickScanPro")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun timestampedName(base: String): String {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${base}_$ts.pdf"
    }
}
