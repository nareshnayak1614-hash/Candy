package com.example.ui.parent

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun LiveStreamTab(
    child: ChildDevice,
    session: LiveStreamSession,
    onRequestStream: (LiveStreamType) -> Unit,
    onStopStream: () -> Unit,
    onToggleCameraFacing: () -> Unit,
    onToggleAudioMute: () -> Unit,
    onSimulateChildGrantConsent: () -> Unit
) {
    var selectedStreamType by remember { mutableStateOf(LiveStreamType.CAMERA) }

    // Waveform animation
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing)),
        label = "wavePhase"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Stream Mode Selector
        item {
            TabRow(
                selectedTabIndex = selectedStreamType.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedStreamType == LiveStreamType.CAMERA,
                    onClick = { selectedStreamType = LiveStreamType.CAMERA },
                    text = { Text("Live Camera", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedStreamType == LiveStreamType.MICROPHONE,
                    onClick = { selectedStreamType = LiveStreamType.MICROPHONE },
                    text = { Text("Live Audio", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedStreamType == LiveStreamType.SCREEN_MIRROR,
                    onClick = { selectedStreamType = LiveStreamType.SCREEN_MIRROR },
                    text = { Text("Screen Mirror", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.ScreenShare, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        // Active Session Status or Stream Viewer
        item {
            when (session.state) {
                StreamConsentState.ACTIVE_STREAMING -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandNavy900),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top session status bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Live Pulsing Indicator
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(EmergencyRed)
                                    )
                                    Text(
                                        text = "LIVE STREAMING (${child.name}'s Device)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    color = BrandNavy700,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val min = session.secondsRemaining / 60
                                        val sec = session.secondsRemaining % 60
                                        Text(
                                            text = "${min}:${if (sec < 10) "0$sec" else "$sec"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Video / Audio Viewport
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BrandNavy800),
                                contentAlignment = Alignment.Center
                            ) {
                                if (session.sessionType == LiveStreamType.CAMERA) {
                                    // Camera feed representation
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        // Viewport grid lines
                                        drawLine(Color.White.copy(alpha = 0.2f), Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), 1f)
                                        drawLine(Color.White.copy(alpha = 0.2f), Offset(2f * size.width / 3f, 0f), Offset(2f * size.width / 3f, size.height), 1f)
                                        drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), 1f)
                                        drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, 2f * size.height / 3f), Offset(size.width, 2f * size.height / 3f), 1f)

                                        // Focus reticle
                                        val c = Offset(size.width / 2f, size.height / 2f)
                                        drawCircle(Color.White.copy(alpha = 0.5f), 30f, c, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = SafetyBlueLight,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (session.cameraFacingFront) "Front Camera Active" else "Rear Ultra-Wide Active",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "HD 1080p • 30 FPS • WebRTC Encrypted",
                                            color = SlateTextSecondaryDark,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                } else if (session.sessionType == LiveStreamType.SCREEN_MIRROR) {
                                    // Screen mirror canvas preview
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = SafetyBlueLight, modifier = Modifier.size(52.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("${child.name}'s Screen: ${child.currentActiveApp}", color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("MediaProjection Live Stream • Zero Latency", color = SlateTextSecondaryDark, style = MaterialTheme.typography.labelSmall)
                                    }
                                } else {
                                    // Audio waveform
                                    Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                                        val midY = size.height / 2f
                                        val count = 28
                                        val step = size.width / count
                                        for (i in 0 until count) {
                                            val h = kotlin.math.sin(wavePhase + i * 0.4f) * 25f * (if (session.audioMuted) 0.05f else 1.0f)
                                            drawLine(
                                                color = if (session.audioMuted) SlateBorderDark else SafeGreen,
                                                start = Offset(i * step + step / 2f, midY - kotlin.math.abs(h)),
                                                end = Offset(i * step + step / 2f, midY + kotlin.math.abs(h)),
                                                strokeWidth = 4f,
                                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                                            )
                                        }
                                    }
                                }

                                // Overlay indicators (Camera & Mic status badges)
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(color = SafeGreen, shape = RoundedCornerShape(4.dp)) {
                                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("CAM ON", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Surface(color = if (session.audioMuted) SlateBorderDark else WarningAmber, shape = RoundedCornerShape(4.dp)) {
                                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(if (session.audioMuted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (session.audioMuted) "MUTED" else "MIC ON", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Controls bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (session.sessionType == LiveStreamType.CAMERA) {
                                    IconButton(
                                        onClick = onToggleCameraFacing,
                                        modifier = Modifier.testTag("toggle_camera_flip_button")
                                    ) {
                                        Icon(Icons.Default.Cameraswitch, contentDescription = "Flip Camera", tint = Color.White)
                                    }
                                }

                                IconButton(
                                    onClick = onToggleAudioMute,
                                    modifier = Modifier.testTag("toggle_audio_mute_button")
                                ) {
                                    Icon(
                                        imageVector = if (session.audioMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = "Mute/Unmute",
                                        tint = if (session.audioMuted) EmergencyRed else Color.White
                                    )
                                }

                                Button(
                                    onClick = onStopStream,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                                    modifier = Modifier.testTag("terminate_stream_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Stop Session")
                                }
                            }
                        }
                    }
                }

                StreamConsentState.AWAITING_CHILD_CONSENT -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarningAmberContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = WarningAmber, modifier = Modifier.size(36.dp))
                            Text(
                                text = "Awaiting Child-Device Consent",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy900
                            )
                            Text(
                                text = "In compliance with Android child safety and privacy policies, a consent prompt has been dispatched to ${child.name}'s screen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandNavy700,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            // Simulator helper for immediate evaluation
                            Button(
                                onClick = onSimulateChildGrantConsent,
                                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                modifier = Modifier.testTag("simulate_child_consent_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simulate: Child Taps 'Grant Consent'")
                            }

                            TextButton(onClick = onStopStream) {
                                Text("Cancel Request", color = BrandNavy900)
                            }
                        }
                    }
                }

                else -> {
                    // IDLE or Terminated
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SafetyBluePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (selectedStreamType) {
                                        LiveStreamType.CAMERA -> Icons.Default.Videocam
                                        LiveStreamType.MICROPHONE -> Icons.Default.Mic
                                        LiveStreamType.SCREEN_MIRROR -> Icons.Default.ScreenShare
                                    },
                                    contentDescription = null,
                                    tint = SafetyBluePrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Text(
                                text = "Start ${selectedStreamType.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = when (selectedStreamType) {
                                    LiveStreamType.CAMERA -> "Connect to ${child.name}'s camera for a quick safety visual verification. Maximum duration: 3 minutes per session."
                                    LiveStreamType.MICROPHONE -> "Check surrounding audio environment. Child device will display visible active microphone badge."
                                    LiveStreamType.SCREEN_MIRROR -> "View real-time screen activity via Android MediaProjection API with explicit child permission."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = { onRequestStream(selectedStreamType) },
                                modifier = Modifier.fillMaxWidth().testTag("start_stream_request_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Authorize & Request (${child.name})")
                            }
                        }
                    }
                }
            }
        }

        // Child Privacy & Safety Disclosure
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(18.dp))
                        Text("Privacy Safeguards & Standards", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "• Child Consent Required: No secret surveillance. The child must tap 'Allow' on their screen before any streaming begins.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Visible System Badges: Android green camera/mic dot and ongoing status banner are displayed on the child's screen during the session.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Auto-Termination: Sessions automatically cut off after 3 minutes to preserve battery and privacy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
