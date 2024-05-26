package com.strongflashlight.flashlight

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import android.content.SharedPreferences
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
    lateinit var seekBar: SeekBar
    lateinit var textBatteryTemp: TextView
    lateinit var ChargeBattery: TextView
    lateinit var batteryImageView: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this) {}

        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        btnFloating = findViewById(R.id.btnFloating)
        btnScreen = findViewById(R.id.btnScreen)
        btnSOS = findViewById(R.id.btnSOS)
        btnPower = findViewById(R.id.btnPower)
        seekBar = findViewById(R.id.seekBar)
        sosSeekBar = findViewById(R.id.sosSeekBar)
        btnSetting = findViewById(R.id.btnsetting)
        leftbutton = findViewById(R.id.leftbutton)
        rightbutton = findViewById(R.id.rightbutton)
        textBatteryTemp = findViewById(R.id.textBatteryTemp)
        ChargeBattery = findViewById(R.id.ChargeBattery)
        batteryImageView = findViewById(R.id.batteryImageView)

        sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE)
    }

    override fun onResume() {
        super.onResume()
        val statusMap = readStatusFromFile()
        updateUIBasedOnStatus(statusMap)
    }

    override fun onPause() {
        super.onPause()
        saveStatusToFile(seekBar.progress, false, sosSeekBar.progress, false, false)
    }

    private fun updateUIBasedOnStatus(statusMap: Map<String, String>) {
        // 보안처리되었습니다
    }

    private fun readStatusFromFile(): Map<String, String> {
        return mapOf()
    }

    private fun saveStatusToFile(level: Int, isTorchOn: Boolean, sosLevel: Int, isSOSActive: Boolean, flag: Boolean) {
        // 보안처리되었습니다
    }

    private fun setTouchListener(button: Button, normalBackground: Int, pressedBackground: Int, onClick: () -> Unit) {
        // 보안처리되었습니다
    }

    private fun showPermissionAlertDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_permission_alert)
        dialog.findViewById<Button>(R.id.dialog_button_positive).setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.dialog_button_negative).setOnClickListener {
            dialog.dismiss()
        }
    }
}
