package com.example.ui.parent

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
import com.example.model.*
import com.example.ui.components.MapCanvas
import com.example.ui.theme.*

@Composable
fun LocationTab(
    child: ChildDevice,
    devices: List<ChildDevice>,
    locationState: DeviceLocationState,
    onSelectDevice: (String) -> Unit,
    onAddGeofence: (String, GeofenceType, Float) -> Unit,
    onRemoveGeofence: (String) -> Unit,
    onResolveSos: (String) -> Unit
) {
    var showAddGeofenceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Emergency SOS Alert Banner if child triggered SOS
        if (child.emergencySosActive) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmergencyRed),
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
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                            Column {
                                Text("EMERGENCY SOS ACTIVE", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("${child.name} pressed SOS panic button!", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                            }
                        }
                        Button(
                            onClick = { onResolveSos(child.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = EmergencyRed),
                            modifier = Modifier.testTag("resolve_sos_button")
                        ) {
                            Text("Acknowledge", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Radar / Interactive Map
        item {
            MapCanvas(
                devices = devices,
                selectedDeviceId = child.id,
                locationState = locationState,
                onSelectDevice = onSelectDevice
            )
        }

        // Real-Time GPS Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SafetyBluePrimary)
                            Text(
                                text = "Current Position",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SafeGreen))
                            Text(locationState.lastUpdated, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Text(
                        text = locationState.address,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Coordinates", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%.4f", locationState.latitude)}, ${String.format("%.4f", locationState.longitude)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Accuracy & Speed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("±${locationState.accuracyMeters}m • ${locationState.speedKmh.toInt()} km/h", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // Safe-Zone Geofencing Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Safe-Zone Geofences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Instant entry and exit notifications", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalButton(
                    onClick = { showAddGeofenceDialog = true },
                    modifier = Modifier.testTag("add_geofence_button")
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Zone", fontSize = 12.sp)
                }
            }
        }

        // Geofences List
        items(locationState.geofences) { zone ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (zone.type) {
                                        GeofenceType.HOME -> SafeGreen.copy(alpha = 0.2f)
                                        GeofenceType.SCHOOL -> SafetyBluePrimary.copy(alpha = 0.2f)
                                        else -> WarningAmber.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (zone.type) {
                                    GeofenceType.HOME -> Icons.Default.Home
                                    GeofenceType.SCHOOL -> Icons.Default.School
                                    else -> Icons.Default.Park
                                },
                                contentDescription = null,
                                tint = when (zone.type) {
                                    GeofenceType.HOME -> SafeGreen
                                    GeofenceType.SCHOOL -> SafetyBluePrimary
                                    else -> WarningAmber
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(zone.name, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                if (zone.isInsideCurrently) {
                                    Surface(
                                        color = SafeGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Inside Now", style = MaterialTheme.typography.labelSmall, color = SafeGreen, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text(
                                "${zone.radiusMeters.toInt()}m radius • Alerts on Enter & Exit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { onRemoveGeofence(zone.id) },
                        modifier = Modifier.size(32.dp).testTag("delete_geofence_${zone.id}")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // Location History Breadcrumbs Timeline
        item {
            Text("Today's Location History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(locationState.breadcrumbs) { bc ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when (bc.type) {
                                    BreadcrumbType.GEOFENCE_ENTER -> SafeGreen
                                    BreadcrumbType.GEOFENCE_EXIT -> WarningAmber
                                    BreadcrumbType.SOS_ALERT -> EmergencyRed
                                    else -> SafetyBluePrimary
                                }
                            )
                    )
                    Box(modifier = Modifier.width(2.dp).height(38.dp).background(MaterialTheme.colorScheme.outlineVariant))
                }
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(bc.detail, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text(bc.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(bc.address, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // Add Geofence Dialog
    if (showAddGeofenceDialog) {
        var zoneName by remember { mutableStateOf("") }
        var zoneType by remember { mutableStateOf(GeofenceType.HOME) }
        var zoneRadius by remember { mutableFloatStateOf(200f) }

        AlertDialog(
            onDismissRequest = { showAddGeofenceDialog = false },
            title = { Text("Create Safe-Zone Geofence") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = zoneName,
                        onValueChange = { zoneName = it },
                        label = { Text("Zone Name (e.g. Grandparents, Dance Studio)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Zone Type", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(GeofenceType.HOME to "Home", GeofenceType.SCHOOL to "School", GeofenceType.PARK to "Park").forEach { (type, label) ->
                            FilterChip(
                                selected = zoneType == type,
                                onClick = { zoneType = type },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("Radius: ${zoneRadius.toInt()} meters", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = zoneRadius,
                        onValueChange = { zoneRadius = it },
                        valueRange = 50f..1000f,
                        steps = 9
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (zoneName.isNotBlank()) {
                            onAddGeofence(zoneName.trim(), zoneType, zoneRadius)
                            showAddGeofenceDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_geofence_button")
                ) {
                    Text("Save Geofence")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGeofenceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
