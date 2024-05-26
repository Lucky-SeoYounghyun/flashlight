
package com.example.flashlight

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

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        fullWhiteLayout = findViewById(R.id.full_white_layout)
        val closeButton = findViewById<ImageButton>(R.id.close_button)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        val initialColor = loadColor()
        if (initialColor != -1) {
            fullWhiteLayout.setBackgroundColor(initialColor)
        }

        // 저장된 밝기 레벨을 로드하여 설정
        val savedBrightness = loadBrightness()
        setScreenBrightness(savedBrightness)

        menuButton.setOnClickListener {
            val colorPickerDialog = CustomColorPickerDialog(this, this, initialColor)
            colorPickerDialog.show()
        }

        closeButton.setOnClickListener {
            finish()  // 액티비티 종료
        }

        // 화면 밝기 조절을 위한 터치 리스너 설정
        fullWhiteLayout.setOnTouchListener(object : View.OnTouchListener {
            private var initialY: Float = 0f
            private var initialBrightness: Float = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                event ?: return false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (event.rawX <= v!!.width / 2) {
                            initialY = event.rawY
                            initialBrightness = getScreenBrightness()
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (event.rawX <= v!!.width / 2) {
                            val deltaY = initialY - event.rawY
                            val deltaYPercentage = deltaY / v.height
                            adjustScreenBrightness(deltaYPercentage)
                        }
                    }
                }
                return true
            }

            private fun adjustScreenBrightness(deltaYPercentage: Float) {
                val newBrightness = (initialBrightness + deltaYPercentage).coerceIn(0.00001f, 1.0f)
                setScreenBrightness(newBrightness)
                showBrightnessLevelToast(newBrightness)
            }
        })
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
        return sharedPref.getInt("lastColor", -1)  // 기본값으로 -1 반환
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
        return sharedPref.getFloat("screenBrightness", 1.0f)  // 기본 밝기를 100%로 설정
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
        lastToast?.cancel()  // 이전 토스트가 아직 표시 중이면 취소
        lastToast = Toast.makeText(this@FullWhiteActivity, "${(brightness * 100).toInt()}%", Toast.LENGTH_SHORT)
        lastToast?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveBrightness(getScreenBrightness())  // 액티비티가 종료될 때 밝기 저장
    }

    override fun onColorSelected(color: Int) {
        fullWhiteLayout.setBackgroundColor(color)
        saveColor(color)
    }
}