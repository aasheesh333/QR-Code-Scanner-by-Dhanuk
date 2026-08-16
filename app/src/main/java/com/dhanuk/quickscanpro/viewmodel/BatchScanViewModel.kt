package com.dhanuk.quickscanpro.viewmodel

import androidx.lifecycle.ViewModel
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BatchScanItem(
    val content: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis()
)

class BatchScanViewModel : ViewModel() {

    private val _results = MutableStateFlow<List<BatchScanItem>>(emptyList())
    val results = _results.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    private var _totalScanned = 0
    val totalScanned: Int get() = _totalScanned

    fun startBatch() {
        _isActive.value = true
    }

    fun stopBatch() {
        _isActive.value = false
    }

    fun clearAll() {
        _results.value = emptyList()
        _totalScanned = 0
    }

    fun addResult(content: String): Boolean {
        if (_results.value.any { it.content == content }) return false
        val item = BatchScanItem(
            content = content,
            type = BarcodeTypeDetector.detectType(content)
        )
        _results.value = _results.value + item
        _totalScanned++
        return true
    }

    fun removeAt(index: Int) {
        val list = _results.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _results.value = list
        }
    }

    /** Removes by item identity so an in-flight scan can't shift the target. */
    fun remove(item: BatchScanItem) {
        _results.value = _results.value.filterNot {
            it.content == item.content && it.timestamp == item.timestamp
        }
    }

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun exportAsText(): String {
        return buildString {
            appendLine("QuickScan Pro - Batch Scan Results")
            appendLine("Date: ${dateFmt.format(Date())}")
            appendLine("Total Items: ${_results.value.size}")
            appendLine("---")
            _results.value.forEachIndexed { idx, item ->
                appendLine("${idx + 1}. [${item.type.uppercase()}] ${item.content}")
            }
        }
    }

    fun exportAsCsv(): String {
        return buildString {
            appendLine("Index,Type,Content,Timestamp")
            _results.value.forEachIndexed { idx, item ->
                val escaped = "\"${item.content.replace("\"", "\"\"")}\""
                appendLine("${idx + 1},${item.type},$escaped,${dateFmt.format(Date(item.timestamp))}")
            }
        }
    }

    fun exportAsJson(): String {
        return buildString {
            appendLine("{")
            appendLine("  \"app\": \"QuickScan Pro\",")
            appendLine("  \"date\": \"${dateFmt.format(Date())}\",")
            appendLine("  \"totalItems\": ${_results.value.size},")
            appendLine("  \"results\": [")
            _results.value.forEachIndexed { idx, item ->
                append("    {\"index\": ${idx + 1}, \"type\": \"${item.type}\", \"content\": \"")
                append(item.content.replace("\\", "\\\\").replace("\"", "\\\""))
                append("\", \"timestamp\": \"${dateFmt.format(Date(item.timestamp))}\"}")
                if (idx < _results.value.lastIndex) append(",")
                appendLine()
            }
            appendLine("  ]")
            append("}")
        }
    }

    fun getPdfLines(): List<com.dhanuk.quickscanpro.util.PdfExporter.PdfLine> {
        val lines = mutableListOf<com.dhanuk.quickscanpro.util.PdfExporter.PdfLine>()
        lines.add(com.dhanuk.quickscanpro.util.PdfExporter.PdfLine("QuickScan Pro - Batch Scan Report", bold = true, size = 16f))
        lines.add(com.dhanuk.quickscanpro.util.PdfExporter.PdfLine("Date: ${dateFmt.format(Date())}"))
        lines.add(com.dhanuk.quickscanpro.util.PdfExporter.PdfLine("Total Items: ${_results.value.size}"))
        lines.add(com.dhanuk.quickscanpro.util.PdfExporter.PdfLine(""))
        _results.value.forEachIndexed { idx, item ->
            lines.add(com.dhanuk.quickscanpro.util.PdfExporter.PdfLine("Item ${idx + 1}  [${item.type.uppercase()}]", bold = true, size = 12f))
            lines.add(com.dhanuk.quickscanpro.util.PdfExporter.PdfLine(item.content))
            lines.add(com.dhanuk.quickscanpro.util.PdfExporter.PdfLine("Time: ${dateFmt.format(Date(item.timestamp))}", size = 9f))
            lines.add(com.dhanuk.quickscanpro.util.PdfExporter.PdfLine(""))
        }
        return lines
    }
}
