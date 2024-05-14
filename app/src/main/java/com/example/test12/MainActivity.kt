package com.example.test12


import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import android.view.View
import android.widget.ImageButton
import android.widget.TextView





class MainActivity : ComponentActivity() {

    lateinit var btnFloating: Button
    lateinit var btnPower: ImageButton
    lateinit var btnScreen: Button
    lateinit var seekBar: SeekBar
    private lateinit var power: Power
    private var isTorchOn = false
    lateinit var textBatteryTemp: TextView
    private lateinit var temp: Temp
    lateinit var ChargeBattery: TextView
    private lateinit var batteryStatus: BatteryStatus


    override fun onResume() {
        super.onResume()
        Intent(this, FloatingService::class.java).apply {
            action = FloatingService.ACTION_HIDE_FLOATING
            startService(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnFloating = findViewById(R.id.btnFloating)
        btnScreen = findViewById(R.id.btnScreen)
        //--------------------여기는 배터리 시작--------------------

        // 배터리 온도 TextView 초기화 및 Temp 클래스 인스턴스 생성
        textBatteryTemp = findViewById(R.id.textBatteryTemp)
        temp = Temp(this, textBatteryTemp)


        // 배터리 충전 상태 TextView 초기화 및 BatteryStatus 클래스 인스턴스 생성
        ChargeBattery = findViewById(R.id.ChargeBattery)
        batteryStatus = BatteryStatus(this, ChargeBattery)
        batteryStatus.updateBatteryStatus()

        //--------------------여기는 온도 시작--------------------
        textBatteryTemp = findViewById(R.id.textBatteryTemp)
        temp = Temp(this, textBatteryTemp)



        //--------------------여기는 플로팅 뷰 컨트롤 시작--------------------

        btnFloating.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            }
            Intent(this, FloatingService::class.java).also { intent ->
                intent.action = FloatingService.ACTION_SHOW_FLOATING
                startService(intent)
                finish()
            }
        }
        //--------------------여기는 플로팅 뷰 컨트롤 종료--------------------


        //--------------------여기는 토치 컨트롤 시작--------------------
        power = Power(this)
        power.initializeCamera()
        btnPower = findViewById(R.id.btnPower)
        seekBar = findViewById(R.id.seekBar)
        seekBar.max = power.maxTorchStrength - 1
        seekBar.progress = 0

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Only update the torch strength if the torch is currently on
                if (isTorchOn) {
                    power.setTorchStrengthLevel(progress + 1) // progress + 1 because levels start from 1
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: 터치 이벤트시 시작
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: 터치 이벤트 종료시 시작
            }
        })

        btnPower.setOnClickListener {
            isTorchOn = !isTorchOn
            if (isTorchOn) {
                power.setTorchStrengthLevel(seekBar.progress + 1)
                btnPower.setBackgroundResource(R.drawable.main_button_on_ver2)

            } else {
                power.turnOffTorch()
                btnPower.setBackgroundResource(R.drawable.main_button_off)
            }
            seekBar.visibility = if (seekBar.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        //--------------------여기는 토치 컨트롤 종료--------------------


        //--------------------여기는 화면 밝기 컨트롤--------------------
        btnScreen.setOnClickListener {
            val intent = Intent(MainActivity@this, FullWhiteActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Temp 클래스의 리시버 등록 해제
        temp.unregisterReceiver()
    }
}


