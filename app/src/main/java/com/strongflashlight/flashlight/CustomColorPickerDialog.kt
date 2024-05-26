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
        colorPickerView = findViewById<ColorPickerView>(R.id.colorPickerView)
        brightnessSlider = findViewById<BrightnessSlideBar>(R.id.brightnessSlider)

        setupColorButton(R.id.colorWhite, 0xFFFFFFFF.toInt())
        setupColorButton(R.id.colorRed, 0xFFFF0000.toInt())
        setupColorButton(R.id.colorBlue, 0xFF0000FF.toInt())
        setupColorButton(R.id.colorBlack, 0xFF000000.toInt())

        colorPickerView.attachBrightnessSlider(brightnessSlider)
        adjustColorPickerSize(colorPickerView)

    }
    private fun setupListeners() {
        val buttonSelect = findViewById<Button>(R.id.button_select)
        val buttonCancel = findViewById<Button>(R.id.button_cancel)

        buttonSelect.setOnClickListener {
            val color = colorPickerView.color
            callback.onColorSelected(color)  // 'callback' 사용
            dismiss()
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }
    }


    private fun initializeColorPicker(initialColor: Int) {
        val initialColor = loadInitialColor()
        if (initialColor != -1) {
            findViewById<ColorPickerView>(R.id.colorPickerView).setInitialColor(initialColor)
        }
    }

    private fun loadInitialColor(): Int {
        val sharedPref = context.getSharedPreferences("ColorPicker", Context.MODE_PRIVATE)
        return sharedPref.getInt("lastColor", -1)
    }

    private fun setupColorButton(buttonId: Int, color: Int) {
        val button = findViewById<Button>(buttonId)
        button.setOnClickListener {
            findViewById<ColorPickerView>(R.id.colorPickerView).setInitialColor(color)
        }
    }
    private fun adjustColorPickerSize(colorPickerView: ColorPickerView) {
        val displayMetrics = DisplayMetrics()
        window?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val minDimension = minOf(width, height)

        val size = (minDimension * 2) / 3
        colorPickerView.layoutParams.width = size
        colorPickerView.layoutParams.height = size
        colorPickerView.requestLayout()
        colorPickerView.invalidate()
        Log.d("ColorPickerSize", "Width: $size, Height: $size")
    }
}

/*
Copyright 2017 skydoves (Jaewoong Eum)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
