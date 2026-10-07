package com.example.clockstudio.util

import android.os.BatteryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BatteryInfoTest {
    @Test fun percentageAndChargingStateComeFromBatteryBroadcastValues() {
        val info = batteryInfoFrom(
            level = 3,
            scale = 4,
            status = BatteryManager.BATTERY_STATUS_CHARGING,
        )

        assertEquals(75, info?.percent)
        assertEquals(true, info?.isCharging)
    }

    @Test fun invalidBatteryLevelDoesNotProduceFakePercentage() {
        assertNull(batteryInfoFrom(level = -1, scale = 100, status = BatteryManager.BATTERY_STATUS_UNKNOWN))
        assertNull(batteryInfoFrom(level = 20, scale = 0, status = BatteryManager.BATTERY_STATUS_UNKNOWN))
    }
}
