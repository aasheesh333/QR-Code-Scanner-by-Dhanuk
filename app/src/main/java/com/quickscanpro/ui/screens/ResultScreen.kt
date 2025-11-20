package com.quickscanpro.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.quickscanpro.R
import com.quickscanpro.ads.InterstitialAdManager
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(data: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val decodedData = URLDecoder.decode(data, StandardCharsets.UTF_8.toString())

    LaunchedEffect(Unit) {
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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(text = decodedData)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { clipboardManager.setText(AnnotatedString(decodedData)) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Copy")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, decodedData)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Share")
                }
                if (isUrl(decodedData)) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(decodedData))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Open URL")
                    }
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
