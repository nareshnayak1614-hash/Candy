package com.example.model

enum class GeofenceType {
    HOME, SCHOOL, PARK, OTHER
}

data class GeofenceZone(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val type: GeofenceType,
    val notifyOnEntry: Boolean = true,
    val notifyOnExit: Boolean = true,
    val isInsideCurrently: Boolean = true
)

enum class BreadcrumbType {
    NORMAL, GEOFENCE_ENTER, GEOFENCE_EXIT, SOS_ALERT
}

data class LocationBreadcrumb(
    val id: String,
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val type: BreadcrumbType,
    val detail: String
)

data class DeviceLocationState(
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val address: String = "Lincoln High School, 450 Elm St",
    val accuracyMeters: Float = 6.5f,
    val speedKmh: Float = 0f,
    val lastUpdated: String = "2 min ago",
    val isGpsActive: Boolean = true,
    val geofences: List<GeofenceZone> = listOf(
        GeofenceZone("geo_home", "Home", 37.7730, -122.4150, 150f, GeofenceType.HOME, isInsideCurrently = false),
        GeofenceZone("geo_school", "Lincoln School", 37.7749, -122.4194, 250f, GeofenceType.SCHOOL, isInsideCurrently = true),
        GeofenceZone("geo_park", "Sunset Park", 37.7780, -122.4230, 300f, GeofenceType.PARK, isInsideCurrently = false)
    ),
    val breadcrumbs: List<LocationBreadcrumb> = listOf(
        LocationBreadcrumb("bc_1", "08:15 AM", 37.7730, -122.4150, "Home, 120 Oak St", BreadcrumbType.GEOFENCE_EXIT, "Departed Safe Zone (Home)"),
        LocationBreadcrumb("bc_2", "08:32 AM", 37.7740, -122.4172, "Transit Route 7", BreadcrumbType.NORMAL, "Speed 24 km/h"),
        LocationBreadcrumb("bc_3", "08:45 AM", 37.7749, -122.4194, "Lincoln School", BreadcrumbType.GEOFENCE_ENTER, "Entered Safe Zone (School)"),
        LocationBreadcrumb("bc_4", "11:30 AM", 37.7749, -122.4194, "School Cafeteria", BreadcrumbType.NORMAL, "In Safe Zone"),
        LocationBreadcrumb("bc_5", "02:15 PM", 37.7749, -122.4194, "School Library", BreadcrumbType.NORMAL, "Current Location")
    )
)
