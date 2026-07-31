package com.dhanuk.quickscanpro.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.config.AppConfig
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
    InfoScreen("About Us", ABOUT_BODY, AppConfig.Legal.ABOUT_US, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(onNavigateBack: () -> Unit) =
    InfoScreen("Contact Us", CONTACT_BODY, AppConfig.Legal.CONTACT_US, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) =
    InfoScreen("Privacy Policy", PRIVACY_BODY, AppConfig.Legal.PRIVACY_POLICY, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsUsageScreen(onNavigateBack: () -> Unit) =
    InfoScreen("Permissions", PERMISSIONS_BODY, AppConfig.Legal.PERMISSIONS, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(onNavigateBack: () -> Unit) =
    InfoScreen("Terms", TERMS_BODY, AppConfig.Legal.TERMS, onNavigateBack)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoScreen(title: String, body: String, url: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val openUrl: () -> Unit = {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            Toast.makeText(context, "No browser available", Toast.LENGTH_SHORT).show()
        }
    }
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
                actions = {
                    IconButton(onClick = openUrl) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = "View online")
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
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = openUrl
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "View Official Policy Online",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = url,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}
