package com.dhanuk.quickscanpro.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.ui.design.IconBadge
import com.dhanuk.quickscanpro.ui.design.PillChip
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsEmptyState
import com.dhanuk.quickscanpro.ui.design.QsOutlinedButton
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(onNavigateBack: () -> Unit) {
    val settingsVm: SettingsViewModel = viewModel()
    val lockMode by settingsVm.vaultLockMode.collectAsState()
    val pinSet by settingsVm.vaultPinSet.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Intentionally NOT rememberSaveable: a process-death restore must re-authenticate.
    var unlocked by remember { mutableStateOf(false) }

    var showPinDialog by remember { mutableStateOf(false) }
    var pinEntry by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableStateOf(0) }
    var verifying by remember { mutableStateOf(false) }

    val biometricAvailable = isBiometricAvailable(context)
    val needsBiometric = lockMode == "biometric" && !unlocked
    val needsPin = lockMode == "pin" && !unlocked

    val activity = context as? FragmentActivity

    fun unlockWithPin() {
        if (attempts >= 5) {
            pinError = "Too many attempts. Try again later."
            return
        }
        verifying = true
        scope.launch {
            val ok = settingsVm.verifyVaultPin(pinEntry)
            verifying = false
            if (ok) {
                unlocked = true
                pinError = null
                pinEntry = ""
                attempts = 0
                showPinDialog = false
            } else {
                val failedAttempts = attempts + 1
                attempts = failedAttempts
                pinEntry = ""
                if (failedAttempts >= 5) {
                    pinError = "Too many attempts. Try again in 30 seconds."
                    delay(30_000)
                    attempts = 0
                    pinError = null
                } else {
                    pinError = "Wrong PIN (${5 - failedAttempts} left)"
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Secure Vault", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (!unlocked) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    IconBadge(Icons.Filled.Lock, size = 76.dp)
                    Text("Vault is locked", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Authenticate to view your protected scans.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (lockMode == "biometric" && biometricAvailable && activity != null) {
                        QsButton(
                            text = "Unlock with biometrics",
                            icon = Icons.Filled.Fingerprint,
                            onClick = { showBiometricPrompt(activity) { if (it) unlocked = true } },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        if (pinSet) {
                            QsOutlinedButton(
                                text = "Unlock with PIN",
                                onClick = { showPinDialog = true },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    } else if (needsPin && pinSet) {
                        QsButton(
                            text = "Enter PIN to unlock",
                            onClick = { showPinDialog = true },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    } else {
                        Text(
                            if (lockMode == "biometric")
                                "Biometrics unavailable on this device. Set a vault PIN in Settings, or disable the lock."
                            else if (needsPin)
                                "No vault PIN is configured. Set one in Settings before enabling PIN lock."
                            else
                                "No lock method configured. Set a PIN or biometrics in Settings.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        } else {
            VaultBody(Modifier.padding(padding))
        }
    }

    if ((needsBiometric || needsPin) && showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Enter vault PIN") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pinEntry,
                        onValueChange = { pinEntry = it.filter { c -> c.isDigit() }.take(6) },
                        label = { Text("PIN") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    pinError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = { TextButton(onClick = ::unlockWithPin, enabled = !verifying && pinEntry.length >= 4) { Text("Unlock") } },
            dismissButton = { TextButton(onClick = { showPinDialog = false; pinEntry = ""; pinError = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun VaultBody(modifier: Modifier) {
    val vm: HistoryViewModel = viewModel()
    val vault by vm.vaultScans.collectAsState()
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PillChip("All", !favoritesOnly, { favoritesOnly = false })
            PillChip("Favorites", favoritesOnly, { favoritesOnly = true })
        }

        val visible = if (favoritesOnly) vault.filter { it.isFavorite } else vault

        if (vault.isEmpty()) {
            QsEmptyState(
                icon = Icons.Filled.Lock,
                title = "Vault is empty",
                subtitle = "From any scan result, tap Vault to hide it here behind biometric lock.",
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        } else if (visible.isEmpty()) {
            QsEmptyState(
                icon = Icons.Filled.Star,
                title = "No favorites",
                subtitle = "Star a vault entry to pin it to favorites.",
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(visible, key = { it.id }) { scan ->
                    QsCard(contentPadding = 14.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(Icons.Filled.Lock)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    scan.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    "${scan.type.uppercase()} · ${vaultTime(scan.timestamp)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { vm.toggleFavorite(scan) }) {
                                Icon(
                                    if (scan.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "Toggle favorite",
                                    tint = if (scan.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

private fun isBiometricAvailable(context: Context) = BiometricManager.from(context)
    .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL) ==
    BiometricManager.BIOMETRIC_SUCCESS

private fun showBiometricPrompt(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onResult(true) }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            onResult(false)
            Toast.makeText(activity, errString, Toast.LENGTH_SHORT).show()
        }
        override fun onAuthenticationFailed() {
            Toast.makeText(activity, "Authentication failed", Toast.LENGTH_SHORT).show()
        }
    }
    BiometricPrompt(activity, executor, callback).authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vault")
            .setSubtitle("Authenticate to access vaulted scans")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    )
}

private fun vaultTime(timestamp: Long): String {
    val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val now = System.currentTimeMillis()
    val today = dayFmt.format(Date(now))
    val yesterday = dayFmt.format(Date(now - 86_400_000L))
    return when (dayFmt.format(Date(timestamp))) {
        today -> "Today, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        yesterday -> "Yesterday, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("d MMM, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
