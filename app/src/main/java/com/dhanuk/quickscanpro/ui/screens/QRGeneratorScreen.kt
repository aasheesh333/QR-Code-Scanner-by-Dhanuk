package com.dhanuk.quickscanpro.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.qrgenerator.QRCodeGenerator
import com.dhanuk.quickscanpro.qrgenerator.QRContentBuilder
import com.dhanuk.quickscanpro.viewmodel.QRGeneratorViewModel

@Composable
fun QRGeneratorScreen(onOpenSettings: () -> Unit = {}) {
    val vm: QRGeneratorViewModel = viewModel()
    val context = LocalContext.current
    val selectedType by vm.selectedType.collectAsState()
    val bitmap by vm.generatedBitmap.collectAsState()
    val f1 by vm.f1.collectAsState()
    val f2 by vm.f2.collectAsState()
    val f3 by vm.f3.collectAsState()
    val f4 by vm.f4.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top app bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.QrCodeScanner,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Create QR Code",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(0.dp))

            // ── Type selection card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(20.dp)
            ) {
                Text(
                    "Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                TypeChipRow(selectedType) { vm.setType(it) }
            }

            // ── Input form ──
            DynamicInputForm(selectedType, f1, f2, f3, f4, vm::setF1, vm::setF2, vm::setF3, vm::setF4)

            // ── QR Preview card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    val previewBitmap = bitmap
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "Generated QR",
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
                    } else {
                        Icon(
                            Icons.Filled.QrCode2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Scan to test before saving",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // ── Primary action ──
            Button(
                onClick = { vm.generateFromInputs() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = f1.isNotBlank(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Generate QR Code", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }

            // ── Secondary actions ──
            val qrBitmap = bitmap
            if (qrBitmap != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val saved = QRCodeGenerator.saveToGallery(context, qrBitmap, "qr_${System.currentTimeMillis()}")
                            if (saved) vm.saveCurrentQR(f1)
                            Toast.makeText(context, if (saved) "Saved to gallery" else "Save failed", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Image")
                    }
                    OutlinedButton(
                        onClick = { QRCodeGenerator.shareQrBitmap(context, qrBitmap) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Share")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private data class QRChip(val type: QRContentBuilder.QRType, val label: String, val icon: ImageVector)

private val CHIPS = listOf(
    QRChip(QRContentBuilder.QRType.URL, "URL", Icons.Filled.Link),
    QRChip(QRContentBuilder.QRType.TEXT, "Text", Icons.Filled.TextSnippet),
    QRChip(QRContentBuilder.QRType.WIFI, "WiFi", Icons.Filled.Wifi),
    QRChip(QRContentBuilder.QRType.VCARD, "Contact", Icons.Filled.ContactPage),
    QRChip(QRContentBuilder.QRType.PHONE, "Phone", Icons.Filled.Call),
    QRChip(QRContentBuilder.QRType.EMAIL, "Email", Icons.Filled.Mail),
    QRChip(QRContentBuilder.QRType.SMS, "SMS", Icons.Filled.Sms),
    QRChip(QRContentBuilder.QRType.CALENDAR, "Event", Icons.Filled.CalendarMonth)
)

@Composable
private fun TypeChipRow(selected: QRContentBuilder.QRType, onSelect: (QRContentBuilder.QRType) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(CHIPS) { chip ->
            val isSel = chip.type == selected
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSel) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    .clickable { onSelect(chip.type) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        chip.icon,
                        contentDescription = null,
                        tint = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        chip.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicInputForm(
    type: QRContentBuilder.QRType,
    f1: String, f2: String, f3: String, f4: String,
    setF1: (String) -> Unit, setF2: (String) -> Unit, setF3: (String) -> Unit, setF4: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (type) {
            QRContentBuilder.QRType.URL -> QRField(f1, setF1, "Enter URL", "https://example.com", Icons.Filled.Language)
            QRContentBuilder.QRType.TEXT -> QRField(f1, setF1, "Plain text", "Enter text")
            QRContentBuilder.QRType.PHONE -> QRField(f1, setF1, "Phone number", "+1 555 000 1234", Icons.Filled.Call)
            QRContentBuilder.QRType.SMS -> { QRField(f1, setF1, "Phone number"); QRField(f2, setF2, "Message", singleLine = false) }
            QRContentBuilder.QRType.EMAIL -> { QRField(f1, setF1, "To", leadingIcon = Icons.Filled.Mail); QRField(f2, setF2, "Subject"); QRField(f3, setF3, "Body", singleLine = false) }
            QRContentBuilder.QRType.WIFI -> { QRField(f1, setF1, "SSID", leadingIcon = Icons.Filled.Wifi); QRField(f2, setF2, "Password"); QRField(f3, setF3, "Encryption (WPA/WEP/NOPASS)") }
            QRContentBuilder.QRType.VCARD -> { QRField(f1, setF1, "Full name", leadingIcon = Icons.Filled.ContactPage); QRField(f2, setF2, "Phone"); QRField(f3, setF3, "Email"); QRField(f4, setF4, "Organization") }
            QRContentBuilder.QRType.CALENDAR -> {
                QRField(f1, setF1, "Event title", leadingIcon = Icons.Filled.CalendarMonth)
                QRField(f2, setF2, "Location (optional)")
                QRField(f3, setF3, "Start (YYYYMMDDTHHMMSS)")
                QRField(f4, setF4, "End (YYYYMMDDTHHMMSS)")
            }
        }
    }
}

@Composable
private fun QRField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = RoundedCornerShape(16.dp)
    )
}
