package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(onNavigateBack: () -> Unit) {
    InfoScreen(
        title = "About Us",
        onNavigateBack = onNavigateBack,
        body = "About Us – Quick Scan Pro\n\nQuick Scan Pro is a modern Indian software brand built under the name “Dhanuk,” created to make everyday digital tasks simpler, faster, and smarter.\n\nFounded by Aasheesh Singh, Dhanuk focuses on building high-quality utility tools that genuinely make life easier for users. What started as a small vision is now evolving into a growing collection of powerful, easy-to-use software for students, professionals, creators, and businesses.\n\nOur focus areas:\n• Lightning-fast document scanning\n• Smart automation tools\n• Everyday productivity apps\n• Secure and privacy-friendly design\n• High-quality features without heavy pricing\n\nWe believe technology should be simple, powerful, and accessible to everyone. Each tool we create is designed with care, real-world testing, and a deep understanding of user needs.\n\nUnder the Dhanuk brand, Quick Scan Pro represents our commitment to building smart and efficient digital solutions for everyone.\n\nOur promise:\nEasy. Fast. Powerful.\nThis is how software should be."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(onNavigateBack: () -> Unit) {
    InfoScreen(
        title = "Contact Us",
        onNavigateBack = onNavigateBack,
        body = "Contact Us – Quick Scan Pro\n\nWe’re always here to help you!\n\nAt Quick Scan Pro (under the brand Dhanuk), your feedback, questions, and suggestions are extremely valuable to us. Whether you need support, want to report an issue, or have ideas to improve our apps, feel free to reach out anytime.\n\n📩 Email Support\nsupport@dhanuksoftwares.com\n\n🛠 For Technical Issues\nTell us your device model, Android version, and a short description of the problem. Our team will respond as quickly as possible.\n\n💡 For Suggestions & Feature Requests\nWe love hearing from our users! Share your ideas and we’ll try our best to include them in future updates.\n\n🤝 Business & Partnership Inquiries\nIf you want to collaborate, integrate our tools, or discuss opportunities, contact us through the same email above with “Business Inquiry” in the subject line.\n\nThank you for choosing Quick Scan Pro.\nWe’re committed to building simple, fast, and powerful tools that improve your everyday digital life."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    InfoScreen(
        title = "Privacy Policy",
        onNavigateBack = onNavigateBack,
        body = "Privacy Policy – Quick Scan Pro (Dhanuk)\n\nQuick Scan Pro (“we”, “our”, “us”), under the brand Dhanuk, is committed to protecting your privacy. This Privacy Policy explains how our app collects, uses, and protects user information.\n\n1. Information We Collect\n• Device information (model, OS version)\n• App usage data for improving performance\n• Camera access (for scanning documents)\n• Storage / Media access (when users import files from the gallery)\n\nWe DO NOT collect:\n• Personal identity data\n• Photos or documents automatically\n• Login information\n• Contacts\n• Background data\n\n2. Third-Party Services (AdMob)\nOur app uses Google AdMob for showing ads.\nAdMob may collect:\n• Advertising ID\n• Approximate location\n• Device & performance info\n\nGoogle Privacy Policy:\nhttps://policies.google.com/privacy\n\n3. How We Use the Information\n• Improve app performance\n• Enable scanning & importing documents\n• Fix bugs and crashes\n• Show relevant ads through AdMob\n\n4. Permissions Explanation\n• Camera: to scan documents\n• Storage / Gallery: to import images or PDF files for scanning\n• Internet: to load ads and app updates\nWe never upload or collect your files without your permission.\n\n5. Children’s Safety\nQuick Scan Pro does not target children under 13.\n\n6. Changes to This Policy\nWe may update this Privacy Policy from time to time.\n\n7. Contact Us\nEmail: support@dhanuksoftwares.com\n\nBy using Quick Scan Pro, you agree to this Privacy Policy."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(onNavigateBack: () -> Unit) {
    InfoScreen(
        title = "Terms & Conditions",
        onNavigateBack = onNavigateBack,
        body = "Terms & Conditions – Quick Scan Pro (Dhanuk)\n\nBy downloading or using Quick Scan Pro, you agree to the following terms:\n\n1. App Usage\nQuick Scan Pro provides document scanning and utility features for personal and professional use. You must use the app legally and responsibly.\n\n2. Intellectual Property\nAll content, design, and software belong to Dhanuk (Developer: Aasheesh Singh). Users may not copy, modify, or distribute the app’s code or assets.\n\n3. User Responsibilities\n• Do not use the app for illegal purposes.\n• Do not attempt to hack, reverse-engineer, or misuse app features.\n\n4. Limitations\nWe are not responsible for:\n• Data loss due to user actions\n• Device malfunctions\n• Third-party ads or content\n\n5. Ads & Third-Party Services\nWe use Google AdMob for showing ads. All data collected by Google follows Google’s Privacy Policy.\n\n6. App Updates\nWe may update or modify the features at any time.\n\n7. Termination\nWe may restrict app use for violations of these terms.\n\n8. Contact\nFor questions: support@dhanuksoftwares.com"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsUsageScreen(onNavigateBack: () -> Unit) {
    InfoScreen(
        title = "Permissions Usage",
        onNavigateBack = onNavigateBack,
        body = "Permissions Used – Quick Scan Pro (Dhanuk)\n\nQuick Scan Pro requires the following permissions:\n\n1. Camera Permission\nUsed only for scanning documents using your device camera.\nWe do NOT upload any images automatically.\n\n2. Storage / Gallery Permission\nUsed for:\n• Importing images or documents from your gallery\n• Saving scanned files\nWe DO NOT access your files automatically. Only files selected by the user are processed.\n\n3. Internet Permission\nUsed for:\n• Showing ads (AdMob)\n• App updates and analytics\n\nWe never collect or store personal files, images, or private data.\nAll files remain on the user’s device."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoScreen(title: String, onNavigateBack: () -> Unit, body: String) {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = title, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
