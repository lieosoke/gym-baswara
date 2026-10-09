package com.gymbaswara.app.feature.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.domain.model.Exercise
import com.gymbaswara.app.domain.model.Routine
import com.gymbaswara.app.feature.exercises.ExerciseListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewWorkoutScreen(
    onStartTemplate: (String) -> Unit,
    onResumeWorkout: () -> Unit,
    viewModel: NewWorkoutViewModel = hiltViewModel()
) {
    val routines by viewModel.routines.collectAsState()
    val isWorkoutActive by viewModel.isWorkoutActive.collectAsState()
    val activeRoutineId by viewModel.activeRoutineId.collectAsState()
    
    var showRoutineDialog by remember { mutableStateOf(false) }
    var routineToEdit by remember { mutableStateOf<Routine?>(null) }
    var isRoutinesExpanded by remember { mutableStateOf(true) }
    
    if (showRoutineDialog) {
        CreateRoutineDialog(
            initialRoutine = routineToEdit,
            onDismiss = { 
                showRoutineDialog = false
                routineToEdit = null
            },
            onSave = { id, name, desc, exercises ->
                viewModel.saveRoutine(id, name, desc, exercises)
                showRoutineDialog = false
                routineToEdit = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Gym Baswara",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // Routines Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Latihan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // New Routine Button
                        Button(
                            onClick = { 
                                routineToEdit = null
                                showRoutineDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Routine",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buat Latihan Baru",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // My Routines List
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { isRoutinesExpanded = !isRoutinesExpanded }
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isRoutinesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isRoutinesExpanded) "Tutup daftar" else "Buka daftar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Latihan Saya (${routines.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        AnimatedVisibility(visible = isRoutinesExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (routines.isEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Belum ada daftar latihan",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Buat rutinitas pertama Anda untuk memulai perjalanan kebugaran.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    routines.forEach { routine ->
                                        val isActiveRoutine = isWorkoutActive && routine.id == activeRoutineId
                                        RoutineCard(
                                            routine = routine,
                                            isActiveRoutine = isActiveRoutine,
                                            onClick = { 
                                                if (isActiveRoutine) onResumeWorkout() else onStartTemplate(routine.id)
                                            },
                                            onEdit = { 
                                                routineToEdit = routine
                                                showRoutineDialog = true
                                            },
                                            onDelete = { viewModel.deleteRoutine(routine.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineCard(
    routine: Routine,
    isActiveRoutine: Boolean = false,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(48.dp) // Touch target
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opsi lainnya",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                expanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            val exerciseString = routine.exercises.joinToString(", ") { it.exercise.name }
            Text(
                text = exerciseString.ifEmpty { "Belum ada gerakan" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActiveRoutine) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isActiveRoutine) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = if (isActiveRoutine) "Lanjutkan Latihan" else "Buka",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineDialog(
    initialRoutine: Routine?,
    onDismiss: () -> Unit,
    onSave: (id: String?, name: String, desc: String?, exercises: List<com.gymbaswara.app.domain.model.RoutineExercise>) -> Unit
) {
    var name by remember { mutableStateOf(initialRoutine?.name ?: "") }
    var description by remember { mutableStateOf(initialRoutine?.description ?: "") }
    var selectedExercises by remember { mutableStateOf(initialRoutine?.exercises ?: emptyList()) }
    
    var showExerciseSelection by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showExerciseSelection) {
                ExerciseListScreen(
                    isSelectionMode = true,
                    initiallySelected = selectedExercises.map { it.exercise },
                    onSelectionComplete = { newExercises -> 
                        val existingMap = selectedExercises.associateBy { it.exercise.id }
                        selectedExercises = newExercises.map { ex ->
                            com.gymbaswara.app.domain.model.RoutineExercise(exercise = ex, notes = existingMap[ex.id]?.notes)
                        }
                        showExerciseSelection = false
                    },
                    onCancelSelection = { showExerciseSelection = false }
                )
            } else {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(if (initialRoutine == null) "Latihan Baru" else "Edit Latihan") },
                            navigationIcon = {
                                IconButton(onClick = onDismiss) {
                                    Icon(Icons.Default.Close, "Tutup")
                                }
                            },
                            actions = {
                                TextButton(
                                    onClick = { onSave(initialRoutine?.id, name, description, selectedExercises) },
                                    enabled = name.isNotBlank() && selectedExercises.isNotEmpty()
                                ) {
                                    Text("Simpan")
                                }
                            }
                        )
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
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nama Latihan") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        
                        item {
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Deskripsi (Opsional)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Daftar Gerakan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { showExerciseSelection = true }) {
                                    Text("Tambah Gerakan")
                                }
                            }
                        }

                        if (selectedExercises.isEmpty()) {
                            item {
                                Text(
                                    text = "Belum ada gerakan yang ditambahkan.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(selectedExercises) { routineEx ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(routineEx.exercise.name, fontWeight = FontWeight.SemiBold)
                                            Text(routineEx.exercise.primaryMuscle ?: "Lainnya", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(
                                            onClick = { selectedExercises = selectedExercises.filter { it.instanceId != routineEx.instanceId } }
                                        ) {
                                            Icon(Icons.Default.Close, "Hapus", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    
                                    OutlinedTextField(
                                        value = routineEx.notes ?: "",
                                        onValueChange = { newNotes ->
                                            selectedExercises = selectedExercises.map { 
                                                if (it.instanceId == routineEx.instanceId) it.copy(notes = newNotes) else it 
                                            }
                                        },
                                        placeholder = { Text("Catatan variasi (misal: V-Bar)") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
