package com.example.data

/**
 * Production Firebase Cloud Firestore Architecture & Security Rules Reference
 * for SafeGuard Parental Control & Child Safety Suite.
 */
object FirestoreSchema {

    const val COLLECTION_PARENTS = "parents"
    const val COLLECTION_CHILDREN = "children"
    const val COLLECTION_DEVICES = "devices"
    const val COLLECTION_PAIRING_CODES = "pairing_codes"
    const val COLLECTION_SESSIONS = "stream_sessions"
    const val COLLECTION_LOGS = "activity_logs"
    const val COLLECTION_GEOFENCES = "geofences"

    val FIRESTORE_RULES = """
        rules_version = '2';
        service cloud.firestore {
          match /databases/{database}/documents {
            
            // Helper functions
            function isAuthenticated() {
              return request.auth != null;
            }
            function isParent(parentId) {
              return isAuthenticated() && request.auth.uid == parentId;
            }
            function isChildDevice(deviceId) {
              return isAuthenticated() && request.auth.token.deviceId == deviceId;
            }
            
            // Parents account collection
            match /parents/{parentId} {
              allow read, write: if isParent(parentId);
              
              // Child profiles managed by this parent (Up to 10 devices)
              match /devices/{deviceId} {
                allow read, write: if isParent(parentId);
                allow read, update: if isAuthenticated() && request.auth.token.parentId == parentId;
              }
              
              // Location breadcrumbs
              match /devices/{deviceId}/locations/{locationId} {
                allow read: if isParent(parentId);
                allow write: if isAuthenticated();
              }
              
              // Live camera & screen mirror sessions
              match /devices/{deviceId}/sessions/{sessionId} {
                allow read, write: if isParent(parentId) || isChildDevice(deviceId);
              }
            }
            
            // Device Pairing temporary codes
            match /pairing_codes/{code} {
              allow create, read: if isAuthenticated();
              allow delete: if isAuthenticated();
            }
          }
        }
    """.trimIndent()

    val SETUP_INSTRUCTIONS = """
        1. Create a Firebase project in the Firebase Console (https://console.firebase.google.com).
        2. Add Android App with package name 'com.aistudio.safeguard.kpnw'.
        3. Download 'google-services.json' and place it in the '/app' directory.
        4. Enable 'Authentication' -> 'Google Sign-In' and 'Email/Password'.
        5. Enable 'Cloud Firestore' in Native mode.
        6. Apply the Firestore Security Rules defined in FirestoreSchema.FIRESTORE_RULES.
        7. Enable 'Firebase Cloud Messaging' for instant emergency SOS push alerts.
        8. For live camera & screen streaming, integrate WebRTC signaling over Firestore or Firebase Realtime DB.
    """.trimIndent()
}
