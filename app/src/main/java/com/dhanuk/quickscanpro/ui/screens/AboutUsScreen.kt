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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(text = "About Us") },
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
            Text(text = "About Us – Quick Scan Pro\n\nQuick Scan Pro is a modern Indian software brand built under the name “Dhanuk,” created to make everyday digital tasks simpler, faster, and smarter.\n\nFounded by Aasheesh Singh, Dhanuk focuses on building high-quality utility tools that genuinely make life easier for users. What started as a small vision is now evolving into a growing collection of powerful, easy-to-use software for students, professionals, creators, and businesses.\n\nOur focus areas:\n• Lightning-fast document scanning\n• Smart automation tools\n• Everyday productivity apps\n• Secure and privacy-friendly design\n• High-quality features without heavy pricing\n\nWe believe technology should be simple, powerful, and accessible to everyone. Each tool we create is designed with care, real-world testing, and a deep understanding of user needs.\n\nUnder the Dhanuk brand, Quick Scan Pro represents our commitment to building smart and efficient digital solutions for everyone.\n\nOur promise:\nEasy. Fast. Powerful.\nThis is how software should be.")
        }
    }
}
