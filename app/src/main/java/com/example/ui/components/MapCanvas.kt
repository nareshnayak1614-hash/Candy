package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChildDevice
import com.example.model.DeviceLocationState
import com.example.model.GeofenceType
import com.example.ui.theme.*

@Composable
fun MapCanvas(
    devices: List<ChildDevice>,
    selectedDeviceId: String,
    locationState: DeviceLocationState,
    onSelectDevice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    val textMeasurer = rememberTextMeasurer()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BrandNavy900)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(devices) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        // Check if tap hit any device node
                        devices.forEachIndexed { index, dev ->
                            val angle = (index * (360f / devices.size.coerceAtLeast(1))) * (Math.PI / 180f).toFloat()
                            val dist = (80f + (index % 3) * 35f) * zoomLevel
                            val nodePos = Offset(
                                center.x + kotlin.math.cos(angle) * dist,
                                center.y + kotlin.math.sin(angle) * dist
                            )
                            val distance = (offset - nodePos).getDistance()
                            if (distance <= 30f) {
                                onSelectDevice(dev.id)
                            }
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Radar grid lines & concentric rings
            val baseRings = listOf(60f, 110f, 160f, 210f)
            baseRings.forEach { r ->
                drawCircle(
                    color = BrandNavy700.copy(alpha = 0.6f),
                    radius = r * zoomLevel,
                    center = center,
                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
            }

            // Radar axis crosses
            drawLine(
                color = BrandNavy700.copy(alpha = 0.4f),
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = 1f
            )
            drawLine(
                color = BrandNavy700.copy(alpha = 0.4f),
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 1f
            )

            // Geofence Zones
            locationState.geofences.forEachIndexed { i, zone ->
                val gAngle = (i * 120f) * (Math.PI / 180f).toFloat()
                val gDist = (90f + i * 25f) * zoomLevel
                val gCenter = Offset(
                    center.x + kotlin.math.cos(gAngle) * gDist,
                    center.y + kotlin.math.sin(gAngle) * gDist
                )
                val zoneColor = when (zone.type) {
                    GeofenceType.HOME -> SafeGreen
                    GeofenceType.SCHOOL -> SafetyBlueLight
                    GeofenceType.PARK -> WarningAmber
                    GeofenceType.OTHER -> BrandNavy600
                }

                drawCircle(
                    color = zoneColor.copy(alpha = 0.15f),
                    radius = 45f * zoomLevel,
                    center = gCenter
                )
                drawCircle(
                    color = zoneColor.copy(alpha = 0.8f),
                    radius = 45f * zoomLevel,
                    center = gCenter,
                    style = Stroke(width = 2f)
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = "🛡️ ${zone.name}",
                    topLeft = Offset(gCenter.x - 30f, gCenter.y - 10f),
                    style = TextStyle(color = zoneColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                )
            }

            // Breadcrumb path trail connecting points
            val trailPoints = listOf(
                Offset(center.x - 80f * zoomLevel, center.y + 40f * zoomLevel),
                Offset(center.x - 40f * zoomLevel, center.y + 10f * zoomLevel),
                Offset(center.x, center.y - 20f * zoomLevel),
                Offset(center.x + 30f * zoomLevel, center.y - 50f * zoomLevel)
            )
            for (p in 0 until trailPoints.size - 1) {
                drawLine(
                    color = SafetyBluePrimary.copy(alpha = 0.5f),
                    start = trailPoints[p],
                    end = trailPoints[p + 1],
                    strokeWidth = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
                drawCircle(
                    color = SafetyBlueLight.copy(alpha = 0.7f),
                    radius = 3.5f,
                    center = trailPoints[p]
                )
            }

            // Draw all 10 child devices
            devices.forEachIndexed { index, dev ->
                val isSelected = dev.id == selectedDeviceId
                val angle = (index * (360f / devices.size.coerceAtLeast(1))) * (Math.PI / 180f).toFloat()
                val dist = (80f + (index % 3) * 35f) * zoomLevel
                val devPos = Offset(
                    center.x + kotlin.math.cos(angle) * dist,
                    center.y + kotlin.math.sin(angle) * dist
                )

                val nodeColor = Color(dev.avatarBgColor)

                if (isSelected) {
                    // Pulsing beacon ring
                    drawCircle(
                        color = SafetyBlueLight.copy(alpha = pulseAlpha),
                        radius = pulseRadius * zoomLevel,
                        center = devPos,
                        style = Stroke(width = 2.5f)
                    )
                }

                // Device Node Circle
                drawCircle(
                    color = if (dev.emergencySosActive) EmergencyRed else nodeColor,
                    radius = if (isSelected) 14f else 10f,
                    center = devPos
                )
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 14f else 10f,
                    center = devPos,
                    style = Stroke(width = 2f)
                )

                // Name tag
                val labelText = if (dev.emergencySosActive) "🚨 ${dev.name} (SOS)" else "${dev.name} (${dev.batteryLevel}%)"
                drawText(
                    textMeasurer = textMeasurer,
                    text = labelText,
                    topLeft = Offset(devPos.x - 30f, devPos.y + 14f),
                    style = TextStyle(
                        color = if (dev.emergencySosActive) EmergencyRed else Color.White,
                        fontSize = if (isSelected) 11.sp else 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                )
            }
        }

        // Overlay status badges
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BrandNavy800.copy(alpha = 0.9f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(SafeGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${devices.count { it.isOnline }}/10 Devices Tracked",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Map controls (+, -, center)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2.0f) },
                containerColor = BrandNavy700,
                contentColor = Color.White,
                modifier = Modifier.size(36.dp).testTag("map_zoom_in_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
            }
            SmallFloatingActionButton(
                onClick = { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.6f) },
                containerColor = BrandNavy700,
                contentColor = Color.White,
                modifier = Modifier.size(36.dp).testTag("map_zoom_out_button")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
            }
            SmallFloatingActionButton(
                onClick = { zoomLevel = 1.0f },
                containerColor = SafetyBluePrimary,
                contentColor = Color.White,
                modifier = Modifier.size(36.dp).testTag("map_center_button")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center", modifier = Modifier.size(18.dp))
            }
        }
    }
}
