package com.example.ui.parent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.model.ActivityLogEntry
import com.example.model.ChildDevice
import com.example.model.LogSeverity
import com.example.ui.theme.*

@Composable
fun DeviceManagementTab(
    child: ChildDevice,
    logs: List<ActivityLogEntry>,
    onToggleLock: (Boolean) -> Unit,
    onRingDevice: () -> Unit
) {
    var isRinging by remember { mutableStateOf(false) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var mediaName by remember { mutableStateOf<String?>(null) }

    // Compliant Android Photo Picker (zero-permission, Google Play policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            mediaName = "Selected Photo / Media (Authorized via Android Photo Picker)"
        }
    }

    // Compliant Android Storage Access Framework (SAF) Document Picker
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            mediaName = "Selected Document (Authorized via Android SAF)"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Emergency Lock Control Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (child.isLocked) EmergencyRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (child.isLocked) EmergencyRed else SafeGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (child.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (child.isLocked) Color.White else SafeGreen
                                )
                            }
                            Column {
                                Text(
                                    text = if (child.isLocked) "Device is Locked" else "Device is Unlocked",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (child.isLocked) EmergencyRed else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (child.isLocked) "Emergency lock active: Only 911 calls allowed" else "Normal operation allowed according to schedules",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onToggleLock(!child.isLocked) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (child.isLocked) SafeGreen else EmergencyRed
                            ),
                            modifier = Modifier.weight(1f).testTag("toggle_device_lock_button")
                        ) {
                            Icon(if (child.isLocked) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (child.isLocked) "Unlock Device" else "Emergency Lock")
                        }

                        OutlinedButton(
                            onClick = {
                                isRinging = true
                                onRingDevice()
                            },
                            modifier = Modifier.testTag("ring_device_button")
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isRinging) "Ringing..." else "Ring Siren")
                        }
                    }
                }
            }
        }

        // Hardware & Network Health Grid
        item {
            Text("Hardware & Connectivity Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Battery Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Battery", style = MaterialTheme.typography.labelMedium)
                            Icon(
                                imageVector = if (child.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                contentDescription = null,
                                tint = if (child.batteryLevel < 20) EmergencyRed else SafeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${child.batteryLevel}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (child.isCharging) "Charging (Fast)" else "Discharging • Good health",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Wi-Fi / Cellular Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Network", style = MaterialTheme.typography.labelMedium)
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = SafetyBluePrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(child.wifiSsid.take(12), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Strong Signal (4/4 bars) • Wi-Fi 6",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Compliant File & Media Monitoring Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.FolderShared, contentDescription = null, tint = SafetyBluePrimary)
                        Column {
                            Text("Authorized Files & Media Inspection", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Google Play compliant zero-permission system pickers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Text(
                        text = "Android strictly protects user file privacy. To inspect school work, photos, or documents, child-authorized files can be selected via the system photo and document picker without unrestricted disk scraping.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("browse_photos_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inspect Photo", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                docPickerLauncher.launch(arrayOf("application/pdf", "text/*"))
                            },
                            modifier = Modifier.weight(1f).testTag("browse_docs_button")
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inspect Doc", fontSize = 11.sp)
                        }
                    }

                    if (selectedMediaUri != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SafeGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(16.dp))
                                Text(mediaName ?: "Authorized file loaded", style = MaterialTheme.typography.labelSmall, color = SafeGreen)
                            }
                        }
                    }
                }
            }
        }

        // Device Activity Logs
        item {
            Text("Device Security & Activity Timeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(logs) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                when (log.severity) {
                                    LogSeverity.ALERT -> EmergencyRed.copy(alpha = 0.2f)
                                    LogSeverity.WARNING -> WarningAmber.copy(alpha = 0.2f)
                                    LogSeverity.INFO -> SafetyBluePrimary.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (log.severity) {
                                LogSeverity.ALERT -> Icons.Default.Warning
                                LogSeverity.WARNING -> Icons.Default.PriorityHigh
                                LogSeverity.INFO -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = when (log.severity) {
                                LogSeverity.ALERT -> EmergencyRed
                                LogSeverity.WARNING -> WarningAmber
                                LogSeverity.INFO -> SafetyBluePrimary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(log.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(log.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(log.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
