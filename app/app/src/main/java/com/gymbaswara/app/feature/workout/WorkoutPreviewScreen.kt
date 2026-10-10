package com.gymbaswara.app.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.feature.exercises.ExerciseListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPreviewScreen(
    onNavigateBack: () -> Unit,
    onStartWorkout: () -> Unit,
    viewModel: WorkoutPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isWorkoutActive by viewModel.isWorkoutActive.collectAsState()
    var showExerciseSelection by remember { mutableStateOf(false) }
    var showOverrideDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showOverrideDialog = false },
            title = { Text("Peringatan") },
            text = { Text("Ada sesi latihan yang sedang berjalan di latar belakang. Apakah Anda ingin mengakhiri sesi tersebut dan memulai sesi yang baru?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOverrideDialog = false
                        viewModel.forceStartNewWorkout(onStartWorkout)
                    }
                ) {
                    Text("Mulai Baru", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverrideDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showExerciseSelection) {
        ExerciseListScreen(
            isSelectionMode = true,
            initiallySelected = emptyList(),
            onSelectionComplete = { selected ->
                selected.forEach { exercise ->
                    viewModel.addExercise(exercise.id, exercise.name)
                }
                showExerciseSelection = false
            },
            onCancelSelection = { showExerciseSelection = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview Latihan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (uiState.routineId != null) {
                        TextButton(
                            onClick = { 
                                viewModel.saveRoutineChanges {
                                    Toast.makeText(context, "Perubahan disimpan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                PaddingValues(16.dp).let {
                    Button(
                        onClick = { 
                            if (isWorkoutActive) {
                                showOverrideDialog = true
                            } else {
                                viewModel.startWorkout(onStartWorkout)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Mulai Latihan",
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.routineName,
                    onValueChange = viewModel::updateRoutineName,
                    label = { Text("Nama Latihan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daftar Gerakan (${uiState.exercises.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showExerciseSelection = true }) {
                        Text("Tambah Gerakan")
                    }
                }
            }
            
            if (uiState.exercises.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada gerakan yang ditambahkan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                itemsIndexed(uiState.exercises, key = { _, item -> item.id }) { index, exercise ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.exerciseName,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.moveExerciseUp(index) },
                                    enabled = index > 0
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Naik")
                                }
                                IconButton(
                                    onClick = { viewModel.moveExerciseDown(index) },
                                    enabled = index < uiState.exercises.size - 1
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Turun")
                                }
                                IconButton(onClick = { viewModel.removeExercise(exercise.id) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        if (exercise.notes == null) {
                            TextButton(
                                onClick = { viewModel.updateExerciseNotes(exercise.id, "") },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("+ Tambah Catatan")
                            }
                        } else {
                            OutlinedTextField(
                                value = exercise.notes,
                                onValueChange = { viewModel.updateExerciseNotes(exercise.id, it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // padding for bottom bar
            }
        }
    }
}
