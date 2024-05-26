package com.strongflashlight.flashlight

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.widget.Toast

class Power(private val context: Context) {
    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    var cameraId: String? = null
    var maxTorchStrength = 1  // 최대 강도 기본값
    private var currentTorchStrength = 0  // 현재 토치 강도를 저장
    private var isTorchOn = false  // 플래시 상태를 추적하기 위한 변수

    init {
        initializeCamera()
    }

    fun getCurrentTorchStrengthLevel(): Int {
        return currentTorchStrength
    }

    fun initializeCamera() {
        // 보안처리되었습니다

    }

    private fun registerTorchCallback(cameraId: String) {
        // 보안처리되었습니다

    }

    fun setTorchStrengthLevel(strengthLevel: Int) {
        // 보안처리되었습니다

    }

    fun turnOffTorch() {
        // 보안처리되었습니다

    }

    fun isTorchOn(): Boolean {
        return isTorchOn
    }
}