package com.dhanuk.quickscanpro.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.dhanuk.quickscanpro.ui.design.IconBadge
import com.dhanuk.quickscanpro.ui.design.PillChip
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsOutlinedButton
import com.dhanuk.quickscanpro.ui.design.SectionLabel
import com.dhanuk.quickscanpro.viewmodel.QRGeneratorViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun QRGeneratorScreen(
    onOpenSettings: () -> Unit = {},
    onOpenBulk: () -> Unit = {},
    onOpenTemplates: () -> Unit = {},
    vm: QRGeneratorViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedType by vm.selectedType.collectAsState()
    val bitmap by vm.generatedBitmap.collectAsState()
    val content by vm.generatedContent.collectAsState()
    val f1 by vm.f1.collectAsState()
    val f2 by vm.f2.collectAsState()
    val f3 by vm.f3.collectAsState()
    val f4 by vm.f4.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Filled.QrCode2, size = 30.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Create QR", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenTemplates) {
                        Icon(Icons.Filled.ViewModule, contentDescription = "Templates")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionLabel("Content type")
            LazyRow(
                contentPadding = PaddingValues(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CHIPS) { chip ->
                    PillChip(
                        label = chip.label,
                        icon = chip.icon,
                        selected = chip.type == selectedType,
                        onClick = { vm.setType(chip.type) }
                    )
                }
            }

            QsCard(contentPadding = 12.dp) {
                DynamicForm(selectedType, f1, f2, f3, f4, vm::setF1, vm::setF2, vm::setF3, vm::setF4)
            }

            QsOutlinedButton(
                text = "Generate many QR codes at once (bulk)",
                icon = Icons.Filled.ViewModule,
                onClick = onOpenBulk
            )

            QsCard(contentPadding = 12.dp) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        val bmp = bitmap
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Generated QR code",
                                modifier = Modifier.fillMaxSize().padding(10.dp)
                            )
                        } else {
                            Icon(
                                Icons.Filled.QrCode2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (content.isBlank()) "Fill the fields, then generate" else "Scan it to test before sharing",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            QsButton(
                text = "Generate QR code",
                enabled = f1.isNotBlank(),
                onClick = { vm.generateFromInputs() }
            )

            val bmp = bitmap
            if (bmp != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QsOutlinedButton(
                        text = "Save",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val saved = QRCodeGenerator.saveToGallery(context, bmp, "qr_${System.currentTimeMillis()}")
                            if (saved) vm.saveCurrentQR(f1)
                            Toast.makeText(context, if (saved) "Saved to gallery" else "Save failed", Toast.LENGTH_SHORT).show()
                        }
                    )
                    QsOutlinedButton(
                        text = "Share",
                        modifier = Modifier.weight(1f),
                        onClick = { QRCodeGenerator.shareQrBitmap(context, bmp) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
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
private fun DynamicForm(
    type: QRContentBuilder.QRType,
    f1: String, f2: String, f3: String, f4: String,
    setF1: (String) -> Unit, setF2: (String) -> Unit, setF3: (String) -> Unit, setF4: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        when (type) {
            QRContentBuilder.QRType.URL -> Field(f1, setF1, "Website URL", "https://example.com", Icons.Filled.Language)
            QRContentBuilder.QRType.TEXT -> Field(f1, setF1, "Plain text", "Your message")
            QRContentBuilder.QRType.PHONE -> Field(f1, setF1, "Phone number", "+91 98765 43210", Icons.Filled.Call)
            QRContentBuilder.QRType.SMS -> {
                Field(f1, setF1, "Phone number", leadingIcon = Icons.Filled.Sms)
                Field(f2, setF2, "Message", singleLine = false)
            }
            QRContentBuilder.QRType.EMAIL -> {
                Field(f1, setF1, "Recipient", leadingIcon = Icons.Filled.Mail)
                Field(f2, setF2, "Subject")
                Field(f3, setF3, "Message body", singleLine = false)
            }
            QRContentBuilder.QRType.WIFI -> {
                Field(f1, setF1, "Network name (SSID)", leadingIcon = Icons.Filled.Wifi)
                Field(f2, setF2, "Password")
                Field(f3, setF3, "Security (WPA / WEP / NOPASS)", "WPA")
            }
            QRContentBuilder.QRType.VCARD -> {
                Field(f1, setF1, "Full name", leadingIcon = Icons.Filled.ContactPage)
                Field(f2, setF2, "Phone")
                Field(f3, setF3, "Email")
                Field(f4, setF4, "Company")
            }
            QRContentBuilder.QRType.CALENDAR -> {
                Field(f1, setF1, "Event title", leadingIcon = Icons.Filled.CalendarMonth)
                Field(f2, setF2, "Location (optional)")
                Field(f3, setF3, "Start · YYYYMMDDTHHMMSS", "20261231T180000")
                Field(f4, setF4, "End · YYYYMMDDTHHMMSS", "20261231T210000")
            }
        }
    }
}

@Composable
private fun Field(
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
        singleLine = singleLine,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
