package com.example.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.AppRoleMode
import com.example.data.SafeGuardRepository
import com.example.model.ChildDevice
import com.example.ui.components.SetupGuideDialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    repository: SafeGuardRepository,
    devices: List<ChildDevice>,
    selectedDeviceId: String,
    onSwitchToChildMode: () -> Unit
) {
    val activeChild = devices.find { it.id == selectedDeviceId } ?: devices.first()
    val screenTimeMap by repository.screenTimeMap.collectAsState()
    val locationMap by repository.locationMap.collectAsState()
    val streamSession by repository.streamSession.collectAsState()
    val logs by repository.logs.collectAsState()
    val pendingRequests by repository.pendingTimeRequests.collectAsState()

    val activeScreenTime = screenTimeMap[activeChild.id] ?: com.example.model.DeviceScreenTimeState()
    val activeLocation = locationMap[activeChild.id] ?: com.example.model.DeviceLocationState()
    val pendingMinutes = pendingRequests.find { it.first == activeChild.id }?.second

    var currentTab by remember { mutableIntStateOf(0) }
    var showDeviceManager by remember { mutableStateOf(false) }
    var showSetupGuide by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SafetyBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("SafeGuard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Parent Dashboard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = {
                    // 10 Connected Devices Manager Button
                    FilledTonalButton(
                        onClick = { showDeviceManager = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("open_device_manager_top_button")
                    ) {
                        Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${devices.size} Devices", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Firebase Guide Button
                    IconButton(
                        onClick = { showSetupGuide = true },
                        modifier = Modifier.testTag("open_setup_guide_button")
                    ) {
                        Icon(Icons.Default.IntegrationInstructions, contentDescription = "Firebase Guide")
                    }

                    // Switch to Child App Mode Button
                    IconButton(
                        onClick = onSwitchToChildMode,
                        modifier = Modifier.testTag("switch_to_child_mode_button")
                    ) {
                        Icon(Icons.Default.Face, contentDescription = "Switch to Child Mode", tint = SafetyBluePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Screen Time") },
                    label = { Text("Limits", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_limits_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Location") },
                    label = { Text("Location", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_location_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (streamSession.state == com.example.model.StreamConsentState.ACTIVE_STREAMING) {
                                    Badge(containerColor = EmergencyRed)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "Live Check")
                        }
                    },
                    label = { Text("Live Check", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_live_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Device Health") },
                    label = { Text("Device", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_device_tab")
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Horizontal Device Strip (Fast Switching between all 10 child devices)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(devices) { dev ->
                    val isSelected = dev.id == activeChild.id
                    val devColor = Color(dev.avatarBgColor)

                    Surface(
                        onClick = { repository.selectDevice(dev.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) SafetyBluePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SafetyBluePrimary) else null,
                        modifier = Modifier.testTag("select_device_${dev.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(devColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dev.name.take(1),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dev.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    if (dev.emergencySosActive) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(EmergencyRed))
                                    }
                                }
                                Text(
                                    text = "${dev.batteryLevel}% • ${dev.deviceModel.take(8)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Tab Content
            when (currentTab) {
                0 -> ScreenTimeTab(
                    child = activeChild,
                    screenTimeState = activeScreenTime,
                    pendingExtraMinutes = pendingMinutes,
                    onUpdateDailyLimit = { repository.updateDailyLimit(activeChild.id, it) },
                    onToggleAppBlock = { appId, blocked -> repository.toggleAppBlock(activeChild.id, appId, blocked) },
                    onUpdateAppLimit = { appId, limit -> repository.updateAppLimit(activeChild.id, appId, limit) },
                    onToggleBedtime = { repository.toggleBedtimeSchedule(activeChild.id, it) },
                    onToggleStudyTime = { repository.toggleStudySchedule(activeChild.id, it) },
                    onGrantExtraTime = { repository.grantPendingTime(activeChild.id, it) }
                )
                1 -> LocationTab(
                    child = activeChild,
                    devices = devices,
                    locationState = activeLocation,
                    onSelectDevice = { repository.selectDevice(it) },
                    onAddGeofence = { name, type, radius -> repository.addGeofence(activeChild.id, name, type, radius) },
                    onRemoveGeofence = { repository.removeGeofence(activeChild.id, it) },
                    onResolveSos = { repository.cancelSos(it) }
                )
                2 -> LiveStreamTab(
                    child = activeChild,
                    session = streamSession,
                    onRequestStream = { repository.startLiveStreamRequest(it, activeChild.id) },
                    onStopStream = { repository.stopStreamSession() },
                    onToggleCameraFacing = { repository.toggleCameraFacing() },
                    onToggleAudioMute = { repository.toggleAudioMute() },
                    onSimulateChildGrantConsent = { repository.grantChildStreamConsent() }
                )
                3 -> DeviceManagementTab(
                    child = activeChild,
                    logs = logs,
                    onToggleLock = { repository.toggleDeviceLock(activeChild.id, it) },
                    onRingDevice = { /* Ring Siren */ }
                )
            }
        }
    }

    if (showDeviceManager) {
        MultiDeviceManagerDialog(
            devices = devices,
            selectedDeviceId = activeChild.id,
            onSelectDevice = { repository.selectDevice(it) },
            onLockAll = { repository.lockAllDevices(it) },
            onPairNewDevice = { name, age, model -> repository.pairNewDevice(name, age, model) },
            onDismiss = { showDeviceManager = false }
        )
    }

    if (showSetupGuide) {
        SetupGuideDialog(
            onDismiss = { showSetupGuide = false }
        )
    }
}
