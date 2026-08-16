package com.dhanuk.quickscanpro.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.dhanuk.quickscanpro.database.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
                val safeContent = sanitizeCsvField(item.content)
                val safeType = sanitizeCsvField(item.type)
                appendLine("${item.id},$safeContent,$safeType,${item.isFavorite},${item.timestamp}")
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
                    ?: throw IllegalStateException("Could not create the download entry")
                try {
                    val stream = context.contentResolver.openOutputStream(uri)
                        ?: throw IllegalStateException("Could not open the download entry")
                    stream.use { it.write(csvContent.toByteArray()) }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                    uri
                } catch (e: Exception) {
                    // Never leave an invisible IS_PENDING=1 orphan behind in Downloads.
                    runCatching { context.contentResolver.delete(uri, null, null) }
                    throw e
                }
            } else {
                // Write to the app's external files dir (no permission needed) and share via FileProvider.
                val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
                val file = File(dir, fileName)
                FileOutputStream(file).use { it.write(csvContent.toByteArray()) }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
            null
        }
    }

    private fun sanitizeCsvField(field: String): String {
        // Neutralize CSV/formula injection: prefix a leading '=,+,-,@ or tab with an apostrophe.
        val needsGuard = field.isNotEmpty() && field[0] in "=+@-\t\r"
        val body = if (needsGuard) "'$field" else field
        return "\"${body.replace("\"", "\"\"")}\""
    }

    fun shareCsv(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share history"))
        } catch (_: Exception) {
            Toast.makeText(context, "No app to share CSV", Toast.LENGTH_SHORT).show()
        }
    }
}
