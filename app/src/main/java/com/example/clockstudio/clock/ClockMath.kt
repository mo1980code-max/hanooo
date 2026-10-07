package com.example.clockstudio.clock

import java.time.ZonedDateTime

/** Clock face angles are clockwise degrees from 12 o'clock, matching Android Canvas rotation. */
object ClockMath {
    fun secondAngle(seconds: Int, milliseconds: Float = 0f): Float =
        (seconds.mod(60) * 6f) + (milliseconds.coerceIn(0f, 999.999f) * 0.006f)

    fun minuteAngle(minutes: Int, seconds: Int, milliseconds: Float = 0f): Float =
        (minutes.mod(60) * 6f) + (seconds.mod(60) * 0.1f) +
            (milliseconds.coerceIn(0f, 999.999f) * 0.0001f)

    fun hourAngle(hours24: Int, minutes: Int, seconds: Int = 0, milliseconds: Float = 0f): Float =
        (hoursToAnalogHour(hours24) * 30f) + (minutes.mod(60) * 0.5f) +
            (seconds.mod(60) / 120f) + (milliseconds.coerceIn(0f, 999.999f) / 120_000f)

    fun hoursToAnalogHour(hours24: Int): Int = Math.floorMod(hours24, 12)

    fun hourAngle(time: ZonedDateTime): Float =
        hourAngle(time.hour, time.minute, time.second, time.nano / 1_000_000f)

    fun minuteAngle(time: ZonedDateTime): Float =
        minuteAngle(time.minute, time.second, time.nano / 1_000_000f)

    fun secondAngle(time: ZonedDateTime): Float =
        secondAngle(time.second, time.nano / 1_000_000f)

    /** Clamp a gesture-derived screen point so saved layouts remain portable between displays. */
    fun normalizedPosition(x: Float, y: Float): Pair<Float, Float> =
        x.coerceIn(0f, 1f) to y.coerceIn(0f, 1f)
}
