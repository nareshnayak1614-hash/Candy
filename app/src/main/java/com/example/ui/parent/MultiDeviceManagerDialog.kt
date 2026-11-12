package com.example.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Smartphone
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
import com.example.model.ChildDevice
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafetyBluePrimary

@Composable
fun MultiDeviceManagerDialog(
    devices: List<ChildDevice>,
    selectedDeviceId: String,
    onSelectDevice: (String) -> Unit,
    onLockAll: (Boolean) -> Unit,
    onPairNewDevice: (String, Int, String) -> Boolean,
    onDismiss: () -> Unit
) {
    var showPairForm by remember { mutableStateOf(false) }
    var newChildName by remember { mutableStateOf("") }
    var newChildAge by remember { mutableStateOf("10") }
    var newDeviceModel by remember { mutableStateOf("Galaxy Tab A9") }
    var pairSuccessMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Devices, contentDescription = null, tint = SafetyBluePrimary)
                    Column {
                        Text("Connected Devices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${devices.size}/10 Devices Linked", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Batch Actions Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Emergency Master Lock", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Control all 10 child devices at once", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { onLockAll(true) },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = EmergencyRed.copy(alpha = 0.2f), contentColor = EmergencyRed),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("lock_all_devices_button")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock All", fontSize = 11.sp)
                            }
                            FilledTonalButton(
                                onClick = { onLockAll(false) },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = SafeGreen.copy(alpha = 0.2f), contentColor = SafeGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("unlock_all_devices_button")
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Unlock", fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (showPairForm) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SafetyBluePrimary.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Pair New Child Device", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            OutlinedTextField(
                                value = newChildName,
                                onValueChange = { newChildName = it },
                                label = { Text("Child Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newChildAge,
                                    onValueChange = { newChildAge = it },
                                    label = { Text("Age") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = newDeviceModel,
                                    onValueChange = { newDeviceModel = it },
                                    label = { Text("Device Model") },
                                    singleLine = true,
                                    modifier = Modifier.weight(2f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showPairForm = false }) { Text("Cancel") }
                                Button(
                                    onClick = {
                                        if (newChildName.isNotBlank()) {
                                            val success = onPairNewDevice(newChildName.trim(), newChildAge.toIntOrNull() ?: 10, newDeviceModel.trim())
                                            if (success) {
                                                pairSuccessMessage = "Device paired successfully!"
                                                showPairForm = false
                                                newChildName = ""
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("submit_pair_device_button")
                                ) {
                                    Text("Pair & Connect")
                                }
                            }
                        }
                    }
                } else if (devices.size < 10) {
                    OutlinedButton(
                        onClick = { showPairForm = true },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("open_pair_form_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pair New Device (+)")
                    }
                }

                // List of connected devices
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(devices) { dev ->
                        val isSelected = dev.id == selectedDeviceId
                        Surface(
                            onClick = {
                                onSelectDevice(dev.id)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SafetyBluePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(dev.avatarBgColor)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dev.name.take(1),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${dev.name} (${dev.age}y)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (dev.isLocked) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = EmergencyRed, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                        Text(
                                            text = "${dev.deviceModel} • ${dev.batteryLevel}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (dev.isOnline) SafeGreen else MaterialTheme.colorScheme.outline)
                                    )
                                    Text(
                                        text = if (dev.isOnline) "Online" else "Offline",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (dev.isOnline) SafeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("close_device_manager_button")) {
                Text("Done")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
