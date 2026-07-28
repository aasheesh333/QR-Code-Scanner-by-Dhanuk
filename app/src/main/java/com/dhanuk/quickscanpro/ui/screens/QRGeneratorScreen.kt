package com.dhanuk.quickscanpro.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.qrgenerator.QRContentBuilder
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

    var input1 by remember { mutableStateOf("") }
    var input2 by remember { mutableStateOf("") }
    var input3 by remember { mutableStateOf("") }
    var input4 by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    fun resetInputs() {
        input1 = ""; input2 = ""; input3 = ""; input4 = ""
        vm.generatedBitmap.value = null
        vm.generatedContent.value = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Create QR Code") },
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
            // Type selector
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    QRContentBuilder.QRType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                vm.selectedType.value = type
                                expanded = false
                                resetInputs()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedType) {
                        QRContentBuilder.QRType.TEXT -> {
                            OutlinedTextField(
                                value = input1,
                                onValueChange = { input1 = it },
                                label = { Text("Text content") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 6,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        QRContentBuilder.QRType.URL -> {
                            OutlinedTextField(
                                value = input1,
                                onValueChange = { input1 = it },
                                label = { Text("https://example.com") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        QRContentBuilder.QRType.WIFI -> {
                            OutlinedTextField(value = input1, onValueChange = { input1 = it },
                                label = { Text("Network Name (SSID)") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Wifi, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = input2, onValueChange = { input2 = it },
                                label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = input3, onValueChange = { input3 = it },
                                label = { Text("Encryption (WPA/WEP/nopass)") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Security, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp))
                        }
                        QRContentBuilder.QRType.VCARD -> {
                            OutlinedTextField(value = input1, onValueChange = { input1 = it },
                                label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = input2, onValueChange = { input2 = it },
                                label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = input3, onValueChange = { input3 = it },
                                label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = input4, onValueChange = { input4 = it },
                                label = { Text("Organization") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp))
                        }
                        QRContentBuilder.QRType.EMAIL -> {
                            OutlinedTextField(value = input1, onValueChange = { input1 = it },
                                label = { Text("To") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = input2, onValueChange = { input2 = it },
                                label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = input3, onValueChange = { input3 = it },
                                label = { Text("Body") }, modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp))
                        }
                        QRContentBuilder.QRType.SMS -> {
                            OutlinedTextField(value = input1, onValueChange = { input1 = it },
                                label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Sms, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = input2, onValueChange = { input2 = it },
                                label = { Text("Message") }, modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp))
                        }
                        QRContentBuilder.QRType.PHONE -> {
                            OutlinedTextField(value = input1, onValueChange = { input1 = it },
                                label = { Text("+1 555 123 4567") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp))
                        }
                        QRContentBuilder.QRType.CALENDAR -> {
                            OutlinedTextField(value = input1, onValueChange = { input1 = it },
                                label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = input2, onValueChange = { input2 = it },
                                label = { Text("Location") }, modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = input3, onValueChange = { input3 = it },
                                label = { Text("Start (YYYYMMDDTHHMMSS)") }, modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = input4, onValueChange = { input4 = it },
                                label = { Text("End (YYYYMMDDTHHMMSS)") }, modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp))
                        }
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

            generatedBitmap?.let { bmp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Generated QR",
                                modifier = Modifier.fillMaxSize(0.9f)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = generatedContent.take(80) + if (generatedContent.length > 80) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { vm.saveCurrentQR(selectedType.displayName) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Bookmark, contentDescription = null,
                                    modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
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
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null,
                                    modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
