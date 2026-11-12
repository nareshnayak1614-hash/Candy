package com.example.ui.child

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SafeGuardRepository
import com.example.model.ChildDevice
import com.example.model.DeviceScreenTimeState
import com.example.model.LiveStreamSession
import com.example.model.StreamConsentState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildSafetyScreen(
    repository: SafeGuardRepository,
    child: ChildDevice,
    screenTimeState: DeviceScreenTimeState,
    streamSession: LiveStreamSession,
    onSwitchToParentMode: () -> Unit
) {
    var showRequestTimeDialog by remember { mutableStateOf(false) }
    var timeRequestSuccess by remember { mutableStateOf<String?>(null) }
    var showPrivacyInfo by remember { mutableStateOf(false) }

    // SOS pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val sosPulseRadius by infiniteTransition.animateFloat(
        initialValue = 42f,
        targetValue = 68f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "sosPulseRadius"
    )
    val sosPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "sosPulseAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(child.avatarBgColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = child.name.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Column {
                            Text(
                                text = "Hi, ${child.name}!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${child.deviceModel} • Battery ${child.batteryLevel}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Switch to Parent Mode button (protected by PIN)
                    FilledTonalButton(
                        onClick = onSwitchToParentMode,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("switch_to_parent_dashboard_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Parent Mode", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
        ) {
            // Persistent Protection Notification Banner (Policy Requirement)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SafeGreenContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SafeGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SafeGuard Protection Active",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy900
                            )
                            Text(
                                text = "Location and screen-time monitored by Parent with active indicators",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandNavy700
                            )
                        }
                    }
                }
            }

            // Stream Active Status Indicator if camera or screen sharing is active
            if (streamSession.state == StreamConsentState.ACTIVE_STREAMING) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandNavy900),
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(EmergencyRed))
                                Column {
                                    Text(
                                        text = "${streamSession.sessionType.title} Active",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Visible camera/audio indicator in use",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SlateTextSecondaryDark
                                    )
                                }
                            }
                            Button(
                                onClick = { repository.stopStreamSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("child_stop_stream_button")
                            ) {
                                Text("Stop Stream", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Circular Screen Time Gauge
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Today's Screen Time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val remainingMinutes = (screenTimeState.dailyLimitMinutes - screenTimeState.todayUsedMinutes).coerceAtLeast(0)
                        val fraction = (screenTimeState.todayUsedMinutes.toFloat() / screenTimeState.dailyLimitMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)

                        Box(
                            modifier = Modifier.size(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                // Background ring
                                drawArc(
                                    color = Color(0xFFCBD5E1),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                // Active progress ring
                                drawArc(
                                    color = if (fraction >= 1f) EmergencyRed else SafetyBluePrimary,
                                    startAngle = -90f,
                                    sweepAngle = 360f * fraction,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${remainingMinutes / 60}h ${remainingMinutes % 60}m",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Remaining Today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${screenTimeState.todayUsedMinutes}m used of ${screenTimeState.dailyLimitMinutes}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Ask for More Time Button
                        Button(
                            onClick = { showRequestTimeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyBluePrimary),
                            modifier = Modifier.fillMaxWidth(0.8f).testTag("ask_more_time_button")
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask for More Time")
                        }

                        if (timeRequestSuccess != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = timeRequestSuccess ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = SafeGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Bedtime and Study Schedule Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Bedtime, contentDescription = null, tint = SafetyBluePrimary, modifier = Modifier.size(18.dp))
                                Text("Bedtime", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Starts at 9:00 PM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Device locks automatically", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.School, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(18.dp))
                                Text("Study Focus", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("4:00 PM - 6:00 PM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Games paused for homework", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // EMERGENCY SOS PANIC BUTTON
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (child.emergencySosActive) EmergencyRedContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (child.emergencySosActive) "SOS ALERT TRANSMITTING!" else "Emergency SOS Panic Button",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (child.emergencySosActive) EmergencyRed else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (child.emergencySosActive) "Mom and Dad have been alerted with your real-time GPS location." else "Press and hold if you need urgent help or feel unsafe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (child.emergencySosActive) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = EmergencyRed.copy(alpha = sosPulseAlpha),
                                        radius = sosPulseRadius
                                    )
                                }
                            }

                            FilledIconButton(
                                onClick = {
                                    if (child.emergencySosActive) {
                                        repository.cancelSos(child.id)
                                    } else {
                                        repository.triggerSos(child.id)
                                    }
                                },
                                modifier = Modifier
                                    .size(80.dp)
                                    .testTag("child_emergency_sos_button"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (child.emergencySosActive) SafeGreen else EmergencyRed
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (child.emergencySosActive) Icons.Default.Check else Icons.Default.Sos,
                                        contentDescription = "Emergency SOS",
                                        tint = Color.White,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (child.emergencySosActive) "Tap again if you are safe now" else "One tap sends emergency GPS to parents",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Transparent Privacy Accordion (No hidden spying)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = SafetyBluePrimary, modifier = Modifier.size(18.dp))
                                Text("What information is shared?", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { showPrivacyInfo = !showPrivacyInfo }) {
                                Icon(
                                    imageVector = if (showPrivacyInfo) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }
                        }

                        AnimatedVisibility(visible = showPrivacyInfo) {
                            Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("📍 GPS Location: Shared with parents so they know you arrived safely at school or home.", style = MaterialTheme.typography.bodySmall)
                                Text("⏱️ Screen Time: Daily time spent in games and apps to help build healthy sleep and study habits.", style = MaterialTheme.typography.bodySmall)
                                Text("📷 Camera & Mic: NEVER recorded in secret. SafeGuard requires you to tap 'Allow' each time.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // Explicit Consent Modal for Live Streaming & Mirroring
    if (streamSession.state == StreamConsentState.AWAITING_CHILD_CONSENT) {
        AlertDialog(
            onDismissRequest = { repository.denyChildStreamConsent() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = when (streamSession.sessionType) {
                            com.example.model.LiveStreamType.CAMERA -> Icons.Default.Videocam
                            com.example.model.LiveStreamType.MICROPHONE -> Icons.Default.Mic
                            com.example.model.LiveStreamType.SCREEN_MIRROR -> Icons.Default.ScreenShare
                        },
                        contentDescription = null,
                        tint = SafetyBluePrimary
                    )
                    Text("Safety Check Request")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your parent is requesting a 3-minute ${streamSession.sessionType.title} to verify your surroundings and safety.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        color = WarningAmberContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "A visible green indicator will be shown while this is active. You can tap 'Stop' at any time.",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandNavy900,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { repository.grantChildStreamConsent() },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    modifier = Modifier.testTag("child_grant_consent_button")
                ) {
                    Text("Allow (3 Min)")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { repository.denyChildStreamConsent() },
                    modifier = Modifier.testTag("child_deny_consent_button")
                ) {
                    Text("Decline")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Ask For More Time Dialog
    if (showRequestTimeDialog) {
        var requestedAmount by remember { mutableIntStateOf(15) }
        AlertDialog(
            onDismissRequest = { showRequestTimeDialog = false },
            title = { Text("Ask for More Screen Time") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select how much additional time you need:", style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15 to "+15m", 30 to "+30m", 60 to "+1h").forEach { (m, label) ->
                            FilterChip(
                                selected = requestedAmount == m,
                                onClick = { requestedAmount = m },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.requestExtraTime(child.id, requestedAmount)
                        timeRequestSuccess = "Sent request for +$requestedAmount mins to Mom & Dad!"
                        showRequestTimeDialog = false
                    },
                    modifier = Modifier.testTag("send_time_request_button")
                ) {
                    Text("Send Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestTimeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
