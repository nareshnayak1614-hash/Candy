package com.example.model

enum class LiveStreamType(val title: String) {
    CAMERA("Live Camera Check"),
    MICROPHONE("Live Audio Check"),
    SCREEN_MIRROR("Screen Mirroring")
}

enum class StreamConsentState {
    IDLE,
    AWAITING_CHILD_CONSENT,
    CONSENT_GRANTED,
    CONSENT_DENIED,
    ACTIVE_STREAMING,
    AUTO_TERMINATED
}

data class LiveStreamSession(
    val sessionType: LiveStreamType = LiveStreamType.CAMERA,
    val state: StreamConsentState = StreamConsentState.IDLE,
    val targetDeviceId: String = "",
    val durationSecondsTotal: Int = 180, // 3-minute safety limit
    val secondsRemaining: Int = 180,
    val cameraFacingFront: Boolean = false,
    val audioMuted: Boolean = false,
    val audioLevelPeak: Float = 0.45f,
    val frameRate: Int = 30,
    val isChildConsentGiven: Boolean = false
)
