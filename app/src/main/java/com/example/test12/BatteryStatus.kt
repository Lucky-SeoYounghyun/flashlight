package com.example.test12

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.TextView

class BatteryStatus(private val context: Context, private val textView: TextView) {

    fun updateBatteryStatus() {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }

        batteryStatus?.let {
            val level: Int = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            val batteryPct: Float = level * 100 / scale.toFloat()
            textView.text = "${batteryPct.toInt()}%"
        }
    }
}