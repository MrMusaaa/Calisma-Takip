package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DayWorkSummary
import java.util.Locale

@Composable
fun WeeklyBarChart(
    dayBreakdowns: List<DayWorkSummary>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<DayWorkSummary?>(null) }
    val maxHours = (dayBreakdowns.maxOfOrNull { it.totalHours } ?: 8.0).coerceAtLeast(8.0)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Haftalık Çalışma Grafiği",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pazartesi — Pazar mesai dağılımı",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedDay != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${selectedDay!!.dayName}: ${String.format(Locale.US, "%.1f", selectedDay!!.totalHours)}s (${String.format(Locale.US, "%.0f", selectedDay!!.totalEarnings)} $currencySymbol)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart area
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary
            val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
            val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val barCount = 7
                    val barWidth = (width / barCount) * 0.45f
                    val slotWidth = width / barCount

                    // Draw reference grid lines (4h, 8h)
                    val line4hY = height - (4.0f / maxHours.toFloat()) * (height - 30.dp.toPx())
                    val line8hY = height - (8.0f / maxHours.toFloat()) * (height - 30.dp.toPx())

                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, line4hY),
                        end = Offset(width, line4hY),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, line8hY),
                        end = Offset(width, line8hY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw bars
                    dayBreakdowns.forEachIndexed { index, day ->
                        val centerX = slotWidth * index + slotWidth / 2f
                        val barHeight = if (day.totalHours > 0) {
                            ((day.totalHours.toFloat() / maxHours.toFloat()) * (height - 30.dp.toPx()))
                                .coerceAtLeast(6.dp.toPx())
                        } else {
                            3.dp.toPx()
                        }
                        val topY = height - barHeight
                        val leftX = centerX - barWidth / 2f

                        val barBrush = when {
                            day.isToday -> Brush.verticalGradient(
                                colors = listOf(secondaryColor, primaryColor)
                            )
                            day.isPayday -> Brush.verticalGradient(
                                colors = listOf(tertiaryColor, tertiaryColor.copy(alpha = 0.7f))
                            )
                            day.totalHours > 0 -> Brush.verticalGradient(
                                colors = listOf(primaryColor, primaryColor.copy(alpha = 0.6f))
                            )
                            else -> Brush.verticalGradient(
                                colors = listOf(onSurfaceVariant.copy(alpha = 0.2f), onSurfaceVariant.copy(alpha = 0.1f))
                            )
                        }

                        // Background track
                        drawRoundRect(
                            color = onSurfaceVariant.copy(alpha = 0.08f),
                            topLeft = Offset(leftX, 0f),
                            size = Size(barWidth, height),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Filled bar
                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(leftX, topY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayBreakdowns.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedDay = day }
                            .padding(vertical = 4.dp)
                    ) {
                        // Hours text
                        Text(
                            text = if (day.totalHours > 0) "${String.format(Locale.US, "%.1f", day.totalHours)}s" else "—",
                            fontSize = 10.sp,
                            fontWeight = if (day.totalHours > 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Day Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = day.dayName,
                                fontSize = 12.sp,
                                fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = when {
                                    day.isToday -> MaterialTheme.colorScheme.secondary
                                    day.isPayday -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (day.isPayday) {
                                Text(
                                    text = "💰",
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(start = 1.dp)
                                )
                            }
                        }

                        // Date number
                        Text(
                            text = "${day.date.dayOfMonth}",
                            fontSize = 10.sp,
                            color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
