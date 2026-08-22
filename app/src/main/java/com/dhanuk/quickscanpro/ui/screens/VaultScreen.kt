package com.dhanuk.quickscanpro.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.ui.design.IconBadge
import com.dhanuk.quickscanpro.ui.design.PillChip
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsEmptyState
import com.dhanuk.quickscanpro.ui.design.QsOutlinedButton
import com.dhanuk.quickscanpro.util.VaultAuth
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import com.dhanuk.quickscanpro.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(onNavigateBack: () -> Unit) {
    val settingsVm: SettingsViewModel = viewModel()
    val lockMode by settingsVm.vaultLockMode.collectAsState()
    val context = LocalContext.current
    // Intentionally NOT rememberSaveable: a process-death restore must re-authenticate.
    var unlocked by remember { mutableStateOf(false) }
    val hasDeviceLock = remember { VaultAuth.hasDeviceLock(context) }
    val activity = context as? FragmentActivity

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
        if (lockMode == "none") {
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
                    Text("Vault is off", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Your vault uses your phone's own screen lock. Turn on \"Phone lock\" for the vault in Settings to protect your scans.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    QsOutlinedButton(text = "Go back", onClick = onNavigateBack)
                }
            }
        } else if (!unlocked) {
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
                        "Unlock with your phone's screen lock — fingerprint, face, PIN or pattern.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    when {
                        !hasDeviceLock -> {
                            Text(
                                "This phone has no screen lock set. Set one in system settings to unlock the vault.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            QsButton(
                                text = "Open device security settings",
                                icon = Icons.Filled.Lock,
                                onClick = { VaultAuth.openSecuritySettings(context) }
                            )
                        }
                        activity != null -> QsButton(
                            text = "Unlock",
                            icon = Icons.Filled.Fingerprint,
                            onClick = {
                                VaultAuth.unlock(
                                    activity = activity,
                                    onSuccess = { unlocked = true },
                                    onCancel = {
                                        Toast.makeText(context, "Locked", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg.ifBlank { "Authentication failed" }, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        )
                        else -> Text(
                            "Biometric unlock is not available in this context.",
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
}

@Composable
private fun VaultBody(modifier: Modifier) {
    val vm: HistoryViewModel = viewModel()
    val context = LocalContext.current
    val vault by vm.vaultScans.collectAsState()
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ScanResult?>(null) }
    var confirmDelete by remember { mutableStateOf<ScanResult?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PillChip("All", !favoritesOnly, { favoritesOnly = false })
            PillChip("Favorites", favoritesOnly, { favoritesOnly = true })
        }

        val visible = if (favoritesOnly) vault.filter { it.isFavorite } else vault

        // Group batch members into one expandable row, exactly like History —
        // a hidden/vaulted bulk scan must stay a batch everywhere, never split
        // into individual rows.
        val batches = visible.filter { !it.batchId.isNullOrBlank() }.groupBy { it.batchId!! }
        val singles = visible.filter { it.batchId.isNullOrBlank() }
        var expandedBatch by rememberSaveable { mutableStateOf<String?>(null) }

        if (vault.isEmpty()) {
            QsEmptyState(
                icon = Icons.Filled.Lock,
                title = "Vault is empty",
                subtitle = "From any scan result, tap Vault to hide it here behind your phone lock.",
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

                // One grouped row per hidden/vaulted batch (kept together, like History)
                batches.forEach { (batchId, members) ->
                    item(key = "group-$batchId") {
                        val isExpanded = expandedBatch == batchId
                        QsCard(contentPadding = 14.dp) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    expandedBatch = if (isExpanded) null else batchId
                                }
                            ) {
                                IconBadge(Icons.Filled.Lock)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "Bulk scan · ${members.size} items",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        vaultTime(members.last().timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse group" else "Expand group",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                    if (isExpanded) {
                        items(members, key = { it.id }) { scan ->
                            Box(Modifier.padding(start = 24.dp)) {
                                VaultItemRow(
                                    scan = scan,
                                    onFavorite = { vm.toggleFavorite(scan) },
                                    onDelete = { confirmDelete = scan },
                                    onView = { selectedItem = scan }
                                )
                            }
                        }
                    }
                }

                items(singles, key = { it.id }) { scan ->
                    VaultItemRow(
                        scan = scan,
                        onFavorite = { vm.toggleFavorite(scan) },
                        onDelete = { confirmDelete = scan },
                        onView = { selectedItem = scan }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    // Detail dialog: view content + unvault action
    selectedItem?.let { scan ->
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = { Text(scan.type.uppercase()) },
            text = {
                Column {
                    Text(
                        scan.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        vaultTime(scan.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.setVault(scan, false, context)
                    selectedItem = null
                }) { Text("Unvault") }
            },
            dismissButton = {
                TextButton(onClick = { selectedItem = null }) { Text("Close") }
            }
        )
    }

    // Delete confirmation
    confirmDelete?.let { scan ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete this vault item?") },
            text = { Text("This will permanently remove the scan from the vault.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(scan.id, context)
                    confirmDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun VaultItemRow(
    scan: ScanResult,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    onView: () -> Unit
) {
    QsCard(contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.Lock)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    scan.note.ifBlank { scan.content },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${scan.type.uppercase()} · ${vaultTime(scan.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    if (scan.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Toggle favorite",
                    tint = if (scan.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onView) {
                Icon(
                    Icons.Filled.LockOpen,
                    contentDescription = "View / Unvault",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
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
