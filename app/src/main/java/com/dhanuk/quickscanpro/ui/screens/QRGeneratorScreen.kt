package com.dhanuk.quickscanpro.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.qrgenerator.QRCodeGenerator
import com.dhanuk.quickscanpro.qrgenerator.QRContentBuilder
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton
import com.dhanuk.quickscanpro.ui.composables.SecondaryButton
import com.dhanuk.quickscanpro.viewmodel.QRGeneratorViewModel

@Composable
fun QRGeneratorScreen() {
    val vm: QRGeneratorViewModel = viewModel()
    val context = LocalContext.current
    val selectedType by vm.selectedType.collectAsState()
    val bitmap by vm.generatedBitmap.collectAsState()
    val f1 by vm.f1.collectAsState()
    val f2 by vm.f2.collectAsState()
    val f3 by vm.f3.collectAsState()
    val f4 by vm.f4.collectAsState()
    val fgColor by vm.foregroundColor.collectAsState()
    val bgColor by vm.backgroundColor.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        GenerateHeader(vm, fgColor, bgColor)
        Column(
            modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TypeChipRow(selectedType) { vm.setType(it) }
            DynamicInputForm(selectedType, f1, f2, f3, f4, vm::setF1, vm::setF2, vm::setF3, vm::setF4)
            QRPreviewBox(bitmap)
            PrimaryButton(text = "Generate QR Code", onClick = { vm.generateFromInputs() }, modifier = Modifier.fillMaxWidth(), enabled = f1.isNotBlank()) {
                Icon(Icons.Filled.AddBox, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generate QR Code")
            }
            if (bitmap != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(text = "Save", onClick = {
                        val saved = QRCodeGenerator.saveToGallery(context, bitmap!!, "qr_${System.currentTimeMillis()}")
                        Toast.makeText(context, if (saved) "Saved to gallery" else "Save failed", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.weight(1f)) { Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Save") }
                    SecondaryButton(text = "Share", onClick = { QRCodeGenerator.shareQrBitmap(context, bitmap!!) }, modifier = Modifier.weight(1f)) { Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Share") }
                }
            }
        }
    }
}

@Composable
private fun GenerateHeader(vm: QRGeneratorViewModel, fgColor: Int, bgColor: Int) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxWidth().statusBarsPadding(), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Text("Generate QR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Box {
                IconButton(onClick = { showSettings = true }) { Icon(Icons.Filled.Settings, contentDescription = "QR Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                DropdownMenu(expanded = showSettings, onDismissRequest = { showSettings = false }) {
                    Text("QR Appearance", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    HorizontalDivider()
                    ColorMenuItem("Foreground", Color(fgColor)) { vm.setForeground(nextColor(fgColor)); showSettings = false }
                    ColorMenuItem("Background", Color(bgColor)) { vm.setBackground(nextColor(bgColor)); showSettings = false }
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Reset Colors") }, onClick = { vm.setForeground(0xFF000000.toInt()); vm.setBackground(0xFFFFFFFF.toInt()); showSettings = false }, leadingIcon = { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp)) })
                }
            }
        }
    }
}

@Composable
private fun ColorMenuItem(label: String, color: Color, onClick: () -> Unit) {
    DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).background(color).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))); Spacer(Modifier.width(12.dp)); Text(label) } }, onClick = onClick)
}

private fun nextColor(current: Int): Int {
    val palette = listOf(0xFF000000.toInt(), 0xFF004AC6.toInt(), 0xFF006C49.toInt(), 0xFF943700.toInt(), 0xFF7C3AED.toInt(), 0xFFDC2626.toInt())
    return palette[(palette.indexOf(current) + 1) % palette.size]
}

private data class QRChip(val type: QRContentBuilder.QRType, val label: String, val icon: ImageVector)

private val CHIPS = listOf(
    QRChip(QRContentBuilder.QRType.URL, "URL", Icons.Filled.Link),
    QRChip(QRContentBuilder.QRType.TEXT, "Text", Icons.Filled.TextSnippet),
    QRChip(QRContentBuilder.QRType.PHONE, "Phone", Icons.Filled.Call),
    QRChip(QRContentBuilder.QRType.SMS, "SMS", Icons.Filled.Sms),
    QRChip(QRContentBuilder.QRType.EMAIL, "Email", Icons.Filled.Mail),
    QRChip(QRContentBuilder.QRType.WIFI, "Wi-Fi", Icons.Filled.Wifi),
    QRChip(QRContentBuilder.QRType.VCARD, "vCard", Icons.Filled.ContactPage)
)

@Composable
private fun TypeChipRow(selected: QRContentBuilder.QRType, onSelect: (QRContentBuilder.QRType) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(CHIPS) { chip ->
            val isSel = chip.type == selected
            Surface(
                modifier = Modifier.height(40.dp).clip(RoundedCornerShape(50)).clickable { onSelect(chip.type) },
                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(50)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(chip.icon, contentDescription = null, tint = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                    Text(chip.label, style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600, letterSpacing = 0.01.sp, lineHeight = 20.sp), color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
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
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (type) {
            QRContentBuilder.QRType.URL -> QRField(f1, setF1, "Website URL", "https://example.com", Icons.Filled.Language, Icons.Filled.QrCode2)
            QRContentBuilder.QRType.TEXT -> QRField(f1, setF1, "Plain text", "Enter text")
            QRContentBuilder.QRType.PHONE -> QRField(f1, setF1, "Phone number", "+1 555 000 1234", Icons.Filled.Call)
            QRContentBuilder.QRType.SMS -> { QRField(f1, setF1, "Phone number"); QRField(f2, setF2, "Message", singleLine = false) }
            QRContentBuilder.QRType.EMAIL -> { QRField(f1, setF1, "To", leadingIcon = Icons.Filled.Mail); QRField(f2, setF2, "Subject"); QRField(f3, setF3, "Body", singleLine = false) }
            QRContentBuilder.QRType.WIFI -> { QRField(f1, setF1, "SSID", leadingIcon = Icons.Filled.Wifi); QRField(f2, setF2, "Password"); QRField(f3, setF3, "Encryption (WPA/WEP/NOPASS)") }
            QRContentBuilder.QRType.VCARD -> { QRField(f1, setF1, "Full name", leadingIcon = Icons.Filled.ContactPage); QRField(f2, setF2, "Phone"); QRField(f3, setF3, "Email"); QRField(f4, setF4, "Organization") }
            QRContentBuilder.QRType.CALENDAR -> Text("Pick another type — calendar is available via the Calendar Import screen", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QRField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String? = null, leadingIcon: ImageVector? = null, trailingIcon: ImageVector? = null, singleLine: Boolean = true) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, placeholder = placeholder?.let { { Text(it) } }, leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } }, trailingIcon = trailingIcon?.let { { Icon(it, contentDescription = null) } }, modifier = Modifier.fillMaxWidth(), singleLine = singleLine, shape = RoundedCornerShape(12.dp))
}

@Composable
private fun QRPreviewBox(bitmap: android.graphics.Bitmap?) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(256.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(12.dp)), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Box(modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest), contentAlignment = Alignment.Center) {
                if (bitmap != null) {
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Generated QR", modifier = Modifier.fillMaxSize().padding(16.dp))
                } else {
                    Icon(Icons.Filled.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Preview updates automatically", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600, letterSpacing = 0.01.sp, lineHeight = 20.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
