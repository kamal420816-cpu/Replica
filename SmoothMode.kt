package com.example.fluidplay

/**
 * Free smoothing presets built on mpv's own interpolation (frame blending).
 * Each preset maps to a temporal scaler; "Off" disables interpolation.
 */
enum class SmoothMode(val label: String, val tscale: String?) {
    OFF("Off", null),
    LIGHT("Light", "linear"),
    BALANCED("Balanced", "mitchell"),
    STRONG("Strong", "oversample");
}

data class PlayerSettings(
    val smooth: SmoothMode = SmoothMode.BALANCED,
    val hardwareDecoding: Boolean = true,
    val maxHeight: Int = 0 // 0 = no cap, otherwise 1080 / 720
)
