package com.strongflashlight.flashlight

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

class FloatingService : Service() {
    private lateinit var powerButton: ImageButton
    private lateinit var closeButton: ImageButton
    private lateinit var fullscreenButton: ImageButton
    private lateinit var torch_level: View
    private lateinit var windowManager: WindowManager
    private lateinit var linearLayout: LinearLayout
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var isMoving: Boolean = false
    private lateinit var textView: TextView
    private lateinit var saveStatus: SaveStatus

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

        saveStatus = SaveStatus(this)

        setupListeners(powerButton) {
            toggleTorchLevel()
        }

        setupListeners(closeButton) {
            stopSelf()
        }

        setupListeners(fullscreenButton) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(this)
            }
            hideFloatingView()
        }

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

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        if (isHomeReceiverRegistered) {
            unregisterReceiver(homeReceiver)
        }
        if (isMenuReceiverRegistered) {
            unregisterReceiver(menuReceiver)
        }
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

    private fun toggleTorchLevel() {
        // 보안처리되었습니다
    }

    private fun showWarningDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.warning_alert, null)
        val dialog = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        dialog.show()

        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    private fun handleHomeButtonPress() {
        // 보안처리되었습니다
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
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    handleActionMove(event, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction {
                        if (!isMoving) {
                            onClick()
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

    private fun handleActionDown(event: MotionEvent, params: WindowManager.LayoutParams) {
        // 보안처리되었습니다

    }

    private fun handleActionMove(event: MotionEvent, params: WindowManager.LayoutParams) {
        // 보안처리되었습니다

    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
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
