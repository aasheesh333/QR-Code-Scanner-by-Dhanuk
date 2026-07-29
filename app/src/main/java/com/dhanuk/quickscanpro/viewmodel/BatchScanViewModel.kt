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

    fun exportAsPdf(): String {
        return buildString {
            appendLine("QUICKSCAN PRO - BATCH SCAN REPORT")
            appendLine("Date: ${dateFmt.format(Date())}")
            appendLine("Total Items: ${_results.value.size}")
            appendLine()
            appendLine("=".repeat(60))
            appendLine()
            _results.value.forEachIndexed { idx, item ->
                appendLine("Item ${idx + 1}")
                appendLine("Type: ${item.type.uppercase()}")
                appendLine("Content: ${item.content}")
                appendLine("Time: ${dateFmt.format(Date(item.timestamp))}")
                appendLine("-".repeat(40))
                appendLine()
            }
        }
    }
}
