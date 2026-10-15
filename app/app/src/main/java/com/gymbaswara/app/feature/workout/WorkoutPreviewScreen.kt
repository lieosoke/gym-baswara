package com.gymbaswara.app.feature.workout

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.core.ui.theme.AppleCrimson
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkCard
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkControl
import com.gymbaswara.app.core.ui.theme.AppleGraySubtext
import com.gymbaswara.app.core.ui.theme.AppleVolt
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
            containerColor = AppleGrayDarkCard,
            title = {
                Text(
                    text = "Latihan Aktif Berjalan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Ada sesi latihan yang sedang berjalan di latar belakang. Apakah Anda ingin mengakhiri sesi tersebut dan memulai sesi yang baru?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppleGraySubtext
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverrideDialog = false
                        viewModel.forceStartNewWorkout(onStartWorkout)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppleCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Mulai Baru", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showOverrideDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Batal", color = AppleGraySubtext, fontWeight = FontWeight.SemiBold)
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
                title = {
                    Text(
                        text = "Pratinjau Sesi",
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
                actions = {
                    if (uiState.routineId != null) {
                        TextButton(
                            onClick = {
                                viewModel.saveRoutineChanges {
                                    Toast.makeText(context, "Perubahan rutinitas disimpan", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = "Simpan",
                                fontWeight = FontWeight.Bold,
                                color = AppleVolt,
                                fontSize = 15.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, AppleGrayDarkControl),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
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
                            .height(56.dp),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppleVolt,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mulai Latihan Sekarang",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Routine Name Card
            item(key = "routine_name_card") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = AppleGrayDarkCard,
                    border = BorderStroke(1.dp, AppleGrayDarkControl)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "NAMA SESI LATIHAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppleVolt
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = uiState.routineName,
                            onValueChange = viewModel::updateRoutineName,
                            placeholder = { Text("Nama latihan...", color = AppleGraySubtext) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            // Exercise Section Header
            item(key = "exercises_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Gerakan (${uiState.exercises.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Button(
                        onClick = { showExerciseSelection = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppleGrayDarkControl,
                            contentColor = AppleVolt
                        ),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tambah",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Empty State
            if (uiState.exercises.isEmpty()) {
                item(key = "exercises_empty") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
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
                                    .size(52.dp)
                                    .background(AppleGrayDarkControl, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = AppleVolt,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Belum Ada Gerakan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tambahkan gerakan untuk memulai sesi latihan Anda.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppleGraySubtext,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(uiState.exercises, key = { _, item -> item.id }) { index, exercise ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = AppleGrayDarkCard,
                        border = BorderStroke(1.dp, AppleGrayDarkControl)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Index badge
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(AppleGrayDarkControl, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppleVolt
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exercise.exerciseName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    if (!exercise.equipment.isNullOrBlank()) {
                                        Text(
                                            text = exercise.equipment,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppleGraySubtext
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.moveExerciseUp(index) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Naikkan urutan gerakan",
                                            tint = if (index > 0) Color.White else AppleGrayDarkControl
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.moveExerciseDown(index) },
                                        enabled = index < uiState.exercises.size - 1,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Turunkan urutan gerakan",
                                            tint = if (index < uiState.exercises.size - 1) Color.White else AppleGrayDarkControl
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeExercise(exercise.id) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus gerakan",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            if (exercise.notes == null) {
                                TextButton(
                                    onClick = { viewModel.updateExerciseNotes(exercise.id, "") },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text("+ Tambah Catatan Sesi", color = AppleVolt, fontSize = 13.sp)
                                }
                            } else {
                                OutlinedTextField(
                                    value = exercise.notes,
                                    onValueChange = { viewModel.updateExerciseNotes(exercise.id, it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    placeholder = { Text("Target beban, repetisi, atau form...", color = AppleGraySubtext) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = AppleGrayDarkControl,
                                        unfocusedContainerColor = AppleGrayDarkControl,
                                        focusedBorderColor = AppleVolt,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
