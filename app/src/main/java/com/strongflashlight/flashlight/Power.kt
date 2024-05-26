package com.example.flashlight

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

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
        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                if (flashAvailable) {
                    cameraId = id
                    maxTorchStrength = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: maxTorchStrength
                    registerTorchCallback(id)
                    return
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun registerTorchCallback(cameraId: String) {
        cameraManager.registerTorchCallback(object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(id: String, enabled: Boolean) {
                if (cameraId == id) {
                    isTorchOn = enabled
                }
            }

            override fun onTorchModeUnavailable(id: String) {
                if (cameraId == id) {
                    isTorchOn = false
                }
            }
        }, null)
    }

    fun setTorchStrengthLevel(strengthLevel: Int) {
        cameraId?.let { id ->
            try {
                cameraManager.turnOnTorchWithStrengthLevel(id, strengthLevel)
                currentTorchStrength = strengthLevel
                isTorchOn = strengthLevel > 0
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    fun turnOffTorch() {
        cameraId?.let { id ->
            try {
                cameraManager.setTorchMode(id, false)
                isTorchOn = false
                currentTorchStrength = 0
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    fun isTorchOn(): Boolean {
        return isTorchOn
    }
}