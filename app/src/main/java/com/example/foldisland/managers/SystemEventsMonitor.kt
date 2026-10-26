package com.example.foldisland.managers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Listens to real system events and turns them into island HUDs:
 *  - Media volume changes  -> Volume HUD
 *  - Manual brightness changes -> Brightness HUD
 */
object SystemEventsMonitor {
    private const val ACTION_VOLUME_CHANGED = "android.media.VOLUME_CHANGED_ACTION"
    private const val EXTRA_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"

    private var volumeReceiver: BroadcastReceiver? = null
    private var brightnessObserver: ContentObserver? = null
    private var lastVolumePercent = -1
    private var lastBrightnessPercent = -1

    fun start(context: Context) {
        val appContext = context.applicationContext
        registerVolume(appContext)
        registerBrightness(appContext)
    }

    fun stop(context: Context) {
        val appContext = context.applicationContext
        volumeReceiver?.let { runCatching { appContext.unregisterReceiver(it) } }
        brightnessObserver?.let { appContext.contentResolver.unregisterContentObserver(it) }
        volumeReceiver = null
        brightnessObserver = null
    }

    private fun registerVolume(context: Context) {
        if (volumeReceiver != null) return
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        lastVolumePercent = musicVolumePercent(audio)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val stream = intent?.getIntExtra(EXTRA_STREAM_TYPE, -1) ?: return
                if (stream != AudioManager.STREAM_MUSIC) return
                val pct = musicVolumePercent(audio)
                if (pct != lastVolumePercent) {
                    lastVolumePercent = pct
                    HUDManager.showVolume(pct)
                }
            }
        }
        ContextCompat.registerReceiver(
            context, receiver, IntentFilter(ACTION_VOLUME_CHANGED), ContextCompat.RECEIVER_EXPORTED
        )
        volumeReceiver = receiver
    }

    private fun musicVolumePercent(audio: AudioManager): Int {
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        return audio.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / max
    }

    private fun registerBrightness(context: Context) {
        if (brightnessObserver != null) return
        val resolver = context.contentResolver
        lastBrightnessPercent = brightnessPercent(context)

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                // Ignore automatic adjustments; only react to user changes in manual mode
                val mode = Settings.System.getInt(
                    resolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                if (mode != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) return
                val pct = brightnessPercent(context)
                if (pct != lastBrightnessPercent) {
                    lastBrightnessPercent = pct
                    HUDManager.showBrightness(pct)
                }
            }
        }
        resolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), false, observer
        )
        brightnessObserver = observer
    }

    private fun brightnessPercent(context: Context): Int {
        val raw = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
        return (raw * 100 / 255).coerceIn(0, 100)
    }
}
