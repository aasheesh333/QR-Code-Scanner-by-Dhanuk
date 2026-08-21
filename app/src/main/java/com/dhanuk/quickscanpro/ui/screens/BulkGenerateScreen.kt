package com.dhanuk.quickscanpro.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.qrgenerator.QRCodeGenerator
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsOutlinedButton
import com.dhanuk.quickscanpro.ui.design.SectionLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BulkGenerateScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var input by rememberSaveable { mutableStateOf("") }
    val entries = remember { mutableStateListOf<BulkQr>() }
    var processing by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Bulk QR Generator", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(0.dp))

            Text(
                "Paste multiple lines — one QR code is created per line. Ideal for product codes, URLs or ticket IDs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("One item per line") },
                minLines = 4,
                maxLines = 10,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            QsButton(
                text = if (processing) "Generating…" else "Generate all",
                icon = Icons.Filled.QrCode2,
                enabled = input.isNotBlank() && !processing,
                onClick = {
                    val lines = input.lines().map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                    if (lines.size > 50) {
                        Toast.makeText(context, "Max 50 codes at a time", Toast.LENGTH_SHORT).show()
                    } else {
                        processing = true
                        scope.launch {
                            val generated = withContext(Dispatchers.Default) {
                                lines.mapNotNull { line ->
                                    QRCodeGenerator.generate(line, 384)?.let { bmp -> BulkQr(line, bmp) }
                                }
                            }
                            entries.clear()
                            entries.addAll(generated)
                            input = ""
                            processing = false
                            if (generated.isEmpty()) {
                                Toast.makeText(context, "Could not generate codes — check your input", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )

            if (entries.isNotEmpty()) {
                SectionLabel("Generated (${entries.size})")
                entries.toList().forEachIndexed { idx, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        QsCard(modifier = Modifier.weight(1f), contentPadding = 12.dp) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    bitmap = item.bitmap.asImageBitmap(),
                                    contentDescription = "QR for ${item.content}",
                                    modifier = Modifier.size(96.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    item.content,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { share(context, item) }) {
                                Icon(Icons.Filled.IosShare, contentDescription = "Share")
                            }
                            IconButton(onClick = { entries.removeAt(idx) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
                QsOutlinedButton("Clear all", onClick = { entries.clear() })
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

private data class BulkQr(val content: String, val bitmap: Bitmap)

private fun share(context: Context, item: BulkQr) {
    QRCodeGenerator.shareQrBitmap(context, item.bitmap)
}
