package com.dhanuk.quickscanpro.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.dhanuk.quickscanpro.database.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryExporter {

    suspend fun exportAsCsv(context: Context, list: List<ScanResult>): Uri? = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "QuickScanPro_history_$timestamp.csv"
        val csvContent = buildString {
            appendLine("id,content,type,favorite,timestamp")
            for (item in list) {
                val escapedContent = "\"${item.content.replace("\"", "\"\"")}\""
                val safeType = sanitizeCsvField(item.type)
                appendLine("${item.id},$escapedContent,$safeType,${item.isFavorite},${item.timestamp}")
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { os ->
                        os.write(csvContent.toByteArray())
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(it, values, null, null)
                }
                uri
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                dir.mkdirs()
                val file = File(dir, fileName)
                FileOutputStream(file).use { it.write(csvContent.toByteArray()) }
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
            null
        }
    }

    private fun sanitizeCsvField(field: String): String {
        val escaped = "\"${field.replace("\"", "\"\"")}\""
        if (field.isNotEmpty() && field[0] in "=+@-") {
            return "\"'${field.replace("\"", "\"\"")}\""
        }
        return escaped
    }

    fun shareCsv(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(intent, "Share history"))
        } catch (_: Exception) {
            Toast.makeText(context, "No app to share CSV", Toast.LENGTH_SHORT).show()
        }
    }
}
