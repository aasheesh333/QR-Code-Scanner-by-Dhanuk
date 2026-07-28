package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.ui.composables.GradientButton
import com.dhanuk.quickscanpro.util.ProductInfo
import com.dhanuk.quickscanpro.util.ProductLookup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductLookupScreen(
    barcode: String,
    onNavigateBack: () -> Unit
) {
    var product by remember { mutableStateOf<ProductInfo?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(barcode) {
        loading = true
        error = null
        val res = ProductLookup.lookup(barcode)
        res.onSuccess { product = it }
        res.onFailure { error = it.message ?: "Unknown error" }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Info") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Barcode: $barcode",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(24.dp))

            if (loading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Looking up product...", style = MaterialTheme.typography.bodyMedium)
            } else if (error != null) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Product not found.\n$barcode does not match a product in the Open Food Facts database.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(24.dp))
                GradientButton(
                    onClick = { onNavigateBack() },
                    text = "Go Back"
                )
            } else if (product != null) {
                val p = product!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = p.name,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = p.brand,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        if (p.quantity.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = p.quantity,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))

                        if (p.categories.isNotEmpty()) {
                            InfoRow(title = "Categories", value = p.categories.joinToString(" • "))
                            Spacer(Modifier.height(8.dp))
                        }
                        if (p.nutriscoreGrade.isNotBlank()) {
                            InfoRow(title = "Nutri-Score", value = p.nutriscoreGrade)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f, false),
            textAlign = TextAlign.End
        )
    }
}

// ProductInfo is declared in util/ProductLookup.kt
