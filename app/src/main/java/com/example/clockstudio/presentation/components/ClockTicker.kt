package com.example.clockstudio.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import java.time.ZonedDateTime

/** A single screen-level ticker avoids one timer per gallery thumbnail. */
@Composable
fun rememberCurrentTime(updateEverySecond: Boolean, smoothSeconds: Boolean = false): ZonedDateTime {
    var time by remember { mutableStateOf(ZonedDateTime.now()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(updateEverySecond, smoothSeconds, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                time = ZonedDateTime.now()
                val wait = when {
                    smoothSeconds -> (33L - System.currentTimeMillis().mod(33L)).coerceAtLeast(8L)
                    updateEverySecond -> (1_000L - System.currentTimeMillis().mod(1_000L)).coerceAtLeast(30L)
                    else -> (60_000L - System.currentTimeMillis().mod(60_000L) + 25L).coerceAtLeast(250L)
                }
                delay(wait)
            }
        }
    }
    return time
}
