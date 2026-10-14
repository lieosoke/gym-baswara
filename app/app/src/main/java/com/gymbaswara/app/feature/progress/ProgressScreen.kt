package com.gymbaswara.app.feature.progress

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymbaswara.app.domain.model.PRProgression
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grafik Progres", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Pilih Latihan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    placeholder = { Text("Ketik untuk mencari...") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    val filteredExercises = exercises.filter { it.name.contains(searchText, ignoreCase = true) }
                    filteredExercises.forEach { exercise ->
                        DropdownMenuItem(
                            text = { Text(exercise.name) },
                            onClick = {
                                viewModel.selectExercise(exercise.id)
                                searchText = exercise.name
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Personal Record (PR)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Perkembangan beban maksimal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (prHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada riwayat latihan",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LineChart(
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
    }
}

@Composable
fun LineChart(
    data: List<PRProgression>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Blue
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    if (data.isEmpty()) return

    val maxWeight = max(data.maxOf { it.maxWeight }, 1.0)
    val minWeight = data.minOf { it.maxWeight }
    val range = max(maxWeight - minWeight, 10.0) // padding min range 10kg

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 20.dp.toPx()
        
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2
        
        val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth
        
        val points = data.mapIndexed { index, point ->
            val x = padding + (index * stepX)
            // Reverse Y calculation because Canvas Y=0 is top
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
                
                // Control points for cubic bezier curve
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
            
            // Draw gradient fill
            val gradientBrush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.4f), Color.Transparent),
                startY = 0f,
                endY = height - padding
            )
            
            clipRect(right = width * animationProgress.value) {
                drawPath(
                    path = fillPath,
                    brush = gradientBrush
                )
                
                // Draw line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                
                points.forEachIndexed { index, point ->
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                    
                    // Draw text label
                    val weightText = "${data[index].maxWeight}kg"
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 12.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    // Draw text shadow
                    val shadowPaint = android.graphics.Paint(textPaint).apply {
                        color = android.graphics.Color.BLACK
                        alpha = 150
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        weightText,
                        point.x,
                        point.y - 12.dp.toPx(),
                        shadowPaint
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        weightText,
                        point.x,
                        point.y - 12.dp.toPx(),
                        textPaint
                    )
                }
            }
        }
    }
}
