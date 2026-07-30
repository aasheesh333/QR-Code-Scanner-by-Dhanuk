package com.dhanuk.quickscanpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhanuk.quickscanpro.database.LeakCheck
import com.dhanuk.quickscanpro.ui.composables.AppBackground
import com.dhanuk.quickscanpro.ui.composables.PrimaryButton
import com.dhanuk.quickscanpro.ui.theme.SafetySafe
import com.dhanuk.quickscanpro.ui.theme.SafetyRisky
import com.dhanuk.quickscanpro.util.PasswordLeakChecker
import com.dhanuk.quickscanpro.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeakCheckScreen(onNavigateBack: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var report by remember { mutableStateOf<PasswordLeakChecker.LeakReport?>(null) }
    val vm: HistoryViewModel = viewModel()
    val checks by vm.leakChecks.collectAsState()

    AppBackground()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Breach Check", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Domain or URL") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = "Check",
                onClick = {
                    report = PasswordLeakChecker.check(input)
                    val r = report!!
                    vm.saveLeakCheck(
                        domain = r.domain, leaked = r.leaked,
                        breachCount = r.breachCount, firstSeen = r.firstSeenYear.toLong()
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = input.isNotBlank()
            )
            Spacer(Modifier.height(20.dp))
            report?.let { r ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Shield,
                                contentDescription = null,
                                tint = if (r.leaked) SafetyRisky else SafetySafe
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = r.domain,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        if (r.leaked) {
                            Text(
                                text = "Detected in ${r.breachCount} public breach(es)${if (r.firstSeenYear > 0) " since ${r.firstSeenYear}" else ""}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SafetyRisky
                            )
                        } else {
                            Text(
                                text = "No public breach records found for this domain.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SafetySafe
                            )
                        }
                        if (r.signals.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            r.signals.forEach { sig ->
                                Text("• $sig", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            if (checks.isNotEmpty()) {
                Text(
                    text = "Recent checks",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(checks) { c -> RecentCheckRow(c) }
                }
            }
        }
    }
}

@Composable
private fun RecentCheckRow(c: LeakCheck) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(c.domain, style = MaterialTheme.typography.bodyMedium)
            if (c.firstSeen > 0) Text(
                text = "First seen $c.firstSeen",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = if (c.leaked) "Leaked" else "Clean",
            style = MaterialTheme.typography.labelMedium,
            color = if (c.leaked) SafetyRisky else SafetySafe
        )
    }
}
