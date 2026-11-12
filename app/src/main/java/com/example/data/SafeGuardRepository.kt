package com.example.data

import com.example.model.ActivityLogEntry
import com.example.model.ActivityType
import com.example.model.AppCategory
import com.example.model.AppUsageLimit
import com.example.model.BreadcrumbType
import com.example.model.ChildDevice
import com.example.model.DeviceLocationState
import com.example.model.DeviceScreenTimeState
import com.example.model.GeofenceType
import com.example.model.GeofenceZone
import com.example.model.LiveStreamSession
import com.example.model.LiveStreamType
import com.example.model.LocationBreadcrumb
import com.example.model.LogSeverity
import com.example.model.StreamConsentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppRoleMode {
    PARENT, CHILD
}

class SafeGuardRepository {

    private val _currentRole = MutableStateFlow(AppRoleMode.PARENT)
    val currentRole: StateFlow<AppRoleMode> = _currentRole.asStateFlow()

    private val _parentPin = MutableStateFlow("1234")
    val parentPin: StateFlow<String> = _parentPin.asStateFlow()

    // 10 Connected Devices initially configured
    private val initialDevices = listOf(
        ChildDevice("dev_1", "Liam", 12, "Google Pixel 8", 0xFF0284C7, true, 84, false, "Lincoln_High_WiFi", 4, false, "", false, "Just now", "SG-8421", "Duolingo", 85, 150),
        ChildDevice("dev_2", "Emma", 9, "Samsung Galaxy Tab S8", 0xFF10B981, true, 92, true, "Home_5G_Secure", 4, false, "", false, "3m ago", "SG-3319", "Khan Academy Kids", 40, 120),
        ChildDevice("dev_3", "Noah", 14, "Samsung Galaxy S23", 0xFFF59E0B, true, 48, false, "Sunset_Public_Net", 3, false, "", false, "1m ago", "SG-7740", "YouTube", 140, 150),
        ChildDevice("dev_4", "Olivia", 11, "Motorola Edge 40", 0xFF8B5CF6, true, 76, false, "Lincoln_High_WiFi", 4, false, "", false, "5m ago", "SG-1928", "Roblox", 65, 120),
        ChildDevice("dev_5", "Lucas", 8, "Amazon Fire HD Kids", 0xFFEC4899, false, 60, false, "Home_5G_Secure", 2, true, "Bedtime limit reached", false, "2h ago", "SG-5501", "Minecraft", 90, 90),
        ChildDevice("dev_6", "Sophia", 13, "Google Pixel 7a", 0xFF06B6D4, true, 34, false, "City_Library_Guest", 3, false, "", false, "Just now", "SG-6622", "Spotify", 110, 120),
        ChildDevice("dev_7", "Ethan", 10, "Xiaomi Redmi Note 13", 0xFF14B8A6, true, 88, false, "Home_5G_Secure", 4, false, "", false, "10m ago", "SG-4481", "Google Classroom", 50, 120),
        ChildDevice("dev_8", "Ava", 15, "Nothing Phone (2)", 0xFF6366F1, true, 65, false, "Sports_Club_Guest", 4, false, "", false, "4m ago", "SG-9934", "Instagram", 90, 180),
        ChildDevice("dev_9", "Mason", 7, "Samsung Galaxy A15", 0xFF84CC16, true, 95, true, "Home_5G_Secure", 4, false, "", false, "15m ago", "SG-2287", "ScratchJr", 15, 60),
        ChildDevice("dev_10", "Mia", 12, "Google Pixel 6a", 0xFFF97316, true, 52, false, "LTE / Cellular", 3, false, "", false, "2m ago", "SG-7104", "Kindle", 75, 120)
    )

    private val _devices = MutableStateFlow(initialDevices)
    val devices: StateFlow<List<ChildDevice>> = _devices.asStateFlow()

    private val _selectedDeviceId = MutableStateFlow("dev_1")
    val selectedDeviceId: StateFlow<String> = _selectedDeviceId.asStateFlow()

    // Per-device Screen Time state
    private val _screenTimeMap = MutableStateFlow<Map<String, DeviceScreenTimeState>>(
        initialDevices.associate { it.id to DeviceScreenTimeState(dailyLimitMinutes = it.dailyLimitMinutes, todayUsedMinutes = it.todayUsedMinutes) }
    )
    val screenTimeMap: StateFlow<Map<String, DeviceScreenTimeState>> = _screenTimeMap.asStateFlow()

    // Per-device Location state
    private val _locationMap = MutableStateFlow<Map<String, DeviceLocationState>>(
        initialDevices.associate { it.id to DeviceLocationState() }
    )
    val locationMap: StateFlow<Map<String, DeviceLocationState>> = _locationMap.asStateFlow()

    // Active Live Streaming Session
    private val _streamSession = MutableStateFlow(LiveStreamSession())
    val streamSession: StateFlow<LiveStreamSession> = _streamSession.asStateFlow()

    // Activity Logs
    private val _logs = MutableStateFlow<List<ActivityLogEntry>>(
        listOf(
            ActivityLogEntry("log_1", "dev_1", "08:45 AM", "Geofence Entered", "Liam entered Lincoln High School safe zone", ActivityType.GEOFENCE_ENTER, LogSeverity.INFO),
            ActivityLogEntry("log_2", "dev_3", "10:15 AM", "Screen Time 90% Warning", "Noah used 135 mins of daily 150m allowance", ActivityType.SCREEN_TIME_WARNING, LogSeverity.WARNING),
            ActivityLogEntry("log_3", "dev_5", "07:30 AM", "Bedtime Scheduled Lock", "Lucas's device locked for study hours", ActivityType.BEDTIME_LOCKED, LogSeverity.INFO),
            ActivityLogEntry("log_4", "dev_1", "11:20 AM", "Duolingo App Launched", "Liam opened Duolingo (Education category)", ActivityType.APP_LAUNCH, LogSeverity.INFO),
            ActivityLogEntry("log_5", "dev_2", "12:00 PM", "Charging Connected", "Emma's Galaxy Tab is charging (92%)", ActivityType.APP_LAUNCH, LogSeverity.INFO)
        )
    )
    val logs: StateFlow<List<ActivityLogEntry>> = _logs.asStateFlow()

    // Pending Extra Time Requests from child devices
    private val _pendingTimeRequests = MutableStateFlow<List<Pair<String, Int>>>(listOf("dev_1" to 30))
    val pendingTimeRequests: StateFlow<List<Pair<String, Int>>> = _pendingTimeRequests.asStateFlow()

    // Role switching
    fun setAppRole(role: AppRoleMode) {
        _currentRole.value = role
    }

    fun verifyParentPin(pin: String): Boolean {
        return pin == _parentPin.value
    }

    fun updateParentPin(newPin: String) {
        _parentPin.value = newPin
    }

    fun selectDevice(deviceId: String) {
        _selectedDeviceId.value = deviceId
    }

    // Emergency Lock Controls
    fun toggleDeviceLock(deviceId: String, lock: Boolean, reason: String = "Locked by Parent") {
        _devices.update { list ->
            list.map {
                if (it.id == deviceId) it.copy(isLocked = lock, lockReason = if (lock) reason else "") else it
            }
        }
        addLog(
            deviceId = deviceId,
            title = if (lock) "Device Remotely Locked" else "Device Remotely Unlocked",
            description = if (lock) "Parent engaged emergency device lock ($reason)" else "Parent unlocked device",
            type = if (lock) ActivityType.EMERGENCY_LOCK else ActivityType.EMERGENCY_UNLOCK,
            severity = if (lock) LogSeverity.ALERT else LogSeverity.INFO
        )
    }

    fun lockAllDevices(lock: Boolean) {
        _devices.update { list ->
            list.map { it.copy(isLocked = lock, lockReason = if (lock) "All Devices Locked for Family Time" else "") }
        }
        addLog(
            deviceId = "all",
            title = if (lock) "All 10 Devices Locked" else "All 10 Devices Unlocked",
            description = if (lock) "Emergency master lock triggered across all 10 child devices" else "Master unlock sent",
            type = if (lock) ActivityType.EMERGENCY_LOCK else ActivityType.EMERGENCY_UNLOCK,
            severity = if (lock) LogSeverity.ALERT else LogSeverity.INFO
        )
    }

    // Screen Time Management
    fun updateDailyLimit(deviceId: String, limitMinutes: Int) {
        _devices.update { list ->
            list.map { if (it.id == deviceId) it.copy(dailyLimitMinutes = limitMinutes) else it }
        }
        _screenTimeMap.update { map ->
            val current = map[deviceId] ?: DeviceScreenTimeState()
            map + (deviceId to current.copy(dailyLimitMinutes = limitMinutes))
        }
        addLog(deviceId, "Daily Limit Changed", "New daily limit set to ${limitMinutes / 60}h ${limitMinutes % 60}m", ActivityType.SCREEN_TIME_WARNING, LogSeverity.INFO)
    }

    fun toggleAppBlock(deviceId: String, appId: String, blocked: Boolean) {
        _screenTimeMap.update { map ->
            val current = map[deviceId] ?: DeviceScreenTimeState()
            val updatedLimits = current.appLimits.map {
                if (it.id == appId) it.copy(isBlocked = blocked) else it
            }
            map + (deviceId to current.copy(appLimits = updatedLimits))
        }
        addLog(deviceId, "App Rule Changed", "App status changed to ${if (blocked) "Blocked" else "Allowed"}", ActivityType.APP_LAUNCH, LogSeverity.WARNING)
    }

    fun updateAppLimit(deviceId: String, appId: String, limitMinutes: Int) {
        _screenTimeMap.update { map ->
            val current = map[deviceId] ?: DeviceScreenTimeState()
            val updatedLimits = current.appLimits.map {
                if (it.id == appId) it.copy(dailyLimitMinutes = limitMinutes) else it
            }
            map + (deviceId to current.copy(appLimits = updatedLimits))
        }
    }

    fun toggleBedtimeSchedule(deviceId: String, enabled: Boolean) {
        _screenTimeMap.update { map ->
            val current = map[deviceId] ?: DeviceScreenTimeState()
            map + (deviceId to current.copy(bedtimeSchedule = current.bedtimeSchedule.copy(enabled = enabled)))
        }
    }

    fun toggleStudySchedule(deviceId: String, enabled: Boolean) {
        _screenTimeMap.update { map ->
            val current = map[deviceId] ?: DeviceScreenTimeState()
            map + (deviceId to current.copy(studySchedule = current.studySchedule.copy(enabled = enabled)))
        }
    }

    // Geofencing & Location
    fun addGeofence(deviceId: String, name: String, type: GeofenceType, radiusMeters: Float) {
        val currentLoc = _locationMap.value[deviceId] ?: DeviceLocationState()
        val newZone = GeofenceZone(
            id = "geo_${UUID.randomUUID().toString().take(6)}",
            name = name,
            latitude = currentLoc.latitude + (Math.random() - 0.5) * 0.005,
            longitude = currentLoc.longitude + (Math.random() - 0.5) * 0.005,
            radiusMeters = radiusMeters,
            type = type,
            isInsideCurrently = true
        )
        _locationMap.update { map ->
            val current = map[deviceId] ?: DeviceLocationState()
            map + (deviceId to current.copy(geofences = current.geofences + newZone))
        }
        addLog(deviceId, "Geofence Created", "Created safe zone '$name' with ${radiusMeters.toInt()}m radius", ActivityType.GEOFENCE_ENTER, LogSeverity.INFO)
    }

    fun removeGeofence(deviceId: String, geofenceId: String) {
        _locationMap.update { map ->
            val current = map[deviceId] ?: DeviceLocationState()
            map + (deviceId to current.copy(geofences = current.geofences.filter { it.id != geofenceId }))
        }
    }

    // Live Streaming & Screen Mirroring
    fun startLiveStreamRequest(type: LiveStreamType, deviceId: String) {
        _streamSession.value = LiveStreamSession(
            sessionType = type,
            state = StreamConsentState.AWAITING_CHILD_CONSENT,
            targetDeviceId = deviceId,
            durationSecondsTotal = 180,
            secondsRemaining = 180,
            isChildConsentGiven = false
        )
        addLog(deviceId, "Live ${type.title} Requested", "Awaiting child on-device explicit consent", ActivityType.STREAM_CONSENT_REQUESTED, LogSeverity.WARNING)
    }

    fun grantChildStreamConsent() {
        _streamSession.update {
            it.copy(
                state = StreamConsentState.ACTIVE_STREAMING,
                isChildConsentGiven = true
            )
        }
        addLog(_streamSession.value.targetDeviceId, "Child Granted Stream Consent", "Live stream session active with child approval", ActivityType.STREAM_STARTED, LogSeverity.INFO)
    }

    fun denyChildStreamConsent() {
        _streamSession.update {
            it.copy(
                state = StreamConsentState.CONSENT_DENIED,
                isChildConsentGiven = false
            )
        }
        addLog(_streamSession.value.targetDeviceId, "Consent Declined", "Child chose not to allow live stream", ActivityType.STREAM_CONSENT_REQUESTED, LogSeverity.INFO)
    }

    fun stopStreamSession() {
        _streamSession.update {
            it.copy(state = StreamConsentState.AUTO_TERMINATED)
        }
    }

    fun toggleCameraFacing() {
        _streamSession.update { it.copy(cameraFacingFront = !it.cameraFacingFront) }
    }

    fun toggleAudioMute() {
        _streamSession.update { it.copy(audioMuted = !it.audioMuted) }
    }

    fun tickStreamTimer() {
        val current = _streamSession.value
        if (current.state == StreamConsentState.ACTIVE_STREAMING) {
            if (current.secondsRemaining > 1) {
                _streamSession.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
            } else {
                stopStreamSession()
            }
        }
    }

    // Child SOS Panic & Extra Time Request
    fun triggerSos(deviceId: String) {
        _devices.update { list ->
            list.map { if (it.id == deviceId) it.copy(emergencySosActive = true) else it }
        }
        addLog(deviceId, "EMERGENCY SOS TRIGGERED", "Child pressed Emergency SOS Panic Button! Live GPS transmitted.", ActivityType.SOS_PANIC, LogSeverity.ALERT)
    }

    fun cancelSos(deviceId: String) {
        _devices.update { list ->
            list.map { if (it.id == deviceId) it.copy(emergencySosActive = false) else it }
        }
        addLog(deviceId, "SOS Resolved", "Emergency alert acknowledged and marked resolved by Parent", ActivityType.SOS_PANIC, LogSeverity.INFO)
    }

    fun requestExtraTime(deviceId: String, minutes: Int) {
        _pendingTimeRequests.update { it + (deviceId to minutes) }
        addLog(deviceId, "Extra Time Requested", "Child requested $minutes additional minutes of screen time", ActivityType.SCREEN_TIME_WARNING, LogSeverity.INFO)
    }

    fun grantPendingTime(deviceId: String, minutes: Int) {
        _pendingTimeRequests.update { it.filter { p -> p.first != deviceId } }
        _devices.update { list ->
            list.map { if (it.id == deviceId) it.copy(dailyLimitMinutes = it.dailyLimitMinutes + minutes) else it }
        }
        _screenTimeMap.update { map ->
            val current = map[deviceId] ?: DeviceScreenTimeState()
            map + (deviceId to current.copy(dailyLimitMinutes = current.dailyLimitMinutes + minutes))
        }
        addLog(deviceId, "Extra Time Approved", "Parent approved +$minutes mins of screen time", ActivityType.SCREEN_TIME_WARNING, LogSeverity.INFO)
    }

    // Pair a new device (up to 10 max)
    fun pairNewDevice(name: String, age: Int, model: String): Boolean {
        if (_devices.value.size >= 10) return false
        val newIndex = _devices.value.size + 1
        val newDev = ChildDevice(
            id = "dev_$newIndex",
            name = name,
            age = age,
            deviceModel = model,
            avatarBgColor = 0xFF0284C7,
            isOnline = true,
            batteryLevel = 100,
            isCharging = false,
            wifiSsid = "Home_5G_Secure",
            wifiSignalBars = 4,
            isLocked = false,
            pairingCode = "SG-${(1000..9999).random()}",
            currentActiveApp = "Home Screen",
            todayUsedMinutes = 0,
            dailyLimitMinutes = 120
        )
        _devices.update { it + newDev }
        _screenTimeMap.update { it + (newDev.id to DeviceScreenTimeState(dailyLimitMinutes = 120, todayUsedMinutes = 0)) }
        _locationMap.update { it + (newDev.id to DeviceLocationState()) }
        addLog(newDev.id, "Device Paired", "Successfully paired $name's $model to SafeGuard network", ActivityType.APP_LAUNCH, LogSeverity.INFO)
        return true
    }

    private fun addLog(deviceId: String, title: String, description: String, type: ActivityType, severity: LogSeverity) {
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val newLog = ActivityLogEntry(
            id = "log_${UUID.randomUUID().toString().take(6)}",
            deviceId = deviceId,
            timestamp = timeStr,
            title = title,
            description = description,
            type = type,
            severity = severity
        )
        _logs.update { listOf(newLog) + it.take(49) }
    }
}
