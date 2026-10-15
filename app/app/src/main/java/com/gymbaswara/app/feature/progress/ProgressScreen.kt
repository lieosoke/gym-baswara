package com.gymbaswara.app.feature.progress

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.domain.model.PRProgression
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsState()
    val prHistory by viewModel.prHistory.collectAsState()
    val selectedId by viewModel.selectedExerciseId.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val selectedExercise = exercises.find { it.id == selectedId }

    LaunchedEffect(selectedExercise) {
        if (selectedExercise != null) {
            searchText = selectedExercise.name
        }
    }

    val maxRecordWeight = remember(prHistory) {
        if (prHistory.isNotEmpty()) prHistory.maxOf { it.maxWeight } else 0.0
    }

    val filteredExercises = remember(searchText, exercises) {
        if (searchText.isBlank()) exercises
        else exercises.filter { it.name.contains(searchText, ignoreCase = true) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Grafik Progres",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Pantau perkembangan beban maksimal (Personal Record)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 48.dp)
        ) {
            item(key = "exercise_selector") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "PILIH LATIHAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                                expanded = true
                            },
                            readOnly = false,
                            placeholder = { Text("Ketik untuk mencari latihan...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            if (filteredExercises.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Latihan tidak ditemukan") },
                                    onClick = { expanded = false },
                                    enabled = false
                                )
                            } else {
                                filteredExercises.forEach { exercise ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = exercise.name,
                                                fontWeight = if (exercise.id == selectedId) FontWeight.Bold else FontWeight.Normal,
                                                color = if (exercise.id == selectedId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            viewModel.selectExercise(exercise.id)
                                            searchText = exercise.name
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "chart_card") {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Personal Record (PR)",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Grafik beban angkatan tertinggi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (maxRecordWeight > 0.0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(100.dp)
                                ) {
                                    Text(
                                        text = "TERBAIK ${String.format(Locale.US, "%.0f", maxRecordWeight)} KG",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.5.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (prHistory.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada riwayat latihan untuk gerakan ini.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            AppleLineChart(
                                data = prHistory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                lineColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (prHistory.isNotEmpty()) {
                item(key = "history_header") {
                    Text(
                        text = "RIWAYAT PENCAPAIAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }

                items(prHistory.reversed(), key = { it.dateMillis }) { record ->
                    ApplePRRecordRow(record)
                }
            }
        }
    }
}

@Composable
fun ApplePRRecordRow(record: PRProgression) {
    val dateStr = remember(record.dateMillis) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        sdf.format(Date(record.dateMillis))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Beban Maksimal Sesi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "${String.format(Locale.US, "%.0f", record.maxWeight)} kg",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AppleLineChart(
    data: List<PRProgression>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFD0FD3E)
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    if (data.isEmpty()) return

    val maxWeight = max(data.maxOf { it.maxWeight }, 1.0)
    val minWeight = data.minOf { it.maxWeight }
    val range = max(maxWeight - minWeight, 10.0)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 20.dp.toPx()

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Draw subtle horizontal Apple Health guide lines
        val gridLines = 3
        for (i in 0..gridLines) {
            val y = padding + (chartHeight * i / gridLines)
            drawLine(
                color = Color(0xFF2C2C2E),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth

        val points = data.mapIndexed { index, point ->
            val x = padding + (index * stepX)
            val yRatio = ((point.maxWeight - (minWeight - range * 0.1)) / (range * 1.2)).toFloat()
            val y = height - padding - (yRatio * chartHeight)
            Offset(x, y.coerceIn(padding, height - padding))
        }

        val path = Path()
        val fillPath = Path()

        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, height - padding)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val current = points[i]
                val next = points[i + 1]

                val cpX = (current.x + next.x) / 2

                path.cubicTo(
                    cpX, current.y,
                    cpX, next.y,
                    next.x, next.y
                )

                fillPath.cubicTo(
                    cpX, current.y,
                    cpX, next.y,
                    next.x, next.y
                )
            }

            fillPath.lineTo(points.last().x, height - padding)
            fillPath.close()

            val gradientBrush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = height - padding
            )

            clipRect(right = width * animationProgress.value) {
                drawPath(
                    path = fillPath,
                    brush = gradientBrush
                )

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 3.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                points.forEachIndexed { index, point ->
                    drawCircle(
                        color = Color.White,
                        radius = 5.5.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 3.5.dp.toPx(),
                        center = point
                    )

                    val weightText = "${String.format(Locale.US, "%.0f", data[index].maxWeight)}kg"
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 11.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        weightText,
                        point.x,
                        point.y - 10.dp.toPx(),
                        textPaint
                    )
                }
            }
        }
    }
}
