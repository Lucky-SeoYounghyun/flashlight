package com.strongflashlight.flashlight

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.widget.ImageView
import android.os.Looper
import android.widget.TextView

class BatteryStatus(private val context: Context, private val textView: TextView, private val imageView: ImageView) {

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 5000L // 5초

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateBatteryStatus()
            handler.postDelayed(this, updateInterval)
        }
    }

    fun startMonitoring() {
        handler.post(updateRunnable)
    }

    fun stopMonitoring() {
        handler.removeCallbacks(updateRunnable)
    }
    fun updateBatteryStatus() {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }

        batteryStatus?.let {
            // 보안처리되었습니다

        }
    }
}