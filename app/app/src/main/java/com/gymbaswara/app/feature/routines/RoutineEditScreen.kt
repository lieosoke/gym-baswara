package com.gymbaswara.app.feature.routines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.domain.model.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: RoutineEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) onNavigateBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Ubah Rutinitas" else "Rutinitas Baru",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = state.name.isNotBlank()
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
        ) {
            item {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Nama rutinitas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("Deskripsi (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Latihan (${state.selected.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.selected.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada latihan dipilih.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(state.selected, key = { it.instanceId }) { item ->
                val index = state.selected.indexOfFirst { it.instanceId == item.instanceId }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.exercise.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!item.exercise.primaryMuscle.isNullOrBlank()) {
                                    Text(
                                        text = item.exercise.primaryMuscle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.moveExercise(item.instanceId, -1) },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Naikkan")
                            }
                            IconButton(
                                onClick = { viewModel.moveExercise(item.instanceId, 1) },
                                enabled = index < state.selected.lastIndex
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Turunkan")
                            }
                            IconButton(onClick = { viewModel.removeExercise(item.instanceId) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Hapus latihan",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = item.notes.orEmpty(),
                            onValueChange = { viewModel.updateNotes(item.instanceId, it) },
                            label = { Text("Catatan (opsional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { showPicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah Latihan")
                }
            }
        }
    }

    if (showPicker) {
        ExercisePickerDialog(
            exercises = state.available,
            onDismiss = { showPicker = false },
            onPick = {
                viewModel.addExercise(it)
                showPicker = false
            }
        )
    }
}

@Composable
private fun ExercisePickerDialog(
    exercises: List<Exercise>,
    onDismiss: () -> Unit,
    onPick: (Exercise) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, exercises) {
        if (query.isBlank()) exercises
        else exercises.filter { it.name.contains(query, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Latihan") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Cari latihan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (filtered.isEmpty()) {
                    Text(
                        text = "Tidak ada latihan yang cocok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(filtered, key = { it.id }) { exercise ->
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(exercise) }
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}
