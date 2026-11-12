package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.AppRoleMode
import com.example.data.SafeGuardRepository
import com.example.ui.child.ChildLockOverlay
import com.example.ui.child.ChildSafetyScreen
import com.example.ui.components.ParentPinDialog
import com.example.ui.parent.ParentDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SafeGuardApp()
                }
            }
        }
    }
}

@Composable
fun SafeGuardApp() {
    val repository = remember { SafeGuardRepository() }
    val currentRole by repository.currentRole.collectAsState()
    val devices by repository.devices.collectAsState()
    val selectedDeviceId by repository.selectedDeviceId.collectAsState()
    val parentPin by repository.parentPin.collectAsState()
    val screenTimeMap by repository.screenTimeMap.collectAsState()
    val streamSession by repository.streamSession.collectAsState()

    val currentChild = devices.find { it.id == selectedDeviceId } ?: devices.first()
    val currentScreenTime = screenTimeMap[currentChild.id] ?: com.example.model.DeviceScreenTimeState()

    var showPinDialogForParentSwitch by remember { mutableStateOf(false) }
    var showPinDialogForUnlock by remember { mutableStateOf(false) }

    // Stream Session Tick Loop (Every 1 second)
    LaunchedEffect(streamSession.state) {
        while (streamSession.state == com.example.model.StreamConsentState.ACTIVE_STREAMING) {
            delay(1000)
            repository.tickStreamTimer()
        }
    }

    when (currentRole) {
        AppRoleMode.PARENT -> {
            ParentDashboardScreen(
                repository = repository,
                devices = devices,
                selectedDeviceId = selectedDeviceId,
                onSwitchToChildMode = {
                    repository.setAppRole(AppRoleMode.CHILD)
                }
            )
        }

        AppRoleMode.CHILD -> {
            if (currentChild.isLocked) {
                ChildLockOverlay(
                    childName = currentChild.name,
                    lockReason = currentChild.lockReason,
                    onParentUnlockClick = {
                        showPinDialogForUnlock = true
                    }
                )
            } else {
                ChildSafetyScreen(
                    repository = repository,
                    child = currentChild,
                    screenTimeState = currentScreenTime,
                    streamSession = streamSession,
                    onSwitchToParentMode = {
                        showPinDialogForParentSwitch = true
                    }
                )
            }
        }
    }

    // PIN Authentication Dialogs
    if (showPinDialogForParentSwitch) {
        ParentPinDialog(
            correctPin = parentPin,
            onSuccess = {
                showPinDialogForParentSwitch = false
                repository.setAppRole(AppRoleMode.PARENT)
            },
            onDismiss = {
                showPinDialogForParentSwitch = false
            },
            title = "Parent Mode Verification",
            subtitle = "Enter 4-digit PIN to exit Child Mode (Default: 1234)"
        )
    }

    if (showPinDialogForUnlock) {
        ParentPinDialog(
            correctPin = parentPin,
            onSuccess = {
                showPinDialogForUnlock = false
                repository.toggleDeviceLock(currentChild.id, false)
            },
            onDismiss = {
                showPinDialogForUnlock = false
            },
            title = "Parent Unlock",
            subtitle = "Enter 4-digit PIN to bypass device lock (Default: 1234)"
        )
    }
}
