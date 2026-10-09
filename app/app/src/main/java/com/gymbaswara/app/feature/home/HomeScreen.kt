package com.gymbaswara.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToWorkout: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onResumeWorkout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val yearlyStats by viewModel.yearlyStats.collectAsState()
    val isWorkoutActive by viewModel.isWorkoutActive.collectAsState()
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Minggu", "Bulan", "Tahun")
    
    val currentStats = when (selectedTabIndex) {
        0 -> weeklyStats
        1 -> monthlyStats
        else -> yearlyStats
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Custom Greeting Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Halo Gengs!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Selagi sehat kita gas Sehattt terus.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                // Banner Mulai Latihan - High Contrast Neon CTA
                Card(
                    onClick = if (isWorkoutActive) onResumeWorkout else onNavigateToWorkout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isWorkoutActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isWorkoutActive) "Lanjutkan Latihan" else "Mulai Latihan",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isWorkoutActive) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isWorkoutActive) "Sesi latihan Anda sedang berjalan" else "Lanjutkan program rutinitas Anda",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isWorkoutActive) MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isWorkoutActive) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow, 
                                contentDescription = if (isWorkoutActive) "Lanjutkan Latihan" else "Mulai Latihan",
                                tint = if (isWorkoutActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
            
            // Statistik Dinamis
            item {
                Text(
                    text = "Aktivitas Anda",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Segmented Control (Pill-shaped tabs)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            val isSelected = selectedTabIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null // Custom indication can be added if needed
                                    ) { selectedTabIndex = index }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val sessionCount = currentStats?.sessionCount ?: 0
                    val volumeKg = currentStats?.totalVolume ?: 0.0
                    val durationSec = currentStats?.totalDurationSeconds ?: 0
                    
                    val (volValue, volUnit) = formatVolume(volumeKg)
                    val (durValue, durUnit) = formatDuration(durationSec)
                    
                    StatCard(
                        title = "Sesi",
                        value = sessionCount.toString(),
                        subtitle = "Latihan",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Volume",
                        value = volValue,
                        subtitle = volUnit,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Durasi",
                        value = durValue,
                        subtitle = durUnit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                // Grafik Progres Button - Sleek Dark Card
                Card(
                    onClick = onNavigateToProgress,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Grafik Personal Record",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pantau perkembangan beban maksimal Anda",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

fun formatVolume(volumeKg: Double): Pair<String, String> {
    return if (volumeKg >= 1000.0) {
        val ton = volumeKg / 1000.0
        Pair(String.format(Locale.US, "%.1f", ton), "Ton")
    } else {
        Pair(String.format(Locale.US, "%.0f", volumeKg), "Kg")
    }
}

fun formatDuration(seconds: Int): Pair<String, String> {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) {
        Pair(String.format(Locale.US, "%d:%02d", hours, minutes), "Jam")
    } else {
        Pair(minutes.toString(), "Menit")
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
