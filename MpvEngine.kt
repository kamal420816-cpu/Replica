package com.example.fluidplay

import android.content.Context
import android.view.Surface
import dev.jdtech.mpv.MPVLib

/**
 * Thin wrapper around libmpv. Uses only mpv's built-in, open-source options.
 * The MPVLib calls follow the common mpv-android wrapper API; if your chosen
 * wrapper version names them differently, adjust here (this is the only file
 * that touches the library).
 */
class MpvEngine(private val context: Context) {

    private var started = false

    fun start(settings: PlayerSettings) {
        if (started) return
        MPVLib.create(context)
        MPVLib.setOptionString("vo", "gpu")
        MPVLib.setOptionString("gpu-context", "android")
        MPVLib.setOptionString("ao", "audiotrack,opensles")
        MPVLib.setOptionString("keep-open", "yes")
        applyOptions(settings, beforeInit = true)
        MPVLib.init()
        started = true
    }

    private fun applyOptions(s: PlayerSettings, beforeInit: Boolean) {
        val set: (String, String) -> Unit =
            if (beforeInit) MPVLib::setOptionString else MPVLib::setPropertyString

        set("hwdec", if (s.hardwareDecoding) "mediacodec" else "no")

        if (s.smooth == SmoothMode.OFF) {
            set("interpolation", "no")
            set("video-sync", "audio")
        } else {
            set("video-sync", "display-resample")
            set("interpolation", "yes")
            set("tscale", s.smooth.tscale!!)
        }
        // Optional resolution cap for weak devices
        if (s.maxHeight > 0) {
            set("vf", "scale=-2:min(ih\\,${s.maxHeight})")
        } else if (!beforeInit) {
            set("vf", "")
        }
    }

    fun update(settings: PlayerSettings) {
        if (started) applyOptions(settings, beforeInit = false)
    }

    fun attachSurface(surface: Surface) {
        MPVLib.attachSurface(surface)
        MPVLib.setOptionString("force-window", "yes")
    }

    fun detachSurface() = MPVLib.detachSurface()

    fun load(uriOrUrl: String) {
        MPVLib.command(arrayOf("loadfile", uriOrUrl))
    }

    fun togglePause() {
        MPVLib.command(arrayOf("cycle", "pause"))
    }

    fun seekBy(seconds: Int) {
        MPVLib.command(arrayOf("seek", seconds.toString(), "relative"))
    }

    fun setSpeed(speed: Double) {
        MPVLib.setPropertyDouble("speed", speed)
    }

    fun destroy() {
        if (started) {
            MPVLib.destroy()
            started = false
        }
    }
}
