package com.example.flashlight

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
            val level: Int = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status: Int = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

            val batteryPct: Float = level * 100 / scale.toFloat()
            textView.text = "${batteryPct.toInt()}%"

            // 배터리 상태에 따라 이미지 변경
            when {
                batteryPct >= 80 -> imageView.setImageResource(R.drawable.battery_80_icon)
                batteryPct >= 60 -> imageView.setImageResource(R.drawable.battery_60_icon)
                batteryPct >= 40 -> imageView.setImageResource(R.drawable.battery_40_icon)
                batteryPct > 15 -> imageView.setImageResource(R.drawable.battery_20_icon)
                batteryPct <= 15 -> imageView.setImageResource(R.drawable.battery_15_icon)
                batteryPct <= 5 -> imageView.setImageResource(R.drawable.battery_empty_icon)
                else -> imageView.setImageResource(R.drawable.battery_icon)
            }

            // 배터리 충전 상태에 따라 추가 이미지 변경 (optional)
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                imageView.setImageResource(R.drawable.charge_battery_icon_ver2)
            }
        }
    }
}