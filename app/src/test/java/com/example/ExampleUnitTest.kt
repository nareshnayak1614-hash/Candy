package com.example

import com.example.data.AppRoleMode
import com.example.data.SafeGuardRepository
import com.example.model.GeofenceType
import com.example.model.LiveStreamType
import com.example.model.StreamConsentState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var repository: SafeGuardRepository

    @Before
    fun setUp() {
        repository = SafeGuardRepository()
    }

    @Test
    fun testInitialTenDevicesConnected() {
        val devices = repository.devices.value
        assertEquals("Repository should connect exactly 10 child devices initially", 10, devices.size)
        assertEquals("dev_1", repository.selectedDeviceId.value)
        assertEquals("Liam", devices[0].name)
        assertEquals("Mia", devices[9].name)
    }

    @Test
    fun testSelectChildDevice() {
        repository.selectDevice("dev_3")
        assertEquals("dev_3", repository.selectedDeviceId.value)
        val selectedChild = repository.devices.value.find { it.id == "dev_3" }
        assertNotNull(selectedChild)
        assertEquals("Noah", selectedChild?.name)
    }

    @Test
    fun testEmergencyDeviceLockToggle() {
        repository.toggleDeviceLock("dev_1", true, "Bedtime lock")
        val dev1 = repository.devices.value.find { it.id == "dev_1" }
        assertTrue(dev1!!.isLocked)
        assertEquals("Bedtime lock", dev1.lockReason)

        repository.toggleDeviceLock("dev_1", false)
        val dev1Unlocked = repository.devices.value.find { it.id == "dev_1" }
        assertFalse(dev1Unlocked!!.isLocked)
    }

    @Test
    fun testLockAllTenDevices() {
        repository.lockAllDevices(true)
        val allLocked = repository.devices.value.all { it.isLocked }
        assertTrue("All 10 child devices should be locked", allLocked)

        repository.lockAllDevices(false)
        val allUnlocked = repository.devices.value.all { !it.isLocked }
        assertTrue("All 10 child devices should be unlocked", allUnlocked)
    }

    @Test
    fun testUpdateDailyScreenTimeLimit() {
        repository.updateDailyLimit("dev_1", 180)
        val dev1 = repository.devices.value.find { it.id == "dev_1" }
        assertEquals(180, dev1!!.dailyLimitMinutes)
        val screenTimeState = repository.screenTimeMap.value["dev_1"]
        assertEquals(180, screenTimeState?.dailyLimitMinutes)
    }

    @Test
    fun testAppSpecificBlockAndLimits() {
        repository.toggleAppBlock("dev_1", "app_tt", true)
        val screenTimeState = repository.screenTimeMap.value["dev_1"]
        val ttApp = screenTimeState?.appLimits?.find { it.id == "app_tt" }
        assertTrue("TikTok app should be blocked", ttApp!!.isBlocked)
    }

    @Test
    fun testGeofenceAddAndRemove() {
        repository.addGeofence("dev_1", "Grandma's House", GeofenceType.HOME, 200f)
        val loc = repository.locationMap.value["dev_1"]
        val newZone = loc?.geofences?.find { it.name == "Grandma's House" }
        assertNotNull(newZone)
        assertEquals(200f, newZone!!.radiusMeters, 0.01f)

        repository.removeGeofence("dev_1", newZone.id)
        val locAfter = repository.locationMap.value["dev_1"]
        assertNull(locAfter?.geofences?.find { it.id == newZone.id })
    }

    @Test
    fun testLiveStreamingConsentLifecycle() {
        repository.startLiveStreamRequest(LiveStreamType.CAMERA, "dev_1")
        assertEquals(StreamConsentState.AWAITING_CHILD_CONSENT, repository.streamSession.value.state)

        repository.grantChildStreamConsent()
        assertEquals(StreamConsentState.ACTIVE_STREAMING, repository.streamSession.value.state)
        assertTrue(repository.streamSession.value.isChildConsentGiven)

        repository.stopStreamSession()
        assertEquals(StreamConsentState.AUTO_TERMINATED, repository.streamSession.value.state)
    }

    @Test
    fun testEmergencySosPanicTrigger() {
        repository.triggerSos("dev_1")
        val dev1 = repository.devices.value.find { it.id == "dev_1" }
        assertTrue(dev1!!.emergencySosActive)

        repository.cancelSos("dev_1")
        val dev1Resolved = repository.devices.value.find { it.id == "dev_1" }
        assertFalse(dev1Resolved!!.emergencySosActive)
    }

    @Test
    fun testParentPinAuthentication() {
        assertTrue(repository.verifyParentPin("1234"))
        assertFalse(repository.verifyParentPin("0000"))

        repository.updateParentPin("5678")
        assertTrue(repository.verifyParentPin("5678"))
        assertFalse(repository.verifyParentPin("1234"))
    }
}
