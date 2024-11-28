package com.quick.dns

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.topjohnwu.superuser.Shell

class QuickDnsTileService : TileService() {
    private val modes = arrayOf("off", "opportunistic", "hostname") // DNS modes
    private var currentModeIndex = 0

    override fun onStartListening() {
        super.onStartListening()
        // Fetch the current DNS mode
        val currentMode = getCurrentPrivateDnsMode()
        currentModeIndex = modes.indexOf(currentMode).coerceAtLeast(0).coerceAtMost(modes.size - 1)
        updateTile(currentMode)
    }

    override fun onClick() {
        super.onClick()

        // Cycle to the next mode
        currentModeIndex = (currentModeIndex + 1) % modes.size
        val newMode = modes[currentModeIndex]

        // Apply the new mode and update the tile state
        val success = setPrivateDnsMode(newMode)

        // If successful, update the tile label and state
        if (success) {
            updateTile(newMode)
        }
    }

    private fun setPrivateDnsMode(mode: String): Boolean {
        return try {
            Shell.cmd("settings put global private_dns_mode $mode").exec().isSuccess
        } catch (e: Exception) {
            false
        }
    }

    private fun getCurrentPrivateDnsMode(): String {
        return try {
            Shell.cmd("settings get global private_dns_mode").exec().out.firstOrNull() ?: "off"
        } catch (e: Exception) {
            "off"
        }
    }

    private fun updateTile(mode: String) {
        // Set subtitle based on current mode
        qsTile.subtitle = when (mode) {
            "off" -> "Off"
            "opportunistic" -> "Automatic"
            "hostname" -> "Custom"
            else -> "Unknown"
        }

        // Change the tile state based on the mode
        qsTile.state = when (mode) {
            "off" -> Tile.STATE_INACTIVE // Set tile to inactive when mode is off
            else -> Tile.STATE_ACTIVE // Active state for other modes
        }

        // Update the tile
        qsTile.updateTile()
    }
}
