package com.gymbaswara.app.feature.exercises

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkCard
import com.gymbaswara.app.core.ui.theme.AppleGrayDarkControl
import com.gymbaswara.app.core.ui.theme.AppleGraySubtext
import com.gymbaswara.app.core.ui.theme.AppleVolt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    exerciseName: String = "Detail Gerakan",
    primaryMuscle: String? = null,
    equipment: String? = null,
    movementType: String? = null,
    difficulty: String? = null,
    instructions: String? = null,
    description: String? = null,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Gerakan",
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        androidx.compose.runtime.key(exerciseId) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
            // Hero Header Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = AppleGrayDarkCard,
                border = BorderStroke(1.dp, AppleGrayDarkControl)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(AppleGrayDarkControl, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = exerciseName.take(1).uppercase(),
                            color = AppleVolt,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppleVolt.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = primaryMuscle ?: "Otot Utama",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppleVolt,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppleGrayDarkControl
                        ) {
                            Text(
                                text = equipment ?: "Mandiri",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (!description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppleGraySubtext
                        )
                    }
                }
            }

            // Specs Trio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = AppleGrayDarkCard,
                    border = BorderStroke(1.dp, AppleGrayDarkControl)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TARGET",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppleGraySubtext
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = primaryMuscle ?: "General",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = AppleGrayDarkCard,
                    border = BorderStroke(1.dp, AppleGrayDarkControl)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TIPE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppleGraySubtext
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = movementType ?: "Compound",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = AppleGrayDarkCard,
                    border = BorderStroke(1.dp, AppleGrayDarkControl)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TINGKAT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppleGraySubtext
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = difficulty ?: "Semua Level",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppleVolt
                        )
                    }
                }
            }

            // Form Guide / Execution Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = AppleGrayDarkCard,
                border = BorderStroke(1.dp, AppleGrayDarkControl)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "PANDUAN EKSEKUSI GERAKAN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppleVolt
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (!instructions.isNullOrBlank()) {
                            instructions
                        } else {
                            "1. Posisikan tubuh dengan stabil sebelum memulai angkatan.\n2. Tarik napas saat fase menurunkan beban (eksentrik) dan kontrol gerakan.\n3. Hembuskan napas secara bertenaga saat fase mengangkat beban (konsentrik).\n4. Hindari mengunci sendi secara berlebihan di titik akhir rentang gerak."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                }
            }

            // Safety & Warm Up Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = AppleGrayDarkCard,
                border = BorderStroke(1.dp, AppleGrayDarkControl)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TIPS KEAMANAN & PEMANASAN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppleGraySubtext
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Awali dengan 1 atau 2 set pemanasan menggunakan beban ringan untuk melancarkan sirkulasi cairan sendi dan memastikan aktivasi otot optimal sebelum masuk ke set kerja utama.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleGraySubtext,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        }
    }
}

