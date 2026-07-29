package com.dhanuk.quickscanpro.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.qrgenerator.QRContentBuilder
import com.dhanuk.quickscanpro.ui.composables.ColorPickerGrid
import com.dhanuk.quickscanpro.ui.composables.GradientButton
import com.dhanuk.quickscanpro.viewmodel.QRGeneratorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRGeneratorScreen() {
    val vm: QRGeneratorViewModel = viewModel()
    val context = LocalContext.current
    val selectedType by vm.selectedType.collectAsState()
    val generatedBitmap by vm.generatedBitmap.collectAsState()
    val generatedContent by vm.generatedContent.collectAsState()
    val fgColor by vm.foregroundColor.collectAsState()
    val bgColor by vm.backgroundColor.collectAsState()

    var input1 by remember(selectedType) { mutableStateOf("") }
    var input2 by remember(selectedType) { mutableStateOf("") }
    var input3 by remember(selectedType) { mutableStateOf("") }
    var input4 by remember(selectedType) { mutableStateOf("") }
    var showColorPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create QR Code", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TypeSelectorChips(
                selectedType = selectedType,
                onTypeSelected = { type ->
                    vm.setType(type)
                }
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (selectedType) {
                        QRContentBuilder.QRType.TEXT -> {
                            InputField(
                                value = input1, onValueChange = { input1 = it },
                                label = "Enter text content",
                                leadingIcon = Icons.Filled.TextFields,
                                minLines = 3, maxLines = 6
                            )
                        }
                        QRContentBuilder.QRType.URL -> {
                            InputField(
                                value = input1, onValueChange = { input1 = it },
                                label = "https://example.com",
                                leadingIcon = Icons.Filled.Link,
                                keyboardType = KeyboardType.Uri
                            )
                        }
                        QRContentBuilder.QRType.WIFI -> {
                            InputField(value = input1, onValueChange = { input1 = it },
                                label = "Network Name (SSID)", leadingIcon = Icons.Filled.Wifi)
                            Spacer(Modifier.height(10.dp))
                            InputField(value = input2, onValueChange = { input2 = it },
                                label = "Password", leadingIcon = Icons.Filled.Lock,
                                keyboardType = KeyboardType.Password)
                            Spacer(Modifier.height(10.dp))
                            InputField(value = input3, onValueChange = { input3 = it },
                                label = "Encryption (WPA/WEP/nopass)", leadingIcon = Icons.Filled.Security)
                        }
                        QRContentBuilder.QRType.VCARD -> {
                            InputField(value = input1, onValueChange = { input1 = it },
                                label = "Full Name", leadingIcon = Icons.Filled.Person)
                            Spacer(Modifier.height(10.dp))
                            InputField(value = input2, onValueChange = { input2 = it },
                                label = "Phone", leadingIcon = Icons.Filled.Phone,
                                keyboardType = KeyboardType.Phone)
                            Spacer(Modifier.height(10.dp))
                            InputField(value = input3, onValueChange = { input3 = it },
                                label = "Email", leadingIcon = Icons.Filled.Email,
                                keyboardType = KeyboardType.Email)
                            Spacer(Modifier.height(10.dp))
                            InputField(value = input4, onValueChange = { input4 = it },
                                label = "Organization", leadingIcon = Icons.Filled.Business)
                        }
                        QRContentBuilder.QRType.EMAIL -> {
                            InputField(value = input1, onValueChange = { input1 = it },
                                label = "To", leadingIcon = Icons.Filled.Email,
                                keyboardType = KeyboardType.Email)
                            Spacer(Modifier.height(10.dp))
                            InputField(value = input2, onValueChange = { input2 = it },
                                label = "Subject")
                            Spacer(Modifier.height(10.dp))
                            InputField(value = input3, onValueChange = { input3 = it },
                                label = "Body", minLines = 2)
                        }
                        QRContentBuilder.QRType.SMS -> {
                            InputField(value = input1, onValueChange = { input1 = it },
                                label = "Phone Number", leadingIcon = Icons.Filled.Sms,
                                keyboardType = KeyboardType.Phone)
                            Spacer(Modifier.height(10.dp))
                            InputField(value = input2, onValueChange = { input2 = it },
                                label = "Message", minLines = 3)
                        }
                        QRContentBuilder.QRType.PHONE -> {
                            InputField(value = input1, onValueChange = { input1 = it },
                                label = "+1 555 123 4567", leadingIcon = Icons.Filled.Phone,
                                keyboardType = KeyboardType.Phone)
                        }
                        QRContentBuilder.QRType.CALENDAR -> {
                            InputField(value = input1, onValueChange = { input1 = it },
                                label = "Event Title", leadingIcon = Icons.Filled.Event)
                            Spacer(Modifier.height(10.dp))
                            InputField(value = input2, onValueChange = { input2 = it },
                                label = "Location", leadingIcon = Icons.Filled.LocationOn)
                            Spacer(Modifier.height(10.dp))
                            InputField(value = input3, onValueChange = { input3 = it },
                                label = "Start (YYYYMMDDTHHMMSS)")
                            Spacer(Modifier.height(10.dp))
                            InputField(value = input4, onValueChange = { input4 = it },
                                label = "End (YYYYMMDDTHHMMSS)")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Colors",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { showColorPicker = !showColorPicker }) {
                    Text(if (showColorPicker) "Hide" else "Customize")
                    Icon(
                        if (showColorPicker) Icons.Filled.ExpandLess
                        else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showColorPicker, enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        ColorPickerGrid(
                            label = "Foreground",
                            selectedColor = Color(fgColor),
                            onColorSelected = { vm.setForeground(it.toArgb()) }
                        )
                        Spacer(Modifier.height(8.dp))
                        ColorPickerGrid(
                            label = "Background",
                            selectedColor = Color(bgColor),
                            onColorSelected = { vm.setBackground(it.toArgb()) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            GradientButton(
                onClick = { vm.generate(input1, input2, input3, input4) },
                modifier = Modifier.fillMaxWidth(),
                text = "Generate QR Code"
            )

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(visible = generatedBitmap != null, enter = fadeIn(), exit = fadeOut()) {
                generatedBitmap?.let { bmp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(260.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                color = Color(bgColor)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Generated QR",
                                        modifier = Modifier.fillMaxSize(0.92f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = generatedContent.take(100) + if (generatedContent.length > 100) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                maxLines = 3
                            )

                            Spacer(Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        vm.saveCurrentQR(selectedType.displayName)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Icon(Icons.Filled.Bookmark, contentDescription = null,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Save")
                                }
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, generatedContent)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share QR Content"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = null,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Share")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TypeSelectorChips(
    selectedType: QRContentBuilder.QRType,
    onTypeSelected: (QRContentBuilder.QRType) -> Unit
) {
    val types = QRContentBuilder.QRType.entries
    val typeIcons = mapOf(
        QRContentBuilder.QRType.TEXT to Icons.Filled.TextFields,
        QRContentBuilder.QRType.URL to Icons.Filled.Link,
        QRContentBuilder.QRType.WIFI to Icons.Filled.Wifi,
        QRContentBuilder.QRType.VCARD to Icons.Filled.ContactPhone,
        QRContentBuilder.QRType.EMAIL to Icons.Filled.Email,
        QRContentBuilder.QRType.SMS to Icons.Filled.Sms,
        QRContentBuilder.QRType.PHONE to Icons.Filled.Phone,
        QRContentBuilder.QRType.CALENDAR to Icons.Filled.Event
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(types) { type ->
            val isSelected = type == selectedType
            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(type) },
                label = { Text(type.displayName, style = MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                   Icon(
                        imageVector = typeIcons[type] ?: Icons.Filled.QrCode2,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp)
    )
}
