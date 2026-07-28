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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dhanuk.quickscanpro.ui.theme.GradientEnd
import com.dhanuk.quickscanpro.ui.theme.GradientMid
import com.dhanuk.quickscanpro.ui.theme.GradientStart

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)

private val onboardingPages = listOf(
    OnboardingPage(
        Icons.Filled.QrCodeScanner,
        "Scan Anything",
        "Quickly scan QR codes, barcodes, Wi-Fi networks, contacts, and products with a single tap.",
        GradientStart
    ),
    OnboardingPage(
        Icons.Filled.QrCode2,
        "Create QR Codes",
        "Generate beautiful custom QR codes for URLs, Wi-Fi, contacts, vCards, and more.",
        GradientMid
    ),
    OnboardingPage(
        Icons.Filled.SmartButton,
        "Smart Actions",
        "Instantly add to contacts, connect to Wi-Fi, call, email, or open in maps - all from a single scan.",
        GradientEnd
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val page = onboardingPages[currentPage]

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(page.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = page.color,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(Modifier.height(40.dp))

            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            )

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                onboardingPages.forEachIndexed { index, _ ->
                    val selected = index == currentPage
                    Box(
                        modifier = Modifier
                            .size(if (selected) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) page.color
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                    )
                    if (index < onboardingPages.lastIndex) Spacer(Modifier.width(8.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPage > 0) {
                    TextButton(onClick = { currentPage-- }) {
                        Text("Back")
                    }
                } else {
                    TextButton(onClick = onFinished) {
                        Text("Skip")
                    }
                }

                Button(
                    onClick = {
                        if (currentPage < onboardingPages.lastIndex) currentPage++
                        else onFinished()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = page.color),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (currentPage < onboardingPages.lastIndex) "Next" else "Get Started")
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
