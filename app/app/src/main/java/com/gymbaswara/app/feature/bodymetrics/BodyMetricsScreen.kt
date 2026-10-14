package com.gymbaswara.app.feature.bodymetrics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.domain.model.BodyMetric
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMetricsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BodyMetricsViewModel = hiltViewModel()
) {
    val metrics by viewModel.metrics.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<BodyMetric?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ukuran Tubuh", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showForm = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Catat") }
            )
        }
    ) { padding ->
        if (metrics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada catatan ukuran tubuh.\nMulai catat untuk melihat perubahan Anda.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
            ) {
                items(metrics, key = { it.id }) { metric ->
                    MetricCard(metric = metric, onDelete = { pendingDelete = metric })
                }
            }
        }
    }

    if (showForm) {
        BodyMetricFormDialog(
            onDismiss = { showForm = false },
            onSave = { weight, fat, chest, waist, arm, thigh, notes ->
                viewModel.save(weight, fat, chest, waist, arm, thigh, notes)
                showForm = false
            }
        )
    }

    pendingDelete?.let { metric ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Hapus catatan?") },
            text = { Text("Catatan tanggal ${formatDate(metric.recordedAt)} akan dihapus.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(metric.id)
                    pendingDelete = null
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun MetricCard(metric: BodyMetric, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(metric.recordedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                val parts = buildList {
                    metric.weightKg?.let { add("${formatNumber(it)} kg") }
                    metric.bodyFatPercent?.let { add("${formatNumber(it)}% lemak") }
                    metric.waistCm?.let { add("perut ${formatNumber(it)} cm") }
                }
                Text(
                    text = if (parts.isEmpty()) "Tanpa data" else parts.joinToString(" • "),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus catatan",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun BodyMetricFormDialog(
    onDismiss: () -> Unit,
    onSave: (
        weightKg: Double?,
        bodyFatPercent: Double?,
        chestCm: Double?,
        waistCm: Double?,
        armCm: Double?,
        thighCm: Double?,
        notes: String?
    ) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var arm by remember { mutableStateOf("") }
    var thigh by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Ukuran Tubuh") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberField("Berat (kg)", weight) { weight = it }
                NumberField("Lemak tubuh (%)", bodyFat) { bodyFat = it }
                NumberField("Lingkar dada (cm)", chest) { chest = it }
                NumberField("Lingkar perut (cm)", waist) { waist = it }
                NumberField("Lingkar lengan (cm)", arm) { arm = it }
                NumberField("Lingkar paha (cm)", thigh) { thigh = it }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    weight.toDoubleOrNull(),
                    bodyFat.toDoubleOrNull(),
                    chest.toDoubleOrNull(),
                    waist.toDoubleOrNull(),
                    arm.toDoubleOrNull(),
                    thigh.toDoubleOrNull(),
                    notes
                )
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.isEmpty() || input.all { it.isDigit() || it == '.' || it == ',' }) {
                onValueChange(input.replace(',', '.'))
            }
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

private fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

private fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.1f", value)
