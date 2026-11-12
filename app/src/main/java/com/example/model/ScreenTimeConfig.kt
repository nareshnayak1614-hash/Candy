package com.example.model

data class AppUsageLimit(
    val id: String,
    val appName: String,
    val packageName: String,
    val category: AppCategory,
    val dailyLimitMinutes: Int,
    val usedMinutes: Int,
    val isBlocked: Boolean = false
)

enum class AppCategory(val displayName: String) {
    GAMES("Games"),
    SOCIAL("Social & Video"),
    EDUCATION("Education"),
    PRODUCTIVITY("Productivity"),
    ENTERTAINMENT("Entertainment")
}

data class TimeSchedule(
    val enabled: Boolean,
    val name: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val activeDays: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
) {
    fun formatRange(): String {
        val startAmPm = if (startHour >= 12) "PM" else "AM"
        val displayStartH = if (startHour % 12 == 0) 12 else startHour % 12
        val startMinStr = if (startMinute < 10) "0$startMinute" else "$startMinute"

        val endAmPm = if (endHour >= 12) "PM" else "AM"
        val displayEndH = if (endHour % 12 == 0) 12 else endHour % 12
        val endMinStr = if (endMinute < 10) "0$endMinute" else "$endMinute"

        return "$displayStartH:$startMinStr $startAmPm - $displayEndH:$endMinStr $endAmPm"
    }
}

data class DayUsage(
    val dayLabel: String,
    val totalMinutes: Int,
    val limitMinutes: Int
)

data class DeviceScreenTimeState(
    val dailyLimitMinutes: Int = 150,
    val todayUsedMinutes: Int = 85,
    val bedtimeSchedule: TimeSchedule = TimeSchedule(
        enabled = true,
        name = "Bedtime",
        startHour = 21,
        startMinute = 0,
        endHour = 7,
        endMinute = 0
    ),
    val studySchedule: TimeSchedule = TimeSchedule(
        enabled = true,
        name = "Study Time",
        startHour = 16,
        startMinute = 0,
        endHour = 18,
        endMinute = 0
    ),
    val appLimits: List<AppUsageLimit> = listOf(
        AppUsageLimit("app_yt", "YouTube", "com.google.android.youtube", AppCategory.SOCIAL, 60, 45),
        AppUsageLimit("app_tt", "TikTok", "com.zhiliaoapp.musically", AppCategory.SOCIAL, 30, 28),
        AppUsageLimit("app_rbx", "Roblox", "com.roblox.client", AppCategory.GAMES, 45, 12),
        AppUsageLimit("app_duo", "Duolingo", "com.duolingo", AppCategory.EDUCATION, 90, 20),
        AppUsageLimit("app_cr", "Chrome", "com.android.chrome", AppCategory.PRODUCTIVITY, 60, 15)
    ),
    val weeklyUsage: List<DayUsage> = listOf(
        DayUsage("Mon", 110, 150),
        DayUsage("Tue", 135, 150),
        DayUsage("Wed", 95, 150),
        DayUsage("Thu", 140, 150),
        DayUsage("Fri", 160, 150),
        DayUsage("Sat", 185, 210),
        DayUsage("Sun", 85, 150)
    )
)
