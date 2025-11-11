package com.easy.flowbalance.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.easy.flowbalance.data.FlowDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var windowStart by remember { mutableStateOf(0) }
    var points by remember { mutableStateOf<List<TrendPoint>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = FlowDatabase.getInstance(context)
            val sessions = db.sessionDao().fetchAll()
            val entries = sessions.map { session ->
                val total = db.outgoingDao().fetchTotal(session.id)
                TrendPoint(
                    label = "${monthAbbrev(session.month)} ${session.year % 100}",
                    inflow = session.expectedInflow,
                    outflow = total
                )
            }.sortedBy { it.labelKey }
            points = entries
            windowStart = (entries.size - 4).coerceAtLeast(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Income / Expenses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (points.size > 4) "Drag horizontally to view more months."
                else "Showing ${points.size} month(s) of inflow vs outflow.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    val side = constraints.maxWidth.toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        TrendsCanvas(
                            points = points,
                            startIndex = windowStart,
                            onShift = { shift ->
                                if (points.size <= 4) return@TrendsCanvas
                                windowStart = (windowStart + shift).coerceIn(0, points.size - 4)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendsCanvas(
    points: List<TrendPoint>,
    startIndex: Int,
    onShift: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val window = if (points.size <= 4) points else points.subList(startIndex, (startIndex + 4).coerceAtMost(points.size))
    val scheme = MaterialTheme.colorScheme
    Canvas(
        modifier = modifier.pointerInput(points, startIndex) {
            detectHorizontalDragGestures { _, drag ->
                when {
                    drag > 18 -> onShift(-1)
                    drag < -18 -> onShift(1)
                }
            }
        }
    ) {
        val paddingLeft = 110f
        val paddingBottom = 120f
        val paddingTop = 70f
        val width = size.width - paddingLeft - 40f
        val height = size.height - paddingTop - paddingBottom

        drawRect(
            color = scheme.surface,
            topLeft = Offset.Zero,
            size = size
        )

        if (window.isEmpty()) return@Canvas

        val maxValue = (window.maxOf { maxOf(it.inflow, it.outflow) } * 1.1).coerceAtLeast(1.0)
        val stepX = if (window.size > 1) width / (window.size - 1) else 0f

        fun mapY(value: Double): Float =
            (size.height - paddingBottom) - (value / maxValue).toFloat() * height

        repeat(5) { index ->
            val y = paddingTop + (height / 4f) * index
            drawLine(
                color = scheme.outlineVariant,
                start = Offset(paddingLeft, y),
                end = Offset(size.width - 20f, y),
                strokeWidth = 1.5f
            )
        }

        drawLine(
            color = scheme.onSurfaceVariant,
            start = Offset(paddingLeft, size.height - paddingBottom),
            end = Offset(size.width - 20f, size.height - paddingBottom),
            strokeWidth = 4f
        )

        drawLine(
            color = scheme.onSurfaceVariant,
            start = Offset(paddingLeft, size.height - paddingBottom),
            end = Offset(paddingLeft, paddingTop),
            strokeWidth = 4f
        )

        val inflowPath = Path()
        val outflowPath = Path()
        window.forEachIndexed { index, point ->
            val x = paddingLeft + index * stepX
            val inflowY = mapY(point.inflow)
            val outflowY = mapY(point.outflow)

            if (index == 0) {
                inflowPath.moveTo(x, inflowY)
                outflowPath.moveTo(x, outflowY)
            } else {
                inflowPath.lineTo(x, inflowY)
                outflowPath.lineTo(x, outflowY)
            }

            drawCircle(Color(0xFF2B95A3), center = Offset(x, inflowY), radius = 10f)
            drawCircle(Color(0xFFE05D5D), center = Offset(x, outflowY), radius = 10f)

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 36f
                    isAntiAlias = true
                }
                drawText(point.label, x - 40f, size.height - paddingBottom + 60f, paint)
            }
        }

        drawPath(
            path = inflowPath,
            color = Color(0xFF2B95A3),
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        drawPath(
            path = outflowPath,
            color = Color(0xFFE05D5D),
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 34f
                isAntiAlias = true
            }
            drawText("$0", paddingLeft - 60f, size.height - paddingBottom + 10f, paint)
            drawText(currency(maxValue), paddingLeft - 100f, paddingTop + 20f, paint)
        }
    }
}

private data class TrendPoint(
    val label: String,
    val inflow: Double,
    val outflow: Double
) {
    val labelKey: Int
        get() = label.substringAfter(' ').toIntOrNull() ?: 0
}

private fun monthAbbrev(month: Int): String =
    SimpleDateFormat("MMM", Locale.getDefault()).format(Date(0, month - 1, 1))

private fun currency(value: Double): String =
    String.format(Locale.getDefault(), "$%,.0f", value)


