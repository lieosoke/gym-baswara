package com.gymbaswara.app.feature.exercises

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkCard
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkControl
import com.gymbaswara.app.core.ui.theme.AppleGraySubtext
import com.gymbaswara.app.core.ui.theme.AppleVolt
import com.gymbaswara.app.domain.model.Exercise

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExerciseListScreen(
    viewModel: ExerciseListViewModel = hiltViewModel(),
    isSelectionMode: Boolean = false,
    initiallySelected: List<Exercise> = emptyList(),
    onSelectionComplete: (List<Exercise>) -> Unit = {},
    onCancelSelection: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMuscle by viewModel.selectedMuscle.collectAsState()
    val selectedEquipment by viewModel.selectedEquipment.collectAsState()
    val filterOptions by viewModel.filterOptions.collectAsState()

    var showMuscleDialog by remember { mutableStateOf(false) }
    var showEquipmentDialog by remember { mutableStateOf(false) }

    var selectedExercises by remember { mutableStateOf(initiallySelected.associateBy { it.id }) }

    // Filter Dialogs
    if (showMuscleDialog) {
        FilterDialog(
            title = "Pilih Target Otot",
            options = filterOptions.first,
            selectedOption = selectedMuscle,
            onSelect = {
                viewModel.onMuscleSelected(it)
                showMuscleDialog = false
            },
            onDismiss = { showMuscleDialog = false }
        )
    }

    if (showEquipmentDialog) {
        FilterDialog(
            title = "Pilih Peralatan",
            options = filterOptions.second,
            selectedOption = selectedEquipment,
            onSelect = {
                viewModel.onEquipmentSelected(it)
                showEquipmentDialog = false
            },
            onDismiss = { showEquipmentDialog = false }
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isSelectionMode) "Pilih Gerakan" else "Perpustakaan Gerakan",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = if (isSelectionMode) onCancelSelection else onBack,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isSelectionMode) "Batal" else "Kembali",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (isSelectionMode) {
                            TextButton(
                                onClick = { onSelectionComplete(selectedExercises.values.toList()) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(
                                    text = if (selectedExercises.isEmpty()) "Selesai" else "Selesai (${selectedExercises.size})",
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

                // Apple Inset Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Cari nama gerakan...", color = AppleGraySubtext) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari",
                                tint = AppleGraySubtext,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = viewModel::clearSearch,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus teks",
                                        tint = AppleGraySubtext,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
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

                // Apple Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isMuscleActive = selectedMuscle != "Semua Otot"
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable { showMuscleDialog = true },
                        color = if (isMuscleActive) AppleVolt.copy(alpha = 0.15f) else AppleGrayDarkCard,
                        border = BorderStroke(
                            1.dp,
                            if (isMuscleActive) AppleVolt else AppleGrayDarkControl
                        ),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedMuscle,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isMuscleActive) AppleVolt else Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = if (isMuscleActive) AppleVolt else AppleGraySubtext,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    val isEquipmentActive = selectedEquipment != "Semua Alat"
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable { showEquipmentDialog = true },
                        color = if (isEquipmentActive) AppleVolt.copy(alpha = 0.15f) else AppleGrayDarkCard,
                        border = BorderStroke(
                            1.dp,
                            if (isEquipmentActive) AppleVolt else AppleGrayDarkControl
                        ),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedEquipment,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isEquipmentActive) AppleVolt else Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = if (isEquipmentActive) AppleVolt else AppleGraySubtext,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ExerciseListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AppleVolt
                    )
                }
                is ExerciseListUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ExerciseListUiState.Success -> {
                    val groupedExercises = state.groupedExercises

                    if (groupedExercises.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Tidak Ada Gerakan Ditemukan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Coba ubah kata kunci pencarian atau filter otot dan peralatan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppleGraySubtext,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            groupedExercises.forEach { (muscle, exercises) ->
                                stickyHeader(key = "header_$muscle") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                                            .padding(horizontal = 20.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = muscle.uppercase(),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AppleVolt,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                items(exercises, key = { it.id }) { exercise ->
                                    val isSelected = selectedExercises.containsKey(exercise.id)
                                    ExerciseItemRow(
                                        exercise = exercise,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onToggleSelection = {
                                            selectedExercises = if (isSelected) {
                                                selectedExercises - exercise.id
                                            } else {
                                                selectedExercises + (exercise.id to exercise)
                                            }
                                        }
                                    )
                                    HorizontalDivider(
                                        color = AppleGrayDarkControl.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(start = 84.dp, end = 20.dp)
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

@Composable
fun ExerciseItemRow(
    exercise: Exercise,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isSelectionMode) onToggleSelection()
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Initial Squircle
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(AppleGrayDarkControl, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = exercise.name.take(1).uppercase(),
                color = AppleVolt,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = exercise.primaryMuscle ?: "Otot Lain",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleGraySubtext
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleGrayDarkControl
                )
                Text(
                    text = exercise.equipment ?: "Mandiri",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleGraySubtext
                )
            }
        }

        // Action Target (48dp x 48dp)
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onToggleSelection),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            if (isSelected) AppleVolt else AppleGrayDarkControl,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = if (isSelected) "Dipilih" else "Pilih",
                        tint = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppleGraySubtext,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FilterDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppleGrayDarkCard,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(option) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppleVolt,
                                unselectedColor = AppleGraySubtext
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else AppleGraySubtext
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Tutup",
                    color = AppleVolt,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
