package com.example.model

enum class LogSeverity {
    INFO, WARNING, ALERT
}

enum class ActivityType {
    APP_LAUNCH,
    SCREEN_TIME_WARNING,
    BEDTIME_LOCKED,
    GEOFENCE_ENTER,
    GEOFENCE_EXIT,
    EMERGENCY_LOCK,
    EMERGENCY_UNLOCK,
    SOS_PANIC,
    STREAM_CONSENT_REQUESTED,
    STREAM_STARTED,
    AUTHORIZED_PHOTO_PICKED
}

data class ActivityLogEntry(
    val id: String,
    val deviceId: String,
    val timestamp: String,
    val title: String,
    val description: String,
    val type: ActivityType,
    val severity: LogSeverity
)
