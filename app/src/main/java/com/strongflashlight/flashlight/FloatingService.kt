package com.example.flashlight

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.widget.ImageButton
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.os.Handler
import android.os.Looper
private var isHomeReceiverRegistered = false
private var isMenuReceiverRegistered = false
var shouldHandleHomePress = false // 추가된 변수

class FloatingService : Service() {
    private lateinit var power: Power
    private lateinit var powerButton: ImageButton
    private lateinit var torch_level: View
    private lateinit var closeButton: ImageButton
    private lateinit var fullscreenButton: ImageButton
    private var currentLevel = 0
    private var currentLevelpre = 0
    private var shouldHandleHomePress = false // 추가된 변수
    private lateinit var windowManager: WindowManager
    private lateinit var linearLayout: LinearLayout
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var isMoving: Boolean = false
    private lateinit var temp: Temp
    private lateinit var textView: TextView
    private lateinit var saveStatus: SaveStatus

    private val menuReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra("reason")
                if (reason != null) {
                    when (reason) {
                        "homekey" ->
                            if (shouldHandleHomePress) {
                                Log.d("값", "값은 $shouldHandleHomePress")
                                handleHomeButtonPress()
                                Log.d("테스트용","홈버튼")
                            }else{
                                Log.d("값", "값은 $shouldHandleHomePress")
                                saveStatus.saveStatusToFile(0, false, 0, false, true)
                            }
                        "recentapps" ->
                            if(shouldHandleHomePress){
                                handleHomeButtonPress()
                                Log.d("테스트용","목차버튼")
                            }else{
                                saveStatus.saveStatusToFile(0, false, 0, false, true)
                            }
                    }
                }
            }
        }
    }

    private val homeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra("reason")
                if (reason != null && reason == "homekey") {
                    // 홈 버튼이 눌린 것을 감지
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null)
        val inflater = LayoutInflater.from(this)

        floatingView = inflater.inflate(R.layout.floating_view, null)
        textView = floatingView.findViewById(R.id.text_view) as TextView
        powerButton = floatingView.findViewById(R.id.floating_button) as ImageButton
        closeButton = floatingView.findViewById(R.id.close_button) as ImageButton
        fullscreenButton = floatingView.findViewById(R.id.full_screen) as ImageButton
        torch_level = floatingView.findViewById(R.id.torch_level) as View
        linearLayout = floatingView.findViewById<LinearLayout>(R.id.floating_linear_layout) as LinearLayout

        temp = Temp(this, textView)
        temp.registerBatteryTemperatureReceiver() // 리시버 등록

        power = Power(this)

        // SaveStatus 객체 초기화
        saveStatus = SaveStatus(this)

        setupListeners(powerButton) {
            toggleTorchLevel()
        } // powerButton 클릭 이벤트

        setupListeners(closeButton) {
            stopSelf()
            power.turnOffTorch()
            shouldHandleHomePress = false // 변경된 부분
        } // closeButton 클릭 이벤트

        setupListeners(fullscreenButton) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(this)
            }
            hideFloatingView()
            shouldHandleHomePress = false // 변경된 부분
        } // fullscreenButton 클릭 이벤트

        textView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handleActionDown(event, params)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    handleActionMove(event, params)
                    true
                }
                else -> false
            }
        }

        linearLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handleActionDown(event, params)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    handleActionMove(event, params)
                    true
                }
                else -> false
            }
        }
        setupFloatingView()
        val homeFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        Handler(Looper.getMainLooper()).post {
            registerReceiver(homeReceiver, homeFilter)
            isHomeReceiverRegistered = true
        }

        registerReceiver(menuReceiver, homeFilter)
        isMenuReceiverRegistered = true

    }

    override fun onDestroy() {
        super.onDestroy()
        temp.unregisterReceiver()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        if (isHomeReceiverRegistered) { // 추가된 부분
            unregisterReceiver(homeReceiver)
        }
        if (isMenuReceiverRegistered) { // 추가된 부분
            unregisterReceiver(menuReceiver)
        }

        shouldHandleHomePress = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_FLOATING -> {
                showFloatingView()
                // 플래시 상태를 확인하고 출력
                val isTorchOn = power.isTorchOn()
                shouldHandleHomePress = intent.getBooleanExtra("shouldHandleHomePress", false)

                val statusMap = saveStatus.readStatusFromFile()
                val isTorchOnStatus = statusMap["isTorchOn"]?.toBoolean() ?: false
                val currentLevel = statusMap["currentlevel"]?.toIntOrNull() ?: 0
                val sosTorchOnStatus = statusMap["sosStatus"]?.toBoolean() ?: false

                Log.d("아따이거당께", "on +$isTorchOnStatus level + $currentLevel  일단 + $isTorchOn")

                if (isTorchOn) {
                    powerButton.setBackgroundResource(R.drawable.button_on)
                    when (currentLevel) {
                        0 -> torch_level.setBackgroundResource(R.drawable.button_on_level1)
                        1 -> torch_level.setBackgroundResource(R.drawable.button_on_level2)
                        2 -> torch_level.setBackgroundResource(R.drawable.button_on_level3)
                        3 -> torch_level.setBackgroundResource(R.drawable.button_on_level4)
                        4 -> torch_level.setBackgroundResource(R.drawable.button_on_level5)
                        else -> torch_level.setBackgroundResource(R.drawable.button_on_level0)
                    }
                }
                else {
                    powerButton.setBackgroundResource(R.drawable.button_off)
                    torch_level.setBackgroundResource(R.drawable.button_on_level0)
                }
                return START_STICKY
            }
            ACTION_HIDE_FLOATING -> {
                shouldHandleHomePress = false
                hideFloatingView()
                return START_NOT_STICKY
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun toggleTorchLevel() {
        val statusMap = saveStatus.readStatusFromFile()
        val sosTorchOnStatus = statusMap["sosStatus"]?.toBoolean() ?: false
        val soscurrentLevel = statusMap["sosCurrentLevel"]?.toIntOrNull() ?: 0
        val isTorchOnStatus = statusMap["isTorchOn"]?.toBoolean() ?: false
        val currentLevel = statusMap["currentlevel"]?.toIntOrNull() ?: 0
        currentLevelpre = currentLevel
        currentLevelpre = (currentLevelpre % 5) + 1
        if (isTorchOnStatus) {
            when (currentLevelpre) {
                1 -> {
                    power.setTorchStrengthLevel(power.maxTorchStrength / 5 * 2)
                    torch_level.setBackgroundResource(R.drawable.button_on_level2)
                    powerButton.setBackgroundResource(R.drawable.button_on)
                    saveStatus.saveStatusToFile(1, isTorchOnStatus, soscurrentLevel, sosTorchOnStatus, false)
                    println("테스트중입니다1")
                }

                2 -> {
                    power.setTorchStrengthLevel(power.maxTorchStrength / 5 * 3)
                    torch_level.setBackgroundResource(R.drawable.button_on_level3)
                    powerButton.setBackgroundResource(R.drawable.button_on)
                    saveStatus.saveStatusToFile(2, isTorchOnStatus, soscurrentLevel, sosTorchOnStatus, false)
                    showWarningDialog()
                }

                3 -> {
                    power.setTorchStrengthLevel(power.maxTorchStrength / 5 * 4)
                    torch_level.setBackgroundResource(R.drawable.button_on_level4)
                    powerButton.setBackgroundResource(R.drawable.button_on)
                    saveStatus.saveStatusToFile(3, isTorchOnStatus, soscurrentLevel, sosTorchOnStatus, false)
                }

                4 -> {
                    power.setTorchStrengthLevel(power.maxTorchStrength)
                    torch_level.setBackgroundResource(R.drawable.button_on_level5)
                    powerButton.setBackgroundResource(R.drawable.button_on)
                    saveStatus.saveStatusToFile(4, isTorchOnStatus, soscurrentLevel, sosTorchOnStatus, false)
                }

                5 -> {
                    power.turnOffTorch()
                    torch_level.setBackgroundResource(R.drawable.button_on_level0)
                    powerButton.setBackgroundResource(R.drawable.button_off)
                    saveStatus.saveStatusToFile(0, false, soscurrentLevel, sosTorchOnStatus, false)
                }
            }
        }
        else{
            power.setTorchStrengthLevel(power.maxTorchStrength / 5 * 1)
            torch_level.setBackgroundResource(R.drawable.button_on_level1)
            powerButton.setBackgroundResource(R.drawable.button_on)
            saveStatus.saveStatusToFile(
                0, true, soscurrentLevel, sosTorchOnStatus, false)
            println("테스트중입니다2")

        }
    }

    private fun showWarningDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.warning_alert, null)
        val dialog = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // AlertDialog에 필요한 플래그 설정
        dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        dialog.show()

        // 플래그를 변경하여 터치 이벤트와 백 버튼 이벤트를 받을 수 있게 합니다.
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    private fun handleHomeButtonPress() {
        // 홈 버튼이 눌렸을 때 수행할 작업
        val statusMap = saveStatus.readStatusFromFile()
        val sosTorchOnStatus = statusMap["sosStatus"]?.toBoolean() ?: false
        val soscurrentLevel = statusMap["sosCurrentLevel"]?.toIntOrNull() ?: 0
        val isTorchOnStatus = statusMap["isTorchOn"]?.toBoolean() ?: false
        val currentLevel = statusMap["currentlevel"]?.toIntOrNull() ?: 0

        saveStatus.saveStatusToFile(currentLevel, isTorchOnStatus, soscurrentLevel, sosTorchOnStatus, false)
        Log.d("테스트용", "테스트입니다$currentLevel")
        Handler(Looper.getMainLooper()).postDelayed({
            if(isTorchOnStatus){
                if (currentLevel >= 0 && currentLevel < 4) {
                    power.setTorchStrengthLevel(power.maxTorchStrength / 5 * (currentLevel+1))
                } else if (currentLevel == 4) {
                    power.maxTorchStrength
                }
            }else{
                power.turnOffTorch()
            }
        }, 10)  // 10ms 지연
    }

    companion object {
        const val ACTION_SHOW_FLOATING = "com.example.test12.action.ActionShowFloating"
        const val ACTION_HIDE_FLOATING = "com.example.test12.action.HIDE_FLOATING"
    }

    private fun hideFloatingView() {
        if (::floatingView.isInitialized) {
            floatingView.visibility = View.GONE
        }
    }

    private fun showFloatingView() {
        if (!::floatingView.isInitialized) {
            setupFloatingView()
        }
        floatingView.visibility = View.VISIBLE
    }

    private fun setupListeners(imageButton: ImageButton, onClick: () -> Unit) {
        imageButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handleActionDown(event, params)
                    isMoving = false
                    // 버튼을 누를 때 애니메이션 시작
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    handleActionMove(event, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // 사용자가 버튼에서 손을 뗄 때 애니메이션을 원래대로 돌립니다
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction {
                        if (!isMoving) {
                            onClick() // 드래그가 아닐 때만 onClick 실행
                        }
                    }.start()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    // 터치가 취소될 경우도 애니메이션을 원래대로 돌립니다
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    true
                }
                else -> false
            }
        }
    }

    private fun handleActionDown(event: MotionEvent, params: WindowManager.LayoutParams) {
        initialX = params.x
        initialY = params.y
        initialTouchX = event.rawX
        initialTouchY = event.rawY
    }

    private fun handleActionMove(event: MotionEvent, params: WindowManager.LayoutParams) {
        val deltaX = (event.rawX - initialTouchX).toInt()
        val deltaY = (event.rawY - initialTouchY).toInt()
        if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
            isMoving = true
            params.x = initialX + deltaX
            params.y = initialY - deltaY
            windowManager.updateViewLayout(floatingView, params)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        // 앱이 최근 앱 화면에서 스와이프되어 제거될 때 호출
        power.turnOffTorch()
        stopSelf()
    }

    private fun setupFloatingView() {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 100
        }

        windowManager.addView(floatingView, params)
    }
}
