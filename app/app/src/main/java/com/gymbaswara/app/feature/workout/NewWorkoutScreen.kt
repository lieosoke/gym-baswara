package com.gymbaswara.app.feature.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.core.ui.theme.AppleCrimson
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkCard
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkControl
import com.gymbaswara.app.core.ui.theme.AppleGraySubtext
import com.gymbaswara.app.core.ui.theme.AppleVolt
import com.gymbaswara.app.domain.model.Routine
import com.gymbaswara.app.domain.model.RoutineExercise
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
                        text = "Mulai Latihan",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Active Workout Live Banner (if running in background)
            if (isWorkoutActive) {
                item(key = "active_workout_banner") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .clickable(onClick = onResumeWorkout),
                        color = AppleCrimson.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AppleCrimson.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(AppleCrimson, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sesi Latihan Sedang Aktif",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Ketuk untuk melanjutkan latihan yang berjalan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppleGraySubtext
                                )
                            }
                            Button(
                                onClick = onResumeWorkout,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppleCrimson,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.heightIn(min = 48.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Text("Lanjutkan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Quick Start Empty Workout Hero Card
            item(key = "quick_start_card") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onStartTemplate("") },
                    color = AppleGrayDarkCard,
                    border = BorderStroke(1.dp, AppleGrayDarkControl),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(AppleGrayDarkControl, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = AppleVolt,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mulai Latihan Kosong",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Catat sesi mandiri tanpa template",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppleGraySubtext
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = AppleVolt,
                            modifier = Modifier.heightIn(min = 36.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mulai",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Routines Header & Action
            item(key = "routines_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rutinitas Latihan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${routines.size} template tersimpan",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleGraySubtext
                        )
                    }
                    Button(
                        onClick = {
                            routineToEdit = null
                            showRoutineDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppleGrayDarkControl,
                            contentColor = AppleVolt
                        ),
                        border = BorderStroke(1.dp, AppleGraySubtext.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Buat Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Routines List
            if (routines.isEmpty()) {
                item(key = "routines_empty") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppleGrayDarkCard,
                        border = BorderStroke(1.dp, AppleGrayDarkControl),
                        shape = RoundedCornerShape(22.dp)
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
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = AppleVolt,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum Ada Rutinitas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Buat rutinitas pertama Anda untuk mempermudah mencatat set dan repetisi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppleGraySubtext,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(routines, key = { it.id }) { routine ->
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

@Composable
fun RoutineCard(
    routine: Routine,
    isActiveRoutine: Boolean = false,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = AppleGrayDarkCard,
        border = BorderStroke(1.dp, if (isActiveRoutine) AppleCrimson.copy(alpha = 0.6f) else AppleGrayDarkControl)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppleGrayDarkControl
                    ) {
                        Text(
                            text = "${routine.exercises.size} Gerakan",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleVolt,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opsi rutinitas",
                            tint = AppleGraySubtext,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(AppleGrayDarkControl)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Rutinitas", color = Color.White) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                            },
                            onClick = {
                                expanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            onClick = {
                                expanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            if (!routine.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleGraySubtext,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val exercisePreview = routine.exercises.joinToString(" • ") { it.exercise.name }
            Text(
                text = exercisePreview.ifEmpty { "Belum ada gerakan yang ditambahkan" },
                style = MaterialTheme.typography.bodySmall,
                color = AppleGraySubtext.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActiveRoutine) AppleCrimson else AppleVolt,
                    contentColor = if (isActiveRoutine) Color.White else Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isActiveRoutine) "Lanjutkan Sesi Latihan" else "Mulai Latihan Ini",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
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
    onSave: (id: String?, name: String, desc: String?, exercises: List<RoutineExercise>) -> Unit
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
                            RoutineExercise(exercise = ex, notes = existingMap[ex.id]?.notes)
                        }
                        showExerciseSelection = false
                    },
                    onCancelSelection = { showExerciseSelection = false }
                )
            } else {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = if (initialRoutine == null) "Rutinitas Baru" else "Edit Rutinitas",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                                }
                            },
                            actions = {
                                TextButton(
                                    onClick = { onSave(initialRoutine?.id, name, description.ifBlank { null }, selectedExercises) },
                                    enabled = name.isNotBlank() && selectedExercises.isNotEmpty(),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(
                                        text = "Simpan",
                                        fontWeight = FontWeight.Bold,
                                        color = if (name.isNotBlank() && selectedExercises.isNotEmpty()) AppleVolt else AppleGraySubtext
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
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
                        item {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nama Rutinitas", color = AppleGraySubtext) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = AppleGrayDarkCard,
                                    unfocusedContainerColor = AppleGrayDarkCard,
                                    focusedBorderColor = AppleVolt,
                                    unfocusedBorderColor = AppleGrayDarkControl,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Deskripsi (Opsional)", color = AppleGraySubtext) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = AppleGrayDarkCard,
                                    unfocusedContainerColor = AppleGrayDarkCard,
                                    focusedBorderColor = AppleVolt,
                                    unfocusedBorderColor = AppleGrayDarkControl,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                minLines = 2
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
                                    text = "Daftar Gerakan (${selectedExercises.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                TextButton(
                                    onClick = { showExerciseSelection = true },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(
                                        text = "+ Tambah Gerakan",
                                        color = AppleVolt,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (selectedExercises.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = AppleGrayDarkCard,
                                    border = BorderStroke(1.dp, AppleGrayDarkControl)
                                ) {
                                    Text(
                                        text = "Belum ada gerakan yang ditambahkan ke rutinitas ini.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppleGraySubtext,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }
                        } else {
                            items(selectedExercises, key = { it.instanceId }) { routineEx ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = AppleGrayDarkCard,
                                    border = BorderStroke(1.dp, AppleGrayDarkControl)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = routineEx.exercise.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    text = routineEx.exercise.primaryMuscle ?: "Otot Lain",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = AppleGraySubtext
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    selectedExercises = selectedExercises.filter { it.instanceId != routineEx.instanceId }
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus gerakan",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        if (routineEx.notes == null) {
                                            TextButton(
                                                onClick = {
                                                    selectedExercises = selectedExercises.map {
                                                        if (it.instanceId == routineEx.instanceId) it.copy(notes = "") else it
                                                    }
                                                },
                                                modifier = Modifier.heightIn(min = 48.dp)
                                            ) {
                                                Text("+ Tambah Catatan", color = AppleVolt, fontSize = 13.sp)
                                            }
                                        } else {
                                            OutlinedTextField(
                                                value = routineEx.notes,
                                                onValueChange = { newNotes ->
                                                    selectedExercises = selectedExercises.map {
                                                        if (it.instanceId == routineEx.instanceId) it.copy(notes = newNotes) else it
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                placeholder = { Text("Catatan set atau instruksi form...", color = AppleGraySubtext) },
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
                    }
                }
            }
        }
    }
}
