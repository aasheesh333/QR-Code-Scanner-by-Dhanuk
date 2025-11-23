package com.quickscanpro.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.quickscanpro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Terms & Conditions") },
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
                .padding(16.dp)
        ) {
            Text(text = "Terms & Conditions – Quick Scan Pro (Dhanuk)\n\nBy downloading or using Quick Scan Pro, you agree to the following terms:\n\n1. App Usage\nQuick Scan Pro provides document scanning and utility features for personal and professional use. You must use the app legally and responsibly.\n\n2. Intellectual Property\nAll content, design, and software belong to Dhanuk (Developer: Aasheesh Singh). Users may not copy, modify, or distribute the app’s code or assets.\n\n3. User Responsibilities\n• Do not use the app for illegal purposes.\n• Do not attempt to hack, reverse-engineer, or misuse app features.\n\n4. Limitations\nWe are not responsible for:\n• Data loss due to user actions\n• Device malfunctions\n• Third-party ads or content\n\n5. Ads & Third-Party Services\nWe use Google AdMob for showing ads. All data collected by Google follows Google’s Privacy Policy.\n\n6. App Updates\nWe may update or modify the features at any time.\n\n7. Termination\nWe may restrict app use for violations of these terms.\n\n8. Contact\nFor questions: Aasheeshkatheriya@gmail.com")
        }
    }
}
