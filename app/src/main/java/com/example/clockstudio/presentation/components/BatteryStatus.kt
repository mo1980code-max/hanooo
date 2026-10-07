package com.example.clockstudio.presentation.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.clockstudio.util.BatteryInfo
import com.example.clockstudio.util.batteryInfoFrom

/** Registers a single battery receiver only while a visible, resumed screen needs smart-clock data. */
@Composable
fun rememberBatteryInfo(active: Boolean): BatteryInfo? {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    var info by remember { mutableStateOf<BatteryInfo?>(null) }

    DisposableEffect(active, context, lifecycleOwner) {
        if (!active) {
            info = null
            onDispose { }
        } else {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action != Intent.ACTION_BATTERY_CHANGED) return
                    info = batteryInfoFrom(
                        level = intent.getIntExtra("level", -1),
                        scale = intent.getIntExtra("scale", -1),
                        status = intent.getIntExtra("status", -1),
                    )
                }
            }
            var registered = false
            fun register() {
                if (registered) return
                val sticky = ContextCompat.registerReceiver(
                    context,
                    receiver,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                    ContextCompat.RECEIVER_NOT_EXPORTED,
                )
                registered = true
                if (sticky != null) receiver.onReceive(context, sticky)
            }
            fun unregister() {
                if (!registered) return
                runCatching { context.unregisterReceiver(receiver) }
                registered = false
            }
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> register()
                    Lifecycle.Event.ON_STOP -> unregister()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) register()
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                unregister()
            }
        }
    }
    return info
}
