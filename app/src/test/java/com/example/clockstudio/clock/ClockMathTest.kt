package com.example.clockstudio.clock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockMathTest {
    @Test fun twelveOClockPointsStraightUp() {
        assertEquals(0f, ClockMath.hourAngle(12, 0), 0.0001f)
    }

    @Test fun threeOClockIsNinetyDegrees() {
        assertEquals(90f, ClockMath.hourAngle(3, 0), 0.0001f)
    }

    @Test fun sixThirtyIncludesMinuteProgressOnHourHand() {
        assertEquals(195f, ClockMath.hourAngle(6, 30), 0.0001f)
    }

    @Test fun twentyFourHourTimeMapsToTwelveHourDial() {
        assertEquals(90f, ClockMath.hourAngle(15, 0), 0.0001f)
        assertEquals(3, ClockMath.hoursToAnalogHour(15))
    }

    @Test fun minuteAndSecondHandsIncludeSubMinuteProgress() {
        assertEquals(181.5f, ClockMath.minuteAngle(30, 15), 0.0001f)
        assertEquals(91.5f, ClockMath.secondAngle(15, 250f), 0.0001f)
    }

    @Test fun normalizedGesturePositionIsClampedToScreenBounds() {
        assertEquals(0f to 1f, ClockMath.normalizedPosition(-0.4f, 1.7f))
        assertTrue(ClockMath.normalizedPosition(0.32f, 0.68f) == (0.32f to 0.68f))
    }
}
