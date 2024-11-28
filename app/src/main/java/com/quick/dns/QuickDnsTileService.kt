package com.quick.dns

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.provider.Settings
import android.util.Log

class QuickDnsTileService : TileService() {
    private val modes = arrayOf("off", "opportunistic", "hostname")
    private var currentModeIndex = 0

    override fun onStartListening() {
        super.onStartListening()
        val currentMode = getCurrentPrivateDnsMode()
        currentModeIndex = modes.indexOf(currentMode).coerceAtLeast(0).coerceAtMost(modes.size - 1)
        updateTile(currentMode)
    }

    override fun onClick() {
        super.onClick()

        currentModeIndex = (currentModeIndex + 1) % modes.size
        val newMode = modes[currentModeIndex]

        val success = setPrivateDnsMode(newMode)

        if (success) {
            updateTile(newMode)
        }
    }

    private fun setPrivateDnsMode(mode: String): Boolean {
        return try {
            Settings.Global.putString(contentResolver, "private_dns_mode", mode)
            true
        } catch (e: Exception) {
            Log.e("QuickDnsTileService", "Failed to set DNS mode: ${e.message}")
            false
        }
    }

    private fun getCurrentPrivateDnsMode(): String {
        return try {
            Settings.Global.getString(contentResolver, "private_dns_mode") ?: "off"
        } catch (e: Exception) {
            Log.e("QuickDnsTileService", "Failed to get DNS mode: ${e.message}")
            "off"
        }
    }

    private fun updateTile(mode: String) {
        qsTile.subtitle = when (mode) {
            "off" -> "Off"
            "opportunistic" -> "Automatic"
            "hostname" -> "Custom"
            else -> "Unknown"
        }

        qsTile.state = when (mode) {
            "off" -> Tile.STATE_INACTIVE
            else -> Tile.STATE_ACTIVE
        }

        qsTile.updateTile()
    }
}
