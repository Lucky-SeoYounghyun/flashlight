package com.example.test12


import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.example.test12.MainActivity
import android.widget.TextView
import android.app.AlertDialog
import android.widget.LinearLayout

class FloatingService : Service() {
    private lateinit var power: Power
    private lateinit var powerButton: ImageButton
    private lateinit var torch_level: View
    private lateinit var closeButton: ImageButton
    private lateinit var fullscreenButton: ImageButton
    private var currentLevel = 0
    private lateinit var windowManager: WindowManager
    private lateinit var linearLayout : LinearLayout
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var isMoving: Boolean = false

    private lateinit var temp: Temp
    private lateinit var textView: TextView

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

        power = Power(this)
        power.initializeCamera()

        setupListeners(powerButton) {
            toggleTorchLevel()
        } //powerButton 클릭 이벤트

        setupListeners(closeButton) {
            stopSelf()
        } //closebutton 클릭 이벤트

        setupListeners(fullscreenButton) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(this)
            }
            hideFloatingView()
        } //fullscreenbutton 클릭 이벤트

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
    }

    private fun toggleTorchLevel() {
        currentLevel = (currentLevel % 6) + 1  // 다음 강도 레벨
        when (currentLevel) {
            1 -> {
                power.setTorchStrengthLevel(power.maxTorchStrength/5*1)
                torch_level.setBackgroundResource(R.drawable.button_on_level1)
                powerButton.setBackgroundResource(R.drawable.button_on)
            }
            2 -> {
                power.setTorchStrengthLevel(power.maxTorchStrength/5*2)
                torch_level.setBackgroundResource(R.drawable.button_on_level2)
                powerButton.setBackgroundResource(R.drawable.button_on)
            }
            3 -> {
                power.setTorchStrengthLevel(power.maxTorchStrength/5*3)
                torch_level.setBackgroundResource(R.drawable.button_on_level3)
                powerButton.setBackgroundResource(R.drawable.button_on)
                showWarningDialog()
            }
            4 -> {
                power.setTorchStrengthLevel(power.maxTorchStrength/5*4)
                torch_level.setBackgroundResource(R.drawable.button_on_level4)
                powerButton.setBackgroundResource(R.drawable.button_on)
            }
            5 -> {
                power.setTorchStrengthLevel(power.maxTorchStrength)
                println(power.maxTorchStrength)
                torch_level.setBackgroundResource(R.drawable.button_on_level5)
                powerButton.setBackgroundResource(R.drawable.button_on)
            }
            6 -> {
                // 6번째 클릭에서 토치를 끔
                power.turnOffTorch()
                torch_level.setBackgroundResource(R.drawable.button_on_level0)
                powerButton.setBackgroundResource(R.drawable.button_off)
                currentLevel = 0  // 강도를 다시 0으로 초기화
            }
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

    companion object {
        const val ACTION_SHOW_FLOATING = "com.example.test12.action.ActionShowFloating"
        const val ACTION_HIDE_FLOATING = "com.example.test12.action.HIDE_FLOATING"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_FLOATING -> {
                showFloatingView()
                return START_STICKY
            }
            ACTION_HIDE_FLOATING -> {
                hideFloatingView()
                return START_NOT_STICKY
            }
        }
        return super.onStartCommand(intent, flags, startId)
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

    override fun onDestroy() {
        super.onDestroy()
        temp.unregisterReceiver()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}


