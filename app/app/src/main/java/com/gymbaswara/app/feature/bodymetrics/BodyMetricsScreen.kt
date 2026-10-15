package com.gymbaswara.app.feature.bodymetrics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.core.ui.theme.AppleCrimson
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkCard
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkControl
import com.gymbaswara.app.core.ui.theme.AppleGraySubtext
import com.gymbaswara.app.core.ui.theme.AppleVolt
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
    val latestMetric by viewModel.latest.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<BodyMetric?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ukuran Tubuh",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showForm = true },
                containerColor = AppleVolt,
                contentColor = Color.Black,
                shape = RoundedCornerShape(100.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                },
                text = {
                    Text(
                        text = "Catat Ukuran",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = AppleGrayDarkCard,
                    border = BorderStroke(1.dp, AppleGrayDarkControl)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(AppleGrayDarkControl, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = AppleVolt,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum Ada Catatan Ukuran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Mulai catat berat badan dan lingkar tubuh untuk melacak perkembangan fisik Anda secara konsisten.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleGraySubtext,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Latest Metric Hero Card
                val hero = latestMetric ?: metrics.firstOrNull()
                if (hero != null && hero.weightKg != null) {
                    item(key = "latest_metric_hero") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            color = AppleGrayDarkCard,
                            border = BorderStroke(1.dp, AppleGrayDarkControl)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "BERAT BADAN TERAKHIR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleVolt
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${formatNumber(hero.weightKg)} kg",
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    if (hero.bodyFatPercent != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = AppleGrayDarkControl
                                        ) {
                                            Text(
                                                text = "${formatNumber(hero.bodyFatPercent)}% Lemak",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AppleVolt,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Dicatat pada ${formatDate(hero.recordedAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppleGraySubtext
                                )
                            }
                        }
                    }
                }

                item(key = "history_header") {
                    Text(
                        text = "Riwayat Pengukuran (${metrics.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(metrics, key = { it.id }) { metric ->
                    MetricCard(metric = metric, onDelete = { pendingDelete = metric })
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp)) // padding for FAB
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
            containerColor = AppleGrayDarkCard,
            title = {
                Text(
                    text = "Hapus Catatan?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Catatan tanggal ${formatDate(metric.recordedAt)} akan dihapus permanen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppleGraySubtext
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.delete(metric.id)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppleCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDelete = null },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Batal", color = AppleGraySubtext, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun MetricCard(metric: BodyMetric, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AppleGrayDarkCard,
        border = BorderStroke(1.dp, AppleGrayDarkControl)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(metric.recordedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleGraySubtext
                )
                Spacer(modifier = Modifier.height(6.dp))

                val measurements = buildList {
                    metric.weightKg?.let { add("Berat: ${formatNumber(it)} kg") }
                    metric.bodyFatPercent?.let { add("Lemak: ${formatNumber(it)}%") }
                    metric.waistCm?.let { add("Perut: ${formatNumber(it)} cm") }
                    metric.chestCm?.let { add("Dada: ${formatNumber(it)} cm") }
                    metric.armCm?.let { add("Lengan: ${formatNumber(it)} cm") }
                    metric.thighCm?.let { add("Paha: ${formatNumber(it)} cm") }
                }

                Text(
                    text = if (measurements.isEmpty()) "Tanpa rincian data" else measurements.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    lineHeight = 20.sp
                )

                if (!metric.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = metric.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleGraySubtext
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus catatan",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
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
        containerColor = AppleGrayDarkCard,
        title = {
            Text(
                text = "Catat Ukuran Tubuh",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberField("Berat Badan (kg)", weight) { weight = it }
                NumberField("Lemak Tubuh (%)", bodyFat) { bodyFat = it }
                NumberField("Lingkar Dada (cm)", chest) { chest = it }
                NumberField("Lingkar Perut (cm)", waist) { waist = it }
                NumberField("Lingkar Lengan (cm)", arm) { arm = it }
                NumberField("Lingkar Paha (cm)", thigh) { thigh = it }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan (opsional)", color = AppleGraySubtext) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppleGrayDarkControl,
                        unfocusedContainerColor = AppleGrayDarkControl,
                        focusedBorderColor = AppleVolt,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        weight.toDoubleOrNull(),
                        bodyFat.toDoubleOrNull(),
                        chest.toDoubleOrNull(),
                        waist.toDoubleOrNull(),
                        arm.toDoubleOrNull(),
                        thigh.toDoubleOrNull(),
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppleVolt,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Batal", color = AppleGraySubtext, fontWeight = FontWeight.SemiBold)
            }
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
        label = { Text(label, color = AppleGraySubtext) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppleGrayDarkControl,
            unfocusedContainerColor = AppleGrayDarkControl,
            focusedBorderColor = AppleVolt,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

private fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

private fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.1f", value)
