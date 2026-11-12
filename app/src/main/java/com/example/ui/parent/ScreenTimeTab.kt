package com.example.ui.parent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppUsageLimit
import com.example.model.ChildDevice
import com.example.model.DeviceScreenTimeState
import com.example.ui.components.UsageChart
import com.example.ui.theme.*

@Composable
fun ScreenTimeTab(
    child: ChildDevice,
    screenTimeState: DeviceScreenTimeState,
    pendingExtraMinutes: Int?,
    onUpdateDailyLimit: (Int) -> Unit,
    onToggleAppBlock: (String, Boolean) -> Unit,
    onUpdateAppLimit: (String, Int) -> Unit,
    onToggleBedtime: (Boolean) -> Unit,
    onToggleStudyTime: (Boolean) -> Unit,
    onGrantExtraTime: (Int) -> Unit
) {
    var showAppLimitDialog by remember { mutableStateOf<AppUsageLimit?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Pending Time Request Banner if present
        if (pendingExtraMinutes != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarningAmberContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = WarningAmber)
                            Column {
                                Text(
                                    text = "${child.name} asks for +$pendingExtraMinutes mins",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy900
                                )
                                Text(
                                    text = "Needs extra time for homework / project",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandNavy700
                                )
                            }
                        }
                        Button(
                            onClick = { onGrantExtraTime(pendingExtraMinutes) },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                            modifier = Modifier.testTag("grant_extra_time_button")
                        ) {
                            Text("Grant", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Today's Screen Time Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Screen Time",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val remaining = (screenTimeState.dailyLimitMinutes - screenTimeState.todayUsedMinutes).coerceAtLeast(0)
                            Text(
                                text = "$remaining mins remaining of ${screenTimeState.dailyLimitMinutes / 60}h ${screenTimeState.dailyLimitMinutes % 60}m limit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SafetyBluePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(screenTimeState.todayUsedMinutes * 100 / screenTimeState.dailyLimitMinutes.coerceAtLeast(1))}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SafetyBluePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    val progress = (screenTimeState.todayUsedMinutes.toFloat() / screenTimeState.dailyLimitMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = when {
                            progress >= 1.0f -> EmergencyRed
                            progress >= 0.85f -> WarningAmber
                            else -> SafetyBluePrimary
                        },
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Daily Limit Adjuster Slider
                    Text(
                        text = "Adjust Daily Limit: ${screenTimeState.dailyLimitMinutes / 60}h ${screenTimeState.dailyLimitMinutes % 60}m",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = screenTimeState.dailyLimitMinutes.toFloat(),
                        onValueChange = { onUpdateDailyLimit(it.toInt()) },
                        valueRange = 30f..360f,
                        steps = 10,
                        modifier = Modifier.testTag("daily_limit_slider")
                    )

                    // Quick preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(60 to "1h", 90 to "1.5h", 120 to "2h", 150 to "2.5h", 180 to "3h").forEach { (mins, label) ->
                            val isSelected = screenTimeState.dailyLimitMinutes == mins
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateDailyLimit(mins) },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).testTag("limit_chip_$label")
                            )
                        }
                    }
                }
            }
        }

        // Schedules: Bedtime & Study Time
        item {
            Text(
                text = "Scheduled Restrictions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Bedtime
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BrandNavy600.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Bedtime, contentDescription = null, tint = SafetyBluePrimary)
                            }
                            Column {
                                Text("Bedtime Schedule", fontWeight = FontWeight.SemiBold)
                                Text(
                                    screenTimeState.bedtimeSchedule.formatRange(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = screenTimeState.bedtimeSchedule.enabled,
                            onCheckedChange = onToggleBedtime,
                            modifier = Modifier.testTag("bedtime_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surface)

                    // Study Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SafetyBluePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = SafetyBluePrimary)
                            }
                            Column {
                                Text("Study Time Focus", fontWeight = FontWeight.SemiBold)
                                Text(
                                    screenTimeState.studySchedule.formatRange(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = screenTimeState.studySchedule.enabled,
                            onCheckedChange = onToggleStudyTime,
                            modifier = Modifier.testTag("study_time_switch")
                        )
                    }
                }
            }
        }

        // Weekly Activity Chart
        item {
            UsageChart(weeklyUsage = screenTimeState.weeklyUsage)
        }

        // App-Specific Usage Limits
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App-Specific Limits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${screenTimeState.appLimits.count { it.isBlocked }} Blocked",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(screenTimeState.appLimits) { app ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (app.isBlocked) EmergencyRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (app.category.displayName) {
                                            "Games" -> WarningAmber.copy(alpha = 0.2f)
                                            "Social & Video" -> EmergencyRed.copy(alpha = 0.2f)
                                            "Education" -> SafeGreen.copy(alpha = 0.2f)
                                            else -> SafetyBluePrimary.copy(alpha = 0.2f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (app.category.displayName) {
                                        "Games" -> Icons.Default.SportsEsports
                                        "Social & Video" -> Icons.Default.SmartDisplay
                                        "Education" -> Icons.Default.School
                                        else -> Icons.Default.Apps
                                    },
                                    contentDescription = null,
                                    tint = when (app.category.displayName) {
                                        "Games" -> WarningAmber
                                        "Social & Video" -> EmergencyRed
                                        "Education" -> SafeGreen
                                        else -> SafetyBluePrimary
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(app.appName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    if (app.isBlocked) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "BLOCKED",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = EmergencyRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "${app.category.displayName} • ${app.usedMinutes}m used / ${app.dailyLimitMinutes}m limit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { showAppLimitDialog = app },
                                modifier = Modifier.size(36.dp).testTag("edit_limit_${app.id}")
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = "Edit Limit", modifier = Modifier.size(18.dp))
                            }
                            Switch(
                                checked = !app.isBlocked,
                                onCheckedChange = { allowed -> onToggleAppBlock(app.id, !allowed) },
                                modifier = Modifier.testTag("app_allow_switch_${app.id}")
                            )
                        }
                    }

                    if (!app.isBlocked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val appProgress = (app.usedMinutes.toFloat() / app.dailyLimitMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { appProgress },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = if (appProgress >= 1f) EmergencyRed else SafetyBluePrimary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
        }
    }

    // Edit App Limit Dialog
    showAppLimitDialog?.let { app ->
        var limitInput by remember { mutableFloatStateOf(app.dailyLimitMinutes.toFloat()) }
        AlertDialog(
            onDismissRequest = { showAppLimitDialog = null },
            title = { Text("Limit for ${app.appName}") },
            text = {
                Column {
                    Text(
                        text = "Daily allowance: ${limitInput.toInt()} minutes (${limitInput.toInt() / 60}h ${limitInput.toInt() % 60}m)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = limitInput,
                        onValueChange = { limitInput = it },
                        valueRange = 15f..180f,
                        steps = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateAppLimit(app.id, limitInput.toInt())
                        showAppLimitDialog = null
                    },
                    modifier = Modifier.testTag("save_app_limit_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppLimitDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
