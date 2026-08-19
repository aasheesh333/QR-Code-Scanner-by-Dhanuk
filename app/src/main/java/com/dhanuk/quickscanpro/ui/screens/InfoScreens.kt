package com.dhanuk.quickscanpro.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.config.AppConfig
import com.dhanuk.quickscanpro.ui.design.IconBadge
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard

private val CONTACT_BODY = """
Support email: ${AppConfig.SUPPORT_EMAIL}

For technical issues include your device model, Android version and a short description. Feature requests are always welcome. For business inquiries use the subject "Business Inquiry".
""".trimIndent()

private val PRIVACY_BODY = """
QuickScan Pro does not collect your personal identity, photos, login info, contacts or background data. Scans are stored only on your device.

What we may use: camera (scanning only), gallery access (only when you choose an import), device info and app usage data.

AdMob may collect an advertising ID and approximate location — see policies.google.com/privacy. You can opt out via ad personalization settings.
""".trimIndent()

private val TERMS_BODY = """
Use QuickScan Pro legally and responsibly. Do not reverse-engineer or use it for illegal purposes. All content belongs to Dhanuk Softwares.

We use Google AdMob for ads. Ad data follows Google's Privacy Policy. Features may be updated at any time.
""".trimIndent()

private val PERMISSIONS_BODY = """
Camera — only for scanning QR codes & barcodes. Nothing is uploaded.
Storage / Gallery — only for imports you explicitly choose.
Notifications — only for scan reminders you set.
Calendar — only when you import a scanned calendar event.
Location — optionally, to read your current Wi-Fi network name for the Wi-Fi sharing QR.
Internet — for ads, product lookup and updates.
Biometrics — only to unlock your Vault / app lock.
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(onNavigateBack: () -> Unit) =
    LegalInfoScreen("Contact Us", CONTACT_BODY, AppConfig.Legal.CONTACT_US, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) =
    LegalInfoScreen("Privacy Policy", PRIVACY_BODY, AppConfig.Legal.PRIVACY_POLICY, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsUsageScreen(onNavigateBack: () -> Unit) =
    LegalInfoScreen("Permissions", PERMISSIONS_BODY, AppConfig.Legal.PERMISSIONS, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(onNavigateBack: () -> Unit) =
    LegalInfoScreen("Terms of Use", TERMS_BODY, AppConfig.Legal.TERMS, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalInfoScreen(title: String, body: String, url: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val openOnline: () -> Unit = {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            Toast.makeText(context, "No browser available", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = openOnline) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = "Open online")
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            QsCard {
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            QsButton(
                text = "View official page online",
                icon = Icons.Filled.OpenInNew,
                onClick = openOnline
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
