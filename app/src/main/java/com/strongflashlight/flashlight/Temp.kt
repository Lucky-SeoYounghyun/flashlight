package com.strongflashlight.flashlight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.TextView

class Temp(private val context: Context, private val textView: TextView) {
    private var batteryTempReceiver: BroadcastReceiver? = null
    private var isReceiverRegistered = false // 리시버 등록 상태 추적 // 이부분 수정했습니다

/*    init {
        registerBatteryTemperatureReceiver()
    }*/

    fun registerBatteryTemperatureReceiver() {
        // 보안처리되었습니다

    }

    private fun updateTemperatureDisplay(temperature: Double) {
        textView.text = context.getString(R.string.battery_temperature, temperature)
    }

    fun unregisterReceiver() { // 수정됨
        // 보안처리되었습니다

    }
}