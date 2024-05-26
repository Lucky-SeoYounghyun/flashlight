package com.example.flashlight

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.view.MotionEvent
import android.widget.ImageView
import android.app.Dialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import android.content.SharedPreferences
import android.widget.LinearLayout
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : ComponentActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var adView: AdView
    lateinit var btnFloating: Button
    lateinit var btnPower: ImageButton
    lateinit var btnScreen: Button
    lateinit var btnSOS: Button
    lateinit var btnSetting: Button
    lateinit var sosSeekBar: SeekBar
    lateinit var leftbutton: LinearLayout
    lateinit var rightbutton: LinearLayout
    private var isSOSActive = false
    lateinit var seekBar: SeekBar
    private lateinit var power: Power
    private var isTorchOn = false
    lateinit var textBatteryTemp: TextView
    private lateinit var temp: Temp
    lateinit var ChargeBattery: TextView
    private lateinit var batteryStatus: BatteryStatus
    lateinit var batteryImageView: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private var sosison = false
    private lateinit var saveStatus: SaveStatus
    private val handler = Handler(Looper.getMainLooper())

    private val originalSosPattern = listOf(
        Pair(true, 150L), Pair(false, 150L), // S: 짧은 신호 3번 (켜짐, 꺼짐 반복)
        Pair(true, 150L), Pair(false, 150L),
        Pair(true, 150L), Pair(false, 450L), // S 끝과 O 사이의 간격
        Pair(true, 900L), Pair(false, 450L), // O: 긴 신호 3번 (켜짐, 꺼짐 반복)
        Pair(true, 900L), Pair(false, 450L),
        Pair(true, 900L), Pair(false, 450L), // O 끝과 S 사이의 간격
        Pair(true, 150L), Pair(false, 150L), // S: 짧은 신호 3번 (켜짐, 꺼짐 반복)
        Pair(true, 150L), Pair(false, 150L),
        Pair(true, 150L), Pair(false, 1500L) // S 끝과 다음 S 사이의 간격
    )

    private var sosPattern = originalSosPattern

    private var sosIndex = 0

    private val sosRunnable = object : Runnable {
        override fun run() {
            if (!isSOSActive) return

            if (sosIndex < sosPattern.size) {
                val (isOn, duration) = sosPattern[sosIndex]

                if (isOn) {
                    power.setTorchStrengthLevel(seekBar.progress + 1)
                } else {
                    power.turnOffTorch()
                }

                handler.postDelayed(this, duration)
                sosIndex++
            } else {
                sosIndex = 0
                handler.postDelayed(this, sosPattern.last().second)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        var level = seekBar.progress
        var soslevel = sosSeekBar.progress
        Log.d("현재 값2", "$level")
        Log.d("현재 값2", "$soslevel")
        Log.d("현재 값2", "$isSOSActive ")
        Log.d("현재 값2", "$isTorchOn")

    }
    private fun updateUIBasedOnStatus(statusMap: Map<String, String>) {
        val seekBarProgress = statusMap["currentlevel"]?.toIntOrNull() ?: 0
        val isTorchOn = statusMap["isTorchOn"]?.toBoolean() ?: false
        val sosSeekBarProgress = statusMap["soscurrentlevel"]?.toIntOrNull() ?: 0
        val isSOSActive = statusMap["sosisTorchOn"]?.toBoolean() ?: false

        if (isTorchOn) {
            power = Power(this)
            power.initializeCamera()
            this.isTorchOn = true
            this.isSOSActive = false
            btnPower.setBackgroundResource(R.drawable.main_button_on)
            seekBar.visibility = View.VISIBLE
            sosSeekBar.visibility = View.GONE
            seekBar.progress = seekBarProgress
            power.setTorchStrengthLevel(seekBar.progress + 1)
        } else if (isSOSActive) {
            this.isSOSActive = true
            this.isTorchOn = false
            power.setTorchStrengthLevel(seekBar.progress + 1)
            seekBar.visibility = View.GONE
            sosSeekBar.visibility = View.VISIBLE
            sosSeekBar.progress = sosSeekBarProgress
            handler.post(sosRunnable)
        } else {
            this.isSOSActive = false
            handler.removeCallbacks(sosRunnable)
            power.turnOffTorch()
            this.isTorchOn = false
            btnPower.setBackgroundResource(R.drawable.main_button_off)
            seekBar.visibility = View.GONE
            sosSeekBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("현재 값", "출력됩니다")
        batteryStatus.startMonitoring()
        temp.registerBatteryTemperatureReceiver()

        val statusMap = saveStatus.readStatusFromFile()
        val flagvalue = statusMap["flag"]
        val seekBarProgress = statusMap["currentlevel"]?.toIntOrNull() ?: 0
        val isTorchOn = statusMap["isTorchOn"]?.toBoolean() ?: false
        val sosSeekBarProgress = statusMap["soscurrentlevel"]?.toIntOrNull() ?: 0
        val isSOSActive = statusMap["sosisTorchOn"]?.toBoolean() ?: false

        Log.d("현재 값", "이것은3 + $statusMap")

        Intent(this, FloatingService::class.java).apply {
            action = FloatingService.ACTION_HIDE_FLOATING
            startService(this)
        }
        Log.d("현재 값", "flag1 + $statusMap")
        updateUIBasedOnStatus(statusMap)
    }
    override fun onPause() {
        super.onPause()
        var level = seekBar.progress
        var soslevel = sosSeekBar.progress
        Log.d("현재 값", "$level")
        Log.d("현재 값", "$soslevel")
        Log.d("현재 값", "$isSOSActive ")
        Log.d("현재 값", "$isTorchOn")

        temp.unregisterReceiver()
        batteryStatus.stopMonitoring()

        val statusMap = saveStatus.readStatusFromFile()
        val flagvalue = statusMap["flag"]
        Log.d("현재 값", "이것은30 + $flagvalue")
        Log.d("현재 값", "이것은3 + $statusMap")

        if(flagvalue.toBoolean()){
            updateUIBasedOnStatus(statusMap)
            saveStatus.saveStatusToFile(0, false, 0, false, false)
        }

        saveStatus.saveStatusToFile(level, isTorchOn, soslevel, isSOSActive, false)

    }
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Initialize SaveStatus
        saveStatus = SaveStatus(this)

        // Initialize the Mobile Ads SDK
        MobileAds.initialize(this) {}

        // Find the AdView as defined in the layout file
        adView = findViewById(R.id.adView)

        // Create an ad request
        val adRequest = AdRequest.Builder().build()

        // Load the ad
        adView.loadAd(adRequest)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        btnFloating = findViewById(R.id.btnFloating)
        btnScreen = findViewById(R.id.btnScreen)
        btnSOS = findViewById(R.id.btnSOS)
        btnPower = findViewById(R.id.btnPower)
        seekBar = findViewById(R.id.seekBar)
        sosSeekBar = findViewById(R.id.sosSeekBar)
        btnSetting = findViewById(R.id.btnsetting)
        leftbutton = findViewById(R.id.leftbutton)
        rightbutton = findViewById(R.id.rightbutton)

        sosSeekBar.visibility = View.GONE

        textBatteryTemp = findViewById(R.id.textBatteryTemp)
        temp = Temp(this, textBatteryTemp)

        ChargeBattery = findViewById(R.id.ChargeBattery)
        batteryImageView = findViewById(R.id.batteryImageView)

        batteryStatus = BatteryStatus(this, ChargeBattery, batteryImageView)
        batteryStatus.updateBatteryStatus()

        textBatteryTemp = findViewById(R.id.textBatteryTemp)
        temp = Temp(this, textBatteryTemp)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE)

        val switchState = sharedPreferences.getBoolean("flashlightSwitch", false)
        val seekBarProgress = sharedPreferences.getInt("flashlightSeekBar", 0)

        Handler(Looper.getMainLooper()).post {
            if (switchState) {
                // Switch is ON
                power = Power(this)
                power.initializeCamera()
                isTorchOn = true
                isSOSActive = false
                sosSeekBar.visibility = View.GONE
                btnPower.setBackgroundResource(R.drawable.main_button_on)
                seekBar.visibility = View.VISIBLE
                seekBar.progress = seekBarProgress
                power.setTorchStrengthLevel(seekBar.progress + 1)
            } else {
                // Switch is OFF
                isTorchOn = false
                isSOSActive = false
                btnPower.setBackgroundResource(R.drawable.main_button_off)
                sosSeekBar.visibility = View.GONE
                seekBar.visibility = View.GONE
            }
        }

        setTouchListener(btnFloating, R.drawable.pip_mode_off_ver2, R.drawable.pip_mode_on_ver2) {
            var level = seekBar.progress
            var soslevel = sosSeekBar.progress
            saveStatus.saveStatusToFile(level, isTorchOn, soslevel, isSOSActive, false)
            if (!Settings.canDrawOverlays(this)) {
                showPermissionAlertDialog()
            } else {
                Intent(this, FloatingService::class.java).also { intent ->
                    intent.action = FloatingService.ACTION_SHOW_FLOATING
                    intent.putExtra("shouldHandleHomePress", true)  // true 값 전달
                    startService(intent)
                    moveTaskToBack(true)
                }
            }
        }

        setTouchListener(btnScreen, R.drawable.screen_mode_off_ver2, R.drawable.screen_mode_on_ver2) {
            val intent = Intent(MainActivity@this, FullWhiteActivity::class.java)
            startActivity(intent)
        }

        setTouchListener(btnSOS, R.drawable.sos, R.drawable.soson) {
            var level = seekBar.progress
            var soslevel = sosSeekBar.progress
            if (isSOSActive) {
                stopSOS()
                saveStatus.saveStatusToFile(level, false, soslevel, isSOSActive, false)
            } else {
                saveStatus.saveStatusToFile(level, false, soslevel, isSOSActive, false)
                startSOS()
            }
        }

        setTouchListener(btnSetting, R.drawable.setting_ver2, R.drawable.setting_ver2_on) {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        power = Power(this)
        power.initializeCamera()
        seekBar.max = power.maxTorchStrength - 1
        seekBar.progress = 0

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isTorchOn || isSOSActive) {
                    power.setTorchStrengthLevel(progress + 1)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sosSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val multiplier = 1 + (progress * 1.1)
                sosPattern = originalSosPattern.map { Pair(it.first, (it.second / multiplier).toLong()) }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        setPowerButtonTouchListener(btnPower)

        val statusMap = saveStatus.readStatusFromFile()
        Log.d("현재 값", "이것은 + $statusMap")
        updateUIBasedOnStatus(statusMap)
    }

    private fun setTouchListener(button: Button, normalBackground: Int, pressedBackground: Int, onClick: () -> Unit) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    button.setBackgroundResource(pressedBackground)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    button.setBackgroundResource(normalBackground)
                    onClick()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    button.setBackgroundResource(normalBackground)
                    true
                }
                else -> false
            }
        }
    }

    private fun showPermissionAlertDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_permission_alert)
        dialog.setCancelable(false)
        dialog.findViewById<Button>(R.id.dialog_button_positive).setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.dialog_button_negative).setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )
        dialog.window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.show()
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    private fun startSOS() {
        isSOSActive = true
        isTorchOn = false
        sosIndex = 0
        power.setTorchStrengthLevel(seekBar.progress + 1)
        btnPower.setBackgroundResource(R.drawable.main_button_off)
        leftbutton.visibility = View.INVISIBLE
        rightbutton.visibility = View.INVISIBLE
        seekBar.visibility = View.GONE
        sosSeekBar.visibility = View.VISIBLE
        handler.post(sosRunnable)
    }

    private fun stopSOS() {
        isSOSActive = false
        handler.removeCallbacks(sosRunnable)
        power.turnOffTorch()
        isTorchOn = false
        leftbutton.visibility = View.VISIBLE
        rightbutton.visibility = View.VISIBLE
        btnPower.setBackgroundResource(R.drawable.main_button_off)
        sosSeekBar.visibility = View.GONE
    }

    private fun toggleTorch() {
        if (isTorchOn) {
            power.turnOffTorch()
        } else {
            power.setTorchStrengthLevel(power.maxTorchStrength)
        }
        isTorchOn = !isTorchOn
    }

    private fun setPowerButtonTouchListener(button: ImageButton) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction {
                        if (isSOSActive) {
                            stopSOS()
                            isTorchOn = true
                            power.setTorchStrengthLevel(seekBar.progress + 1)
                            button.setBackgroundResource(R.drawable.main_button_on)
                            seekBar.visibility = View.VISIBLE
                            println("불이 켜져있습니당2")
                        } else {
                            if (isTorchOn) {
                                power.turnOffTorch()
                                button.setBackgroundResource(R.drawable.main_button_off)
                                seekBar.visibility = View.GONE
                                println("불이 꺼져있습니당")
                            } else {
                                power.setTorchStrengthLevel(seekBar.progress + 1)
                                button.setBackgroundResource(R.drawable.main_button_on)
                                seekBar.visibility = View.VISIBLE
                                println("불이 켜져있습니당")
                            }
                            isTorchOn = !isTorchOn
                        }
                    }.start()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    true
                }
                else -> false
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val intent = Intent("com.example.test12.HOME_BUTTON_PRESSED")
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        temp.unregisterReceiver()
        batteryStatus.stopMonitoring()
    }
}
