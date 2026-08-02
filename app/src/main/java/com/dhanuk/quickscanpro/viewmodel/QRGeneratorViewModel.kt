package com.dhanuk.quickscanpro.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhanuk.quickscanpro.database.AppDatabase
import com.dhanuk.quickscanpro.database.GeneratedQR
import com.dhanuk.quickscanpro.qrgenerator.QRCodeGenerator
import com.dhanuk.quickscanpro.qrgenerator.QRContentBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QRGeneratorViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).generatedQRDao()

    val savedQRs = dao.getAll().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedType = MutableStateFlow(QRContentBuilder.QRType.TEXT)
    val selectedType = _selectedType.asStateFlow()

    private val _generatedBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedBitmap = _generatedBitmap.asStateFlow()

    private val _generatedContent = MutableStateFlow("")
    val generatedContent = _generatedContent.asStateFlow()

    private val _foregroundColor = MutableStateFlow(0xFF000000.toInt())
    val foregroundColor = _foregroundColor.asStateFlow()

    private val _backgroundColor = MutableStateFlow(0xFFFFFFFF.toInt())
    val backgroundColor = _backgroundColor.asStateFlow()

    private val _f1 = MutableStateFlow("")
    val f1 = _f1.asStateFlow()
    private val _f2 = MutableStateFlow("")
    val f2 = _f2.asStateFlow()
    private val _f3 = MutableStateFlow("")
    val f3 = _f3.asStateFlow()
    private val _f4 = MutableStateFlow("")
    val f4 = _f4.asStateFlow()

    fun setF1(value: String) { _f1.value = value }
    fun setF2(value: String) { _f2.value = value }
    fun setF3(value: String) { _f3.value = value }
    fun setF4(value: String) { _f4.value = value }

    fun generateFromInputs() {
        generate(_f1.value, _f2.value, _f3.value, _f4.value)
    }

    fun generate(
        input1: String,
        input2: String = "",
        input3: String = "",
        input4: String = ""
    ) {
        val content = when (_selectedType.value) {
            QRContentBuilder.QRType.TEXT -> input1
            QRContentBuilder.QRType.URL -> QRContentBuilder.buildUrl(input1)
            QRContentBuilder.QRType.WIFI -> QRContentBuilder.buildWifi(input1, input2, input3.ifBlank { "WPA" })
            QRContentBuilder.QRType.VCARD -> QRContentBuilder.buildVCARD(input1, input2, input3, input4)
            QRContentBuilder.QRType.EMAIL -> QRContentBuilder.buildEmail(input1, input2, input3)
            QRContentBuilder.QRType.SMS -> QRContentBuilder.buildSMS(input1, input2)
            QRContentBuilder.QRType.PHONE -> QRContentBuilder.buildPhone(input1)
            QRContentBuilder.QRType.CALENDAR -> QRContentBuilder.buildCalendar(input1, input2, input3, input4)
        }
        _generatedContent.value = content
        val fg = _foregroundColor.value
        val bg = _backgroundColor.value
        viewModelScope.launch {
            _generatedBitmap.value = withContext(Dispatchers.Default) {
                QRCodeGenerator.generate(
                    content = content,
                    size = 512,
                    foregroundColor = fg,
                    backgroundColor = bg
                )
            }
        }
    }

    fun saveCurrentQR(label: String) {
        val content = _generatedContent.value
        if (content.isBlank()) return
        viewModelScope.launch {
            dao.insert(
                GeneratedQR(
                    content = content,
                    type = _selectedType.value.name,
                    displayLabel = label.ifBlank { content.take(30) },
                    foregroundColor = _foregroundColor.value.toLong(),
                    backgroundColor = _backgroundColor.value.toLong()
                )
            )
        }
    }

    fun setForeground(color: Int) {
        _foregroundColor.value = color
    }

    fun setBackground(color: Int) {
        _backgroundColor.value = color
    }

    fun setType(type: QRContentBuilder.QRType) {
        _selectedType.value = type
        _generatedBitmap.value = null
        _generatedContent.value = ""
        _f1.value = ""
        _f2.value = ""
        _f3.value = ""
        _f4.value = ""
    }

    fun prefill(type: QRContentBuilder.QRType, p1: String = "", p2: String = "", p3: String = "", p4: String = "") {
        _selectedType.value = type
        _f1.value = p1
        _f2.value = p2
        _f3.value = p3
        _f4.value = p4
        _generatedBitmap.value = null
        _generatedContent.value = ""
    }

    fun deleteById(id: Int) {
        viewModelScope.launch { dao.deleteById(id) }
    }
}
