package com.dhanuk.quickscanpro.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.ui.composables.EmptyState
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VaultScreen(onNavigateBack: () -> Unit) {
    val settingsVm: SettingsViewModel = viewModel()
    val biometricLock by settingsVm.biometricLock.collectAsState()
    val context = LocalContext.current
    var unlocked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(biometricLock) { if (!biometricLock) unlocked = true }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        VaultHeader(onNavigateBack)
        if (!unlocked) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(40.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(40.dp))
                    }
                    Text("Vault is locked", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text("Authenticate to access your vaulted scans", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val activity = context as? FragmentActivity
                    if (activity != null && isBiometricAvailable(context)) {
                        PrimaryButton(text = "Unlock", onClick = { showBiometricPrompt(activity) { if (it) unlocked = true } }, modifier = Modifier.width(200.dp)) { Icon(Icons.Filled.Fingerprint, contentDescription = null); Spacer(Modifier.size(8.dp)); Text("Unlock") }
                    } else {
                        Text("Biometrics unavailable. Disable lock in Settings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            return@Column
        }
        VaultContent()
    }
}

@Composable
private fun VaultHeader(onNavigateBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().statusBarsPadding(), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary) }
            Text("Vault", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        }
    }
}

private fun isBiometricAvailable(context: Context) = BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

private fun showBiometricPrompt(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onResult(true) }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { onResult(false); Toast.makeText(activity, errString, Toast.LENGTH_SHORT).show() }
        override fun onAuthenticationFailed() { Toast.makeText(activity, "Authentication failed", Toast.LENGTH_SHORT).show() }
    }
    BiometricPrompt(activity, executor, callback).authenticate(
        BiometricPrompt.PromptInfo.Builder().setTitle("Unlock Vault").setSubtitle("Authenticate to access vaulted scans").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK).setNegativeButtonText("Cancel").build()
    )
}

@Composable
private fun VaultContent() {
    val vm: HistoryViewModel = viewModel()
    val vault by vm.vaultScans.collectAsState()
    var showFavoritesOnly by rememberSaveable { mutableStateOf(false) }

    if (vault.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) { EmptyState(icon = Icons.Filled.Lock, title = "Vault is empty", subtitle = "Send a scan into the vault from the result screen to hide it behind biometric lock") }
        return
    }
    val filtered = if (showFavoritesOnly) vault.filter { it.isFavorite } else vault
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SegmentChip("All", !showFavoritesOnly, { showFavoritesOnly = false }, Modifier.weight(1f))
            SegmentChip("Favorites", showFavoritesOnly, { showFavoritesOnly = true }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp)) { EmptyState(icon = Icons.Filled.Star, title = "No favorites", subtitle = "Tap the star on a vault entry to pin it here") }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filtered.forEach { scan ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(scan.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(scan.type.uppercase() + " · " + formatVaultTime(scan.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { vm.toggleFavorite(scan) }) {
                                Icon(if (scan.isFavorite) Icons.Filled.Star else Icons.Outlined.Star, contentDescription = "Favorite", tint = if (scan.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SegmentChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(modifier = modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(50)).background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow).border(1.dp, if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50)).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, letterSpacing = 0.01.sp, lineHeight = 20.sp), color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatVaultTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
    val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now - 86400000L))
    val scanDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    return when (scanDay) { today -> "Today, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp)); yesterday -> "Yesterday, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp)); else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp)) }
}
