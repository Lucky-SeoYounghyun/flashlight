package com.strongflashlight.flashlight

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Window
import android.widget.Button
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar

class CustomColorPickerDialog(context: Context, private val callback: ColorPickerDialogCallback, private val initialColor: Int) : Dialog(context) {
    private lateinit var colorPickerView: ColorPickerView
    private lateinit var brightnessSlider: BrightnessSlideBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_color_picker)

        setupViews()
        setupListeners()
        colorPickerView.post {
            initializeColorPicker(initialColor)
        }
    }

    private fun setupViews() {
        // 보안처리되었습니다

    }
    private fun setupListeners() {
        val buttonSelect = findViewById<Button>(R.id.button_select)
        val buttonCancel = findViewById<Button>(R.id.button_cancel)

        // 보안처리되었습니다

    }


    private fun initializeColorPicker(initialColor: Int) {
        // 보안처리되었습니다

    }

    private fun loadInitialColor(): Int {
        // 보안처리되었습니다

    }

    private fun setupColorButton(buttonId: Int, color: Int) {
        val button = findViewById<Button>(buttonId)
        // 보안처리되었습니다

    }
    private fun adjustColorPickerSize(colorPickerView: ColorPickerView) {
        val displayMetrics = DisplayMetrics()
        window?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val minDimension = minOf(width, height)

        val size = (minDimension * 2) / 3
        // 보안처리되었습니다

    }
}