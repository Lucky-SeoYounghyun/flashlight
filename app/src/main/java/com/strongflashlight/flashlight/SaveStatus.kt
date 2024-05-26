package com.example.flashlight

import android.content.Context
import java.io.File
import java.io.FileOutputStream

class SaveStatus(private val context: Context) {

    fun saveStatusToFile(currentLevel: Int, torchStatus: Boolean, sosCurrentLevel: Int, sosStatus: Boolean, flag: Boolean) {
        val statusText = """
            currentlevel = [$currentLevel]
            isTorchOn = [$torchStatus]
            soscurrentlevel = [$sosCurrentLevel]
            sosisTorchOn = [$sosStatus]
            flag = [$flag]
        """.trimIndent()

        val file = File(context.filesDir, "status.txt")
        FileOutputStream(file).use { output ->
            output.write(statusText.toByteArray())
        }
    }

    fun readStatusFromFile(): Map<String, String> {
        val file = File(context.filesDir, "status.txt")
        if (!file.exists()) return emptyMap()

        val statusMap = mutableMapOf<String, String>()
        file.forEachLine { line ->
            val parts = line.split("=").map { it.trim() }
            if (parts.size == 2) {
                val key = parts[0]
                val value = parts[1].removeSurrounding("[", "]")
                statusMap[key] = value
            }
        }
        return statusMap
    }

    fun updateCurrentLevel(newCurrentLevel: Int) {
        val statusMap = readStatusFromFile().toMutableMap()
        statusMap["currentlevel"] = newCurrentLevel.toString()

        val updatedStatusText = statusMap.entries.joinToString("\n") { (key, value) ->
            "$key = [$value]"
        }

        val file = File(context.filesDir, "status.txt")
        FileOutputStream(file).use { output ->
            output.write(updatedStatusText.toByteArray())
        }
    }
    fun updateFlag(newFlag: Boolean) {
        val statusMap = readStatusFromFile().toMutableMap()
        statusMap["flag"] = newFlag.toString()

        val updatedStatusText = statusMap.entries.joinToString("\n") { (key, value) ->
            "$key = [$value]"
        }

        val file = File(context.filesDir, "status.txt")
        FileOutputStream(file).use { output ->
            output.write(updatedStatusText.toByteArray())
        }
    }
}
