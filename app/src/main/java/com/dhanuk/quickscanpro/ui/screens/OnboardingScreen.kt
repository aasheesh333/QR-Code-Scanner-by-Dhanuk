package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accent: Color
)

private val onboardingPages = listOf(
    OnboardingPage(
        Icons.Filled.QrCodeScanner,
        "Scan Anything",
        "Instantly scan QR codes, barcodes, Wi-Fi networks, contacts, and products.",
        Color(0xFF1F3A8A)
    ),
    OnboardingPage(
        Icons.Filled.VerifiedUser,
        "Stay Safe",
        "Offline link safety scoring checks phishing signals before you open links.",
        Color(0xFF16A34A)
    ),
    OnboardingPage(
        Icons.Filled.AutoAwesome,
        "Smart Actions",
        "Connect to Wi-Fi, save contacts, translate text, set reminders, and secure scans in the vault.",
        Color(0xFF2563EB)
    )
)

/**
 * Clean onboarding with light background, simple icon circles,
 * page dots, and a clear primary CTA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val page = onboardingPages[currentPage]

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(page.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = page.accent,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                onboardingPages.forEachIndexed { index, _ ->
                    val selected = index == currentPage
                    Box(
                        modifier = Modifier
                            .width(if (selected) 24.dp else 8.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                    )
                    if (index < onboardingPages.lastIndex) Spacer(Modifier.width(8.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (currentPage < onboardingPages.lastIndex) currentPage++
                    else onFinished()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (currentPage < onboardingPages.lastIndex) "Next" else "Get Started",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    letterSpacing = 1.sp)
                if (currentPage < onboardingPages.lastIndex) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                }
            }

            TextButton(
                onClick = onFinished,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
