package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.ui.design.IconBadge
import com.dhanuk.quickscanpro.ui.design.QsButton
import com.dhanuk.quickscanpro.ui.design.QsCard
import com.dhanuk.quickscanpro.ui.design.QsEmptyState
import com.dhanuk.quickscanpro.ui.design.SectionLabel
import com.dhanuk.quickscanpro.ui.theme.Danger
import com.dhanuk.quickscanpro.ui.theme.Positive
import com.dhanuk.quickscanpro.util.PasswordLeakChecker
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LeakCheckScreen(
    onNavigateBack: () -> Unit,
    onOpenPasswordTools: () -> Unit = {}
) {
    val vm: HistoryViewModel = viewModel()
    val checks by vm.leakChecks.collectAsState()
    val scope = rememberCoroutineScope()

    var input by rememberSaveable { mutableStateOf("") }
    var checking by rememberSaveable { mutableStateOf(false) }
    var report by remember { mutableStateOf<PasswordLeakChecker.LeakReport?>(null) }

    var passwordInput by rememberSaveable { mutableStateOf("") }
    var checkingPassword by rememberSaveable { mutableStateOf(false) }
    var passwordReport by remember { mutableStateOf<PasswordLeakChecker.PasswordLeakReport?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Leak Check", style = MaterialTheme.typography.titleLarge) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(0.dp))

            QsCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Filled.Key)
                    Spacer(Modifier.padding(horizontal = 7.dp))
                    Text(
                        "Check websites against a documented breach database, or test a password live — securely.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column {
                SectionLabel("Website breach check")
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Website or domain") },
                    placeholder = { Text("example.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                QsButton(
                    text = if (checking) "Checking…" else "Run website check",
                    enabled = input.isNotBlank() && !checking,
                    onClick = {
                        checking = true
                        report = null
                        scope.launch {
                            val r = withContext(Dispatchers.Default) { PasswordLeakChecker.check(input) }
                            vm.saveLeakCheck(
                                domain = r.domain,
                                leaked = r.leaked,
                                breachCount = r.breachCount,
                                firstSeen = r.firstSeenYear.toLong()
                            )
                            report = r
                            checking = false
                        }
                    }
                )
            }

            Column {
                SectionLabel("Password exposure check")
                QsCard {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Test a password") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Live check against the Have I Been Pwned database. Your password never leaves this device — only a hashed fingerprint is compared (k-anonymity).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))
                QsButton(
                    text = if (checkingPassword) "Checking…" else "Check password exposure",
                    enabled = passwordInput.isNotBlank() && !checkingPassword,
                    onClick = {
                        checkingPassword = true
                        passwordReport = null
                        scope.launch {
                            passwordReport = PasswordLeakChecker.checkPassword(passwordInput)
                            checkingPassword = false
                        }
                    }
                )
            }

            if (checking || checkingPassword) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            passwordReport?.let { r ->
                val ok = r.success && !r.exposed
                val color = if (ok) Positive else Danger
                QsCard {
                    if (r.success) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (ok) Icons.Filled.GppGood else Icons.Filled.GppBad,
                                contentDescription = null,
                                tint = color
                            )
                            Spacer(Modifier.padding(horizontal = 7.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (ok) "Password not found in breach data" else "Password exposed in breaches",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = color
                                )
                                if (!ok) {
                                    Text(
                                        "Seen ${r.exposureCount} time${if (r.exposureCount != 1L) "s" else ""} in known data breaches. Change it and avoid reuse.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        QsButton(
                            text = "Use the secure generator",
                            onClick = onOpenPasswordTools
                        )
                    } else {
                        Text(
                            r.error ?: "Could not reach the breach database.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            report?.let { r ->
                val color = if (r.leaked) Danger else Positive
                QsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(50))
                                .background(color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (r.leaked) Icons.Filled.GppBad else Icons.Filled.GppGood,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.padding(horizontal = 7.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                r.domain,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (r.leaked) "Found in breach records (${r.breachCount} breach${if (r.breachCount != 1) "es" else ""}, first seen ${r.firstSeenYear})"
                                else "No known breach for this domain",
                                style = MaterialTheme.typography.bodySmall,
                                color = color
                            )
                        }
                    }
                    if (r.signals.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        r.signals.forEach { signal ->
                            Text(
                                "• $signal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column {
                SectionLabel("Recent checks")
                if (checks.isEmpty()) {
                    QsEmptyState(
                        icon = Icons.Filled.Key,
                        title = "No checks yet",
                        subtitle = "Your check history stays on this device."
                    )
                } else {
                    QsCard(contentPadding = 8.dp) {
                        checks.take(10).forEach { c ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (c.leaked) Icons.Filled.GppBad else Icons.Filled.GppGood,
                                    contentDescription = null,
                                    tint = if (c.leaked) Danger else Positive,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.padding(horizontal = 5.dp))
                                Text(c.domain, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(
                                    SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(c.checkedAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
