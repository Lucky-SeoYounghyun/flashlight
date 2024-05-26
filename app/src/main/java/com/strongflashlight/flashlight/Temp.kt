package com.example.flashlight

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
        if (!isReceiverRegistered) { // 리시버가 이미 등록되어 있는지 확인 // 이부분 수정했습니다
            batteryTempReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0
                    updateTemperatureDisplay(temp)
                }
            }
            context.registerReceiver(batteryTempReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            isReceiverRegistered = true // 리시버 등록 상태 업데이트 // 이부분 수정했습니다
        }
    }

    private fun updateTemperatureDisplay(temperature: Double) {
        textView.text = context.getString(R.string.battery_temperature, temperature)
    }

    fun unregisterReceiver() { // 수정됨
        if (isReceiverRegistered) {
            batteryTempReceiver?.let {
                context.unregisterReceiver(it)
                isReceiverRegistered = false // 리시버가 등록 해제되었음을 표시
            }
        }
    }
}