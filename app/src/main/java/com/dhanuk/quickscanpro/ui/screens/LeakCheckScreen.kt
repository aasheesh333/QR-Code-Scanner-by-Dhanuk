package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.LeakCheck
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton
import com.dhanuk.quickscanpro.util.PasswordLeakChecker
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LeakCheckScreen(onNavigateBack: () -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    var report by remember { mutableStateOf<PasswordLeakChecker.LeakReport?>(null) }
    var isChecking by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val vm: HistoryViewModel = viewModel()
    val checks by vm.leakChecks.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Leak Check", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                leadingIcon = { Icon(Icons.Filled.Domain, contentDescription = null) },
                placeholder = { Text("e.g. acmecorp.com") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            PrimaryButton(
                text = "Check for Leaks",
                onClick = {
                    isChecking = true
                    scope.launch {
                        val r = withContext(Dispatchers.Default) { PasswordLeakChecker.check(input) }
                        report = r
                        vm.saveLeakCheck(domain = r.domain, leaked = r.leaked, breachCount = r.breachCount, firstSeen = r.firstSeenYear.toLong())
                        isChecking = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = input.isNotBlank() && !isChecking
            ) {
                if (isChecking) { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(8.dp)) }
                else { Icon(Icons.Filled.Search, contentDescription = null); Spacer(Modifier.width(8.dp)) }
                Text(if (isChecking) "Checking..." else "Check for Leaks")
            }
            report?.let { r -> ResultBanner(r); Spacer(Modifier.height(8.dp)) }
            Text("RECENT CHECKS", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W600, letterSpacing = 0.01.sp, lineHeight = 20.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            if (checks.isNotEmpty()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(checks, key = { it.id }) { c -> RecentCheckRow(c) } }
            } else {
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No checks yet", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            DisclaimerFooter()
        }
    }
}

@Composable
private fun DisclaimerFooter() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(Modifier.width(8.dp))
        Text("All checks run entirely on your device using a small built-in breach database.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ResultBanner(r: PasswordLeakChecker.LeakReport) {
    val isLeaked = r.leaked
    val containerColor = if (isLeaked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isLeaked) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
    Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)), color = containerColor) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isLeaked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.GppGood, contentDescription = null, tint = if (isLeaked) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (isLeaked) "Compromised" else "Safe", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
                    Spacer(Modifier.height(2.dp))
                    Text(when { isLeaked && r.breachCount > 0 -> "Seen in ${r.breachCount} known breach${if (r.breachCount > 1) "es" else ""}."; isLeaked -> "Suspicious pattern matches."; r.domain.isNotBlank() -> "${r.domain} looks clean."; else -> "No breaches." }, style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
private fun RecentCheckRow(c: LeakCheck) {
    Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)), color = MaterialTheme.colorScheme.surface) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Domain, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column { Text(c.domain, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface); Spacer(Modifier.height(2.dp)); Text(timeAgo(c.checkedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (c.leaked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary))
        }
    }
}

private fun timeAgo(ts: Long): String {
    val m = (System.currentTimeMillis() - ts) / 60000
    return when { m < 1 -> "Just now"; m < 60 -> "${m} minutes ago"; m < 1440 -> "${m / 60} hours ago"; m < 2880 -> "Yesterday"; else -> "${m / 1440} days ago" }
}
