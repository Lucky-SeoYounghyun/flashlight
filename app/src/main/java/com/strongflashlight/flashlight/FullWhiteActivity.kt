package com.strongflashlight.flashlight

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import android.content.Context

class FullWhiteActivity : Activity(), ColorPickerDialogCallback {
    private var lastToast: Toast? = null
    private lateinit var fullWhiteLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.white_view)

        // 보안처리되었습니다

    }

    private fun saveColor(color: Int) {
        val sharedPref = getSharedPreferences("ColorPicker", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("lastColor", color)
            apply()
        }
    }

    fun loadColor(): Int {
        val sharedPref = getSharedPreferences("ColorPicker", Context.MODE_PRIVATE)
        return sharedPref.getInt("lastColor", -1)
    }

    private fun saveBrightness(brightness: Float) {
        val sharedPref = getSharedPreferences("FullWhitePrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putFloat("screenBrightness", brightness)
            apply()
        }
    }

    private fun loadBrightness(): Float {
        val sharedPref = getSharedPreferences("FullWhitePrefs", Context.MODE_PRIVATE)
        return sharedPref.getFloat("screenBrightness", 1.0f)
    }

    private fun getScreenBrightness(): Float {
        return window.attributes.screenBrightness.takeIf { it > 0 } ?: 0.5f
    }

    private fun setScreenBrightness(brightness: Float) {
        val params = window.attributes
        params.screenBrightness = brightness
        window.attributes = params
    }

    private fun showBrightnessLevelToast(brightness: Float) {
        lastToast?.cancel()
        lastToast = Toast.makeText(this@FullWhiteActivity, "${(brightness * 100).toInt()}%", Toast.LENGTH_SHORT)
        lastToast?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveBrightness(getScreenBrightness())
    }

    override fun onColorSelected(color: Int) {
        fullWhiteLayout.setBackgroundColor(color)
        saveColor(color)
    }
}
