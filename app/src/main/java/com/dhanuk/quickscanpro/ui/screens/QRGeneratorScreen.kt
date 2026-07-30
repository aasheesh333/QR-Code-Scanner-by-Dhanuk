package com.dhanuk.quickscanpro.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.qrgenerator.QRContentBuilder
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton
import com.dhanuk.quickscanpro.viewmodel.QRGeneratorViewModel

@Composable
fun QRGeneratorScreen() {
    val vm: QRGeneratorViewModel = viewModel()
    val selectedType by vm.selectedType.collectAsState()
    val bitmap by vm.generatedBitmap.collectAsState()

    AppBackground()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        // Type selector filter chips row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(QRContentBuilder.QRType.entries.toList().filter { it != QRContentBuilder.QRType.CALENDAR }) { type ->
                FilterChip(
                    selected = type == selectedType,
                    onClick = { vm.setType(type) },
                    label = { Text(type.displayName) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Input form
        when (selectedType) {
            QRContentBuilder.QRType.TEXT, QRContentBuilder.QRType.URL -> QRSingleField(vm)
            QRContentBuilder.QRType.PHONE -> QRPhoneField(vm)
            QRContentBuilder.QRType.SMS -> QRSmsField(vm)
            QRContentBuilder.QRType.EMAIL -> QREmailField(vm)
            QRContentBuilder.QRType.WIFI -> QRWifiField(vm)
            QRContentBuilder.QRType.VCARD -> QRVcardField(vm)
            QRContentBuilder.QRType.CALENDAR -> QRSingleFieldCalendar(vm)
        }

        Spacer(Modifier.height(20.dp))

        if (bitmap != null) {
            QRPreview(bitmap = bitmap!!)
        }

        Spacer(Modifier.height(20.dp))

        PrimaryButton(
            text = "Generate QR Code",
            onClick = { vm.generateFromInputs() },
            modifier = Modifier.fillMaxWidth(),
            enabled = true
        )
    }
}

@Composable
private fun QRSingleField(vm: QRGeneratorViewModel) {
    OutlinedTextField(
        value = vm.f1, onValueChange = { vm.f1 = it },
        label = { Text("Content") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = false
    )
}

@Composable
private fun QRPhoneField(vm: QRGeneratorViewModel) {
    OutlinedTextField(
        value = vm.f1, onValueChange = { vm.f1 = it },
        label = { Text("Phone number") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun QRSmsField(vm: QRGeneratorViewModel) {
    Column {
        OutlinedTextField(
            value = vm.f1, onValueChange = { vm.f1 = it },
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = vm.f2, onValueChange = { vm.f2 = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
    }
}

@Composable
private fun QREmailField(vm: QRGeneratorViewModel) {
    Column {
        OutlinedTextField(value = vm.f1, onValueChange = { vm.f1 = it }, label = { Text("To") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = vm.f2, onValueChange = { vm.f2 = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = vm.f3, onValueChange = { vm.f3 = it }, label = { Text("Body") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
    }
}

@Composable
private fun QRWifiField(vm: QRGeneratorViewModel) {
    Column {
        OutlinedTextField(value = vm.f1, onValueChange = { vm.f1 = it }, label = { Text("SSID / Network name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = vm.f2, onValueChange = { vm.f2 = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = vm.f3, onValueChange = { vm.f3 = it }, label = { Text("Encryption (WPA/WEP/NOPASS)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    }
}

@Composable
private fun QRVcardField(vm: QRGeneratorViewModel) {
    Column {
        OutlinedTextField(value = vm.f1, onValueChange = { vm.f1 = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = vm.f2, onValueChange = { vm.f2 = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = vm.f3, onValueChange = { vm.f3 = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = vm.f4, onValueChange = { vm.f4 = it }, label = { Text("Organisation") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    }
}

@Composable
private fun QRSingleFieldCalendar(vm: QRGeneratorViewModel) {
    SQLPlaceholder()
}

@Composable
private fun QRPreview(bitmap: Bitmap) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Generated QR",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SQLPlaceholder() {
    Text("Pick another type — calendar QR is available via the Calendar Import screen", style = MaterialTheme.typography.bodySmall)
}
