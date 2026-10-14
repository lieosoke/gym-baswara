package com.gymbaswara.app.feature.routines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.domain.model.Routine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(
    onNavigateBack: () -> Unit,
    onAddRoutine: () -> Unit,
    onEditRoutine: (String) -> Unit,
    viewModel: RoutineListViewModel = hiltViewModel()
) {
    val routines by viewModel.routines.collectAsState()
    var pendingDelete by remember { mutableStateOf<Routine?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Rutinitas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddRoutine,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Rutinitas") }
            )
        }
    ) { padding ->
        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada rutinitas.\nBuat susunan latihan agar sesi berikutnya lebih cepat dimulai.",
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
                items(routines, key = { it.id }) { routine ->
                    RoutineCard(
                        routine = routine,
                        onClick = { onEditRoutine(routine.id) },
                        onDelete = { pendingDelete = routine }
                    )
                }
            }
        }
    }

    pendingDelete?.let { routine ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Hapus rutinitas?") },
            text = { Text("\"${routine.name}\" akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRoutine(routine.id)
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
private fun RoutineCard(
    routine: Routine,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
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
                    text = routine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!routine.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = routine.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${routine.exercises.size} latihan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus rutinitas",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
