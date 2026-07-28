package com.dhanuk.quickscanpro.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhanuk.quickscanpro.database.AppDatabase
import com.dhanuk.quickscanpro.database.GeneratedQR
import com.dhanuk.quickscanpro.qrgenerator.QRCodeGenerator
import com.dhanuk.quickscanpro.qrgenerator.QRContentBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QRGeneratorViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).generatedQRDao()

    val savedQRs = dao.getAll().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var selectedType = MutableStateFlow(QRContentBuilder.QRType.TEXT)
    var generatedBitmap = MutableStateFlow<Bitmap?>(null)
    var generatedContent = MutableStateFlow("")
    var foregroundColor = MutableStateFlow(0xFF000000.toInt())
    var backgroundColor = MutableStateFlow(0xFFFFFFFF.toInt())

    fun generate(
        input1: String,
        input2: String = "",
        input3: String = "",
        input4: String = ""
    ) {
        val content = when (selectedType.value) {
            QRContentBuilder.QRType.TEXT, QRContentBuilder.QRType.URL -> input1
            QRContentBuilder.QRType.WIFI -> QRContentBuilder.buildWifi(input1, input2, input3.ifBlank { "WPA" })
            QRContentBuilder.QRType.VCARD -> QRContentBuilder.buildVCARD(input1, input2, input3, input4)
            QRContentBuilder.QRType.EMAIL -> QRContentBuilder.buildEmail(input1, input2, input3)
            QRContentBuilder.QRType.SMS -> QRContentBuilder.buildSMS(input1, input2)
            QRContentBuilder.QRType.PHONE -> QRContentBuilder.buildPhone(input1)
            QRContentBuilder.QRType.CALENDAR -> QRContentBuilder.buildCalendar(input1, input2, input3, input4)
        }
        generatedContent.value = content
        generatedBitmap.value = QRCodeGenerator.generate(
            content = content,
            size = 512,
            foregroundColor = foregroundColor.value,
            backgroundColor = backgroundColor.value
        )
    }

    fun saveCurrentQR(label: String) {
        val content = generatedContent.value
        if (content.isBlank()) return
        viewModelScope.launch {
            dao.insert(
                GeneratedQR(
                    content = content,
                    type = selectedType.value.name,
                    displayLabel = label.ifBlank { content.take(30) },
                    foregroundColor = foregroundColor.value.toLong(),
                    backgroundColor = backgroundColor.value.toLong()
                )
            )
        }
    }

    fun setForeground(color: Int) {
        foregroundColor.value = color
    }

    fun setBackground(color: Int) {
        backgroundColor.value = color
    }

    fun deleteById(id: Int) {
        viewModelScope.launch { dao.deleteById(id) }
    }
}
