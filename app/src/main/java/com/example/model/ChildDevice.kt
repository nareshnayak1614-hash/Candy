package com.example.model

data class ChildDevice(
    val id: String,
    val name: String,
    val age: Int,
    val deviceModel: String,
    val avatarBgColor: Long = 0xFF0284C7,
    val isOnline: Boolean = true,
    val batteryLevel: Int = 85,
    val isCharging: Boolean = false,
    val wifiSsid: String = "Home_5G_Secure",
    val wifiSignalBars: Int = 4,
    val isLocked: Boolean = false,
    val lockReason: String = "",
    val emergencySosActive: Boolean = false,
    val lastActiveTime: String = "Just now",
    val pairingCode: String = "SG-8421",
    val currentActiveApp: String = "Duolingo",
    val todayUsedMinutes: Int = 85,
    val dailyLimitMinutes: Int = 150
)
