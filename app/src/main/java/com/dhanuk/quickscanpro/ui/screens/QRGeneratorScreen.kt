package com.dhanuk.quickscanpro.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        GenerateHeader()
        Spacer(Modifier.height(16.dp))
        TypeChipRow(selectedType) { vm.setType(it) }
        Spacer(Modifier.height(20.dp))
        DynamicInputForm(selectedType, vm)
        Spacer(Modifier.height(20.dp))
        QRPreviewBox(bitmap)
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = "Generate QR Code",
            onClick = { vm.generateFromInputs() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.AddBox, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Generate QR Code")
        }
    }
}

@Composable
private fun GenerateHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "Generate QR",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

private data class QRChipOption(
    val type: QRContentBuilder.QRType,
    val label: String,
    val icon: ImageVector
)

private val QR_CHIP_OPTIONS: List<QRChipOption> = listOf(
    QRChipOption(QRContentBuilder.QRType.URL, "URL", Icons.Filled.Link),
    QRChipOption(QRContentBuilder.QRType.TEXT, "Text", Icons.Filled.TextSnippet),
    QRChipOption(QRContentBuilder.QRType.PHONE, "Phone", Icons.Filled.Call),
    QRChipOption(QRContentBuilder.QRType.SMS, "SMS", Icons.Filled.Sms),
    QRChipOption(QRContentBuilder.QRType.EMAIL, "Email", Icons.Filled.Mail),
    QRChipOption(QRContentBuilder.QRType.WIFI, "Wi-Fi", Icons.Filled.Wifi),
    QRChipOption(QRContentBuilder.QRType.VCARD, "vCard", Icons.Filled.ContactPage)
)

@Composable
private fun TypeChipRow(selected: QRContentBuilder.QRType, onSelect: (QRContentBuilder.QRType) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(QR_CHIP_OPTIONS) { option ->
            val isSelected = option.type == selected
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onSelect(option.type) },
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicInputForm(type: QRContentBuilder.QRType, vm: QRGeneratorViewModel) {
    Column(Modifier.fillMaxWidth()) {
        when (type) {
            QRContentBuilder.QRType.URL -> OutlinedQRField(
                vm.f1, onValueChange = { vm.f1 = it },
                label = "Website URL",
                placeholder = "https://example.com",
                leadingIcon = Icons.Filled.Language,
                trailingIcon = Icons.Filled.QrCode2
            )
            QRContentBuilder.QRType.TEXT -> OutlinedQRField(
                vm.f1, onValueChange = { vm.f1 = it },
                label = "Plain text",
                placeholder = "Enter text"
            )
            QRContentBuilder.QRType.PHONE -> OutlinedQRField(
                vm.f1, onValueChange = { vm.f1 = it },
                label = "Phone number",
                placeholder = "+1 555 000 1234",
                leadingIcon = Icons.Filled.Call
            )
            QRContentBuilder.QRType.SMS -> {
                OutlinedQRField(vm.f1, onValueChange = { vm.f1 = it }, label = "Phone number")
                Spacer(Modifier.height(10.dp))
                OutlinedQRField(vm.f2, onValueChange = { vm.f2 = it }, label = "Message", singleLine = false)
            }
            QRContentBuilder.QRType.EMAIL -> {
                OutlinedQRField(vm.f1, onValueChange = { vm.f1 = it }, label = "To", leadingIcon = Icons.Filled.Mail)
                Spacer(Modifier.height(8.dp))
                OutlinedQRField(vm.f2, onValueChange = { vm.f2 = it }, label = "Subject")
                Spacer(Modifier.height(8.dp))
                OutlinedQRField(vm.f3, onValueChange = { vm.f3 = it }, label = "Body", singleLine = false)
            }
            QRContentBuilder.QRType.WIFI -> {
                OutlinedQRField(vm.f1, onValueChange = { vm.f1 = it }, label = "SSID / Network name", leadingIcon = Icons.Filled.Wifi)
                Spacer(Modifier.height(8.dp))
                OutlinedQRField(vm.f2, onValueChange = { vm.f2 = it }, label = "Password")
                Spacer(Modifier.height(8.dp))
                OutlinedQRField(vm.f3, onValueChange = { vm.f3 = it }, label = "Encryption (WPA/WEP/NOPASS)")
            }
            QRContentBuilder.QRType.VCARD -> {
                OutlinedQRField(vm.f1, onValueChange = { vm.f1 = it }, label = "Full name", leadingIcon = Icons.Filled.ContactPage)
                Spacer(Modifier.height(8.dp))
                OutlinedQRField(vm.f2, onValueChange = { vm.f2 = it }, label = "Phone")
                Spacer(Modifier.height(8.dp))
                OutlinedQRField(vm.f3, onValueChange = { vm.f3 = it }, label = "Email")
                Spacer(Modifier.height(8.dp))
                OutlinedQRField(vm.f4, onValueChange = { vm.f4 = it }, label = "Organisation")
            }
            QRContentBuilder.QRType.CALENDAR -> Text(
                "Pick another type — calendar QR is available via the Calendar Import screen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OutlinedQRField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = trailingIcon?.let { { Icon(it, contentDescription = null) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun QRPreviewBox(bitmap: Bitmap?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(12.dp)
                ),
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated QR",
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.QrCode2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Preview updates automatically",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


