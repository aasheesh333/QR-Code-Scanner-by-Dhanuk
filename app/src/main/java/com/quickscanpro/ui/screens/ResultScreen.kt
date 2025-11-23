package com.quickscanpro.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quickscanpro.R
import com.quickscanpro.ads.InterstitialAdManager
import com.quickscanpro.database.ScanResult
import com.quickscanpro.viewmodel.HistoryViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(data: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val viewModel: HistoryViewModel = viewModel()
    val decodedData = URLDecoder.decode(data, StandardCharsets.UTF_8.toString())

    LaunchedEffect(key1 = decodedData) {
        viewModel.insert(ScanResult(content = decodedData))
        InterstitialAdManager.showAd(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Scan Result") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = decodedData)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = {
                    clipboardManager.setText(AnnotatedString(decodedData))
                }) {
                    Text(text = "Copy")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, decodedData)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }) {
                    Text(text = "Share")
                }
            }
            if (isUrl(decodedData)) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(decodedData))
                    context.startActivity(intent)
                }) {
                    Text(text = "Open URL")
                }
            }
        }
    }
}

private fun isUrl(decodedData: String): Boolean {
    return try {
        Uri.parse(decodedData).scheme in listOf("http", "https")
    } catch (e: Exception) {
        false
    }
}
