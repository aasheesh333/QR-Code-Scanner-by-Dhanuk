package com.dhanuk.quickscanpro.ui.screens

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
import com.dhanuk.quickscanpro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(text = "Contact Us") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(text = "Contact Us – Quick Scan Pro\n\nWe’re always here to help you!\n\nAt Quick Scan Pro (under the brand Dhanuk), your feedback, questions, and suggestions are extremely valuable to us. Whether you need support, want to report an issue, or have ideas to improve our apps, feel free to reach out anytime.\n\n📩 Email Support\nsupport@dhanuksoftwares.com\n\n🛠 For Technical Issues\nTell us your device model, Android version, and a short description of the problem. Our team will respond as quickly as possible.\n\n💡 For Suggestions & Feature Requests\nWe love hearing from our users! Share your ideas and we’ll try our best to include them in future updates.\n\n🤝 Business & Partnership Inquiries\nIf you want to collaborate, integrate our tools, or discuss opportunities, contact us through the same email above with “Business Inquiry” in the subject line.\n\nThank you for choosing Quick Scan Pro.\nWe’re committed to building simple, fast, and powerful tools that improve your everyday digital life.")
        }
    }
}
