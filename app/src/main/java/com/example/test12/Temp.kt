package com.example.test12

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.TextView

class Temp(private val context: Context, private val textView: TextView) {
    private var batteryTempReceiver: BroadcastReceiver? = null

    init {
        registerBatteryTemperatureReceiver()
    }

    private fun registerBatteryTemperatureReceiver() {
        batteryTempReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0
                updateTemperatureDisplay(temp)
            }
        }
        context.registerReceiver(batteryTempReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun updateTemperatureDisplay(temperature: Double) {
        textView.text = context.getString(R.string.battery_temperature, temperature)
    }

    fun unregisterReceiver() {
        batteryTempReceiver?.let {
            context.unregisterReceiver(it)
        }
    }
}