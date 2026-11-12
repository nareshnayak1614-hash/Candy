package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DayUsage
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafetyBluePrimary
import com.example.ui.theme.WarningAmber

@Composable
fun UsageChart(
    weeklyUsage: List<DayUsage>,
    modifier: Modifier = Modifier
) {
    val maxMinutes = (weeklyUsage.maxOfOrNull { maxOf(it.totalMinutes, it.limitMinutes) } ?: 180).coerceAtLeast(60)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weekly Activity Report",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(SafetyBluePrimary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Safe", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(EmergencyRed))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Over Limit", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyUsage.forEach { day ->
                val fraction = (day.totalMinutes.toFloat() / maxMinutes).coerceIn(0.05f, 1.0f)
                val animatedHeight by animateFloatAsState(targetValue = fraction, label = "barHeight")
                val isOverLimit = day.totalMinutes > day.limitMinutes

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Time Label on top
                    Text(
                        text = "${day.totalMinutes / 60}h",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (isOverLimit) EmergencyRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight(animatedHeight)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                when {
                                    isOverLimit -> EmergencyRed
                                    day.totalMinutes >= day.limitMinutes * 0.85 -> WarningAmber
                                    else -> SafetyBluePrimary
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Day of week label
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
