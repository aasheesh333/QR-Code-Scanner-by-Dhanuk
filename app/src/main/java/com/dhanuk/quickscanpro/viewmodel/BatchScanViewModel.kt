package com.dhanuk.quickscanpro.viewmodel

import androidx.lifecycle.ViewModel
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    fun startBatch() {
        _isActive.value = true
    }

    fun stopBatch() {
        _isActive.value = false
    }

    fun clearAll() {
        _results.value = emptyList()
    }

    fun addResult(content: String): Boolean {
        if (_results.value.any { it.content == content }) return false
        val item = BatchScanItem(
            content = content,
            type = BarcodeTypeDetector.detectType(content)
        )
        _results.value = _results.value + item
        return true
    }

    fun removeAt(index: Int) {
        val list = _results.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _results.value = list
        }
    }

    fun exportAsText(): String {
        return buildString {
            appendLine("QuickScan Pro - Batch Scan Result")
            appendLine("Timestamp: ${System.currentTimeMillis()}")
            appendLine("Total: ${_results.value.size}")
            appendLine("---")
            for (item in _results.value) {
                appendLine("[${item.type.uppercase()}] ${item.content}")
            }
        }
    }

    fun exportAsCsv(): String {
        return buildString {
            appendLine("index,type,content")
            _results.value.forEachIndexed { idx, item ->
                val escaped = "\"${item.content.replace("\"", "\"\"")}\""
                appendLine("${idx + 1},${item.type},$escaped")
            }
        }
    }
}
