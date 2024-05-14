package com.example.test12

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.widget.SeekBar

class Power(private val context: Context) {
    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    var cameraId: String? = null
    var maxTorchStrength = 1  // 최대 강도 기본값

    fun initializeCamera() {
        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                if (flashAvailable) {
                    cameraId = id
                    maxTorchStrength = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: maxTorchStrength
                    return
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun setTorchStrengthLevel(strengthLevel: Int) {
        cameraId?.let { id ->
            try {
                cameraManager.setTorchMode(id, false)  // 토치 모드를 끈 다음 다시 켭니다.
                cameraManager.turnOnTorchWithStrengthLevel(id, strengthLevel)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    fun turnOffTorch() {
        cameraId?.let { id ->
            try {
                cameraManager.setTorchMode(id, false)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }
}