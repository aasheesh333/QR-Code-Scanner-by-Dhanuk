package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.ui.composables.AppBackground

private val ABOUT_BODY = """
About Us — QuickScan Pro

QuickScan Pro is a modern Indian software brand built under the name "Dhanuk," created to make everyday digital tasks simpler, faster, and smarter.

Founded by Aasheesh Singh, Dhanuk focuses on building high-quality utility tools that genuinely make life easier for users.

Promise: Easy. Fast. Powerful. This is how software should be.
""".trimIndent()

private val CONTACT_BODY = """
Contact Us — QuickScan Pro

Support email: support@dhanuksoftwares.com
For technical issues: tell us device model, Android version, short description.
For suggestions & feature requests: we love hearing from our users.
For business inquiries: use the same email with "Business Inquiry" subject.
""".trimIndent()

private val PRIVACY_BODY = """
Privacy Policy — QuickScan Pro

We do not collect personal identity data, photos automatically, login info, contacts or background data.

We may use: device info, app usage data, camera (for scanning), gallery access (for imports).

AdMob may collect advertising ID, approximate location, device/performance info — see policies.google.com/privacy.
""".trimIndent()

private val TERMS_BODY = """
Terms & Conditions — QuickScan Pro

Use the app legally and responsibly. Don't reverse-engineer. Don't use for illegal purposes. All content belongs to Dhanuk.

We use Google AdMob for ads. Data follows Google's Privacy Policy.

We may update features any time.
""".trimIndent()

private val PERMISSIONS_BODY = """
Permissions Usage — QuickScan Pro

Camera: only for scanning QR/barcodes — never uploaded automatically.
Storage/Gallery: only for user-selected imports — never accessed automatically.
Internet: for ads, app updates and analytics.
Calendar: optional — only when user imports a scanned calendar event.
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(onNavigateBack: () -> Unit) =
    InfoScreen("About Us", ABOUT_BODY, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(onNavigateBack: () -> Unit) =
    InfoScreen("Contact Us", CONTACT_BODY, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) =
    InfoScreen("Privacy Policy", PRIVACY_BODY, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsUsageScreen(onNavigateBack: () -> Unit) =
    InfoScreen("Permissions", PERMISSIONS_BODY, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(onNavigateBack: () -> Unit) =
    InfoScreen("Terms", TERMS_BODY, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoScreen(title: String, body: String, onNavigateBack: () -> Unit) {
    AppBackground()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }
    }
}
