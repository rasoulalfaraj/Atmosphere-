package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.IonCyan
import com.example.ui.theme.SolarAmber
import java.util.Locale

data class ChartDataPoint(
    val label: String,
    val primaryValue: Float,
    val secondaryValue: Float? = null,
    val tooltipSubtitle: String = ""
)

/**
 * Interactive meteorological curve & bar chart with touch scrubbing and live telemetry readout.
 */
@Composable
fun InteractiveMeteorologicalChart(
    title: String,
    subtitle: String,
    points: List<ChartDataPoint>,
    unitSymbol: String,
    primaryColor: Color = IonCyan,
    secondaryColor: Color = SolarAmber,
    primaryLegend: String = "",
    secondaryLegend: String = "",
    showBars: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    var selectedIndex by remember(points) { mutableIntStateOf(0) }
    val safeIndex = selectedIndex.coerceIn(0, points.lastIndex)
    val selectedPoint = points[safeIndex]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Selected point live telemetry badge
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${selectedPoint.label} • ${String.format(Locale.US, "%.1f", selectedPoint.primaryValue)}$unitSymbol",
                    style = MaterialTheme.typography.labelLarge,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
                if (selectedPoint.tooltipSubtitle.isNotEmpty()) {
                    Text(
                        text = selectedPoint.tooltipSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val allValues = points.flatMap { listOfNotNull(it.primaryValue, it.secondaryValue) }
        val minVal = (allValues.minOrNull() ?: 0f) - 1f
        val maxVal = (allValues.maxOrNull() ?: 10f) + 1f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                        selectedIndex = (fraction * (points.size - 1)).toInt().coerceIn(0, points.lastIndex)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(165.dp)) {
                val w = size.width
                val h = size.height
                val topPad = 14f
                val bottomPad = 24f
                val usableH = (h - topPad - bottomPad).coerceAtLeast(10f)

                // Horizontal grid lines
                for (g in 0..3) {
                    val gy = topPad + (usableH * g / 3f)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.18f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 1f
                    )
                }

                val stepX = if (points.size > 1) w / (points.size - 1) else w

                if (showBars) {
                    val barW = (w / points.size.coerceAtLeast(1)) * 0.56f
                    points.forEachIndexed { idx, pt ->
                        val cx = idx * stepX
                        val norm = ((pt.primaryValue - minVal) / range).coerceIn(0.04f, 1f)
                        val barH = norm * usableH
                        val isSel = idx == safeIndex
                        drawRoundRect(
                            color = if (isSel) secondaryColor else primaryColor.copy(alpha = 0.72f),
                            topLeft = Offset((cx - barW / 2f).coerceIn(0f, w - barW), topPad + usableH - barH),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                } else {
                    val linePath = Path()
                    val fillPath = Path()
                    val secondaryPath = Path()
                    var hasSecondary = false

                    points.forEachIndexed { idx, pt ->
                        val x = idx * stepX
                        val y = topPad + usableH * (1f - ((pt.primaryValue - minVal) / range))
                        if (idx == 0) {
                            linePath.moveTo(x, y)
                            fillPath.moveTo(x, topPad + usableH)
                            fillPath.lineTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                        if (idx == points.lastIndex) {
                            fillPath.lineTo(x, topPad + usableH)
                            fillPath.close()
                        }

                        if (pt.secondaryValue != null) {
                            hasSecondary = true
                            val sy = topPad + usableH * (1f - ((pt.secondaryValue - minVal) / range))
                            if (idx == 0) secondaryPath.moveTo(x, sy) else secondaryPath.lineTo(x, sy)
                        }
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.34f), Color.Transparent),
                            startY = topPad,
                            endY = topPad + usableH
                        )
                    )

                    if (hasSecondary) {
                        drawPath(
                            path = secondaryPath,
                            color = secondaryColor.copy(alpha = 0.85f),
                            style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                        )
                    }

                    drawPath(
                        path = linePath,
                        color = primaryColor,
                        style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                    )

                    // Highlight active point
                    val selX = safeIndex * stepX
                    val selY = topPad + usableH * (1f - ((selectedPoint.primaryValue - minVal) / range))
                    drawLine(
                        color = primaryColor.copy(alpha = 0.45f),
                        start = Offset(selX, topPad),
                        end = Offset(selX, topPad + usableH),
                        strokeWidth = 2f
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = Offset(selX, selY)
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 5.5f,
                        center = Offset(selX, selY)
                    )
                }
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labelIndices = listOf(0, points.size / 3, (points.size * 2) / 3, points.lastIndex).distinct()
            labelIndices.forEach { idx ->
                val p = points.getOrNull(idx)
                if (p != null) {
                    Text(
                        text = p.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (primaryLegend.isNotEmpty() || secondaryLegend.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (primaryLegend.isNotEmpty()) {
                    LegendDot(color = primaryColor, text = primaryLegend)
                }
                if (secondaryLegend.isNotEmpty()) {
                    LegendDot(color = secondaryColor, text = secondaryLegend)
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(modifier = Modifier.height(10.dp).padding(end = 2.dp)) {
            drawCircle(color = color, radius = 5f)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
