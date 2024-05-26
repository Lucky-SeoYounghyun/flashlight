package com.strongflashlight.flashlight

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdLoader
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import android.view.View
import android.content.SharedPreferences

class SettingActivity : ComponentActivity() {

    private lateinit var adLoader: AdLoader
    private lateinit var flashlightSwitch: Switch
    private lateinit var flashlightSeekBar: SeekBar
    private lateinit var brightnessLevel: TextView
    private lateinit var levelLayout: View
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // 보안처리되었습니다
    }

    private fun loadSettings() {
        // 보안처리되었습니다

    }

    private fun saveSettings() {
        // 보안처리되었습니다

    }

    private fun getDataFromAssets(): List<ListData> {
        // 보안처리되었습니다

    }

    private fun loadNativeAd() {
        // 보안처리되었습니다

    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // 보안처리되었습니다

    }
}