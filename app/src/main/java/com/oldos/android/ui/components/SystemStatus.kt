package com.oldos.android.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.telephony.TelephonyManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

data class BatteryStatus(
    val percentage: Int = 100,
    val isCharging: Boolean = false
)

/** Live battery level/charging state via the sticky ACTION_BATTERY_CHANGED broadcast. */
@Composable
fun rememberBatteryStatus(): BatteryStatus {
    val context = LocalContext.current
    val status = remember { mutableStateOf(readBattery(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                status.value = readBattery(context)
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }
    return status.value
}

private fun readBattery(context: Context): BatteryStatus {
    return try {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val chargeState = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val percentage = if (level >= 0 && scale > 0) (level * 100) / scale else 100
        val charging = chargeState == BatteryManager.BATTERY_STATUS_CHARGING ||
            chargeState == BatteryManager.BATTERY_STATUS_FULL
        BatteryStatus(percentage, charging)
    } catch (_: Exception) {
        BatteryStatus()
    }
}

/**
 * Real carrier name (no permission required). Falls back to the user-chosen
 * name from Settings → Carrier when the SIM/radio is unavailable.
 */
@Composable
fun rememberCarrierName(fallback: String): String {
    val context = LocalContext.current
    return remember(fallback) {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            tm?.networkOperatorName?.takeIf { it.isNotBlank() } ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }
}
