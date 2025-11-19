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
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Privacy Policy") },
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
            Text(text = "Privacy Policy – Quick Scan Pro (Dhanuk)\n\nQuick Scan Pro (“we”, “our”, “us”), under the brand Dhanuk, is committed to protecting your privacy. This Privacy Policy explains how our app collects, uses, and protects user information.\n\n1. Information We Collect\n• Device information (model, OS version)\n• App usage data for improving performance\n• Camera access (for scanning documents)\n• Storage / Media access (when users import files from the gallery)\n\nWe DO NOT collect:\n• Personal identity data\n• Photos or documents automatically\n• Login information\n• Contacts\n• Background data\n\n2. Third-Party Services (AdMob)\nOur app uses Google AdMob for showing ads.\nAdMob may collect:\n• Advertising ID\n• Approximate location\n• Device & performance info\n\nGoogle Privacy Policy:\nhttps://policies.google.com/privacy\n\n3. How We Use the Information\n• Improve app performance\n• Enable scanning & importing documents\n• Fix bugs and crashes\n• Show relevant ads through AdMob\n\n4. Permissions Explanation\n• Camera: to scan documents\n• Storage / Gallery: to import images or PDF files for scanning\n• Internet: to load ads and app updates\nWe never upload or collect your files without your permission.\n\n5. Children’s Safety\nQuick Scan Pro does not target children under 13.\n\n6. Changes to This Policy\nWe may update this Privacy Policy from time to time.\n\n7. Contact Us\nEmail: Aasheeshkatheriya@gmail.com\n\nBy using Quick Scan Pro, you agree to this Privacy Policy.")
        }
    }
}
