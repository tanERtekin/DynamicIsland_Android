package com.example.foldisland.managers

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import com.example.foldisland.core.model.MediaTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

object MediaManager {
    private val _currentTrack = MutableStateFlow(MediaTrack())
    val currentTrack: StateFlow<MediaTrack> = _currentTrack.asStateFlow()

    private val _equalizerBars = MutableStateFlow(listOf(0.2f, 0.5f, 0.8f, 0.3f))
    val equalizerBars: StateFlow<List<Float>> = _equalizerBars.asStateFlow()

    private var activeController: MediaController? = null
    private val managerScope = CoroutineScope(Dispatchers.Default)

    init {
        // Equalizer animation ticker when media is playing
        managerScope.launch {
            while (isActive) {
                if (_currentTrack.value.isPlaying) {
                    _equalizerBars.value = listOf(
                        Random.nextFloat().coerceIn(0.2f, 1.0f),
                        Random.nextFloat().coerceIn(0.3f, 1.0f),
                        Random.nextFloat().coerceIn(0.1f, 0.9f),
                        Random.nextFloat().coerceIn(0.4f, 1.0f)
                    )
                    delay(120)
                } else {
                    delay(600)
                }
            }
        }
    }

    fun updateMediaInfo(title: String, artist: String, isPlaying: Boolean, durationMs: Long = 0, appPackage: String? = null) {
        _currentTrack.value = _currentTrack.value.copy(
            title = title.ifBlank { "Bilinmeyen Parça" },
            artist = artist.ifBlank { "Bilinmeyen Sanatçı" },
            isPlaying = isPlaying,
            durationMs = durationMs,
            appPackage = appPackage
        )
    }

    fun togglePlayPause() {
        activeController?.transportControls?.let { controls ->
            if (_currentTrack.value.isPlaying) {
                controls.pause()
                _currentTrack.value = _currentTrack.value.copy(isPlaying = false)
            } else {
                controls.play()
                _currentTrack.value = _currentTrack.value.copy(isPlaying = true)
            }
        } ?: run {
            // Fallback for simulation / mock testing
            _currentTrack.value = _currentTrack.value.copy(isPlaying = !_currentTrack.value.isPlaying)
        }
    }

    fun skipNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    fun setActiveController(controller: MediaController?) {
        activeController = controller
    }
}

class MediaNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        val sessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
        val componentName = ComponentName(this, MediaNotificationListenerService::class.java)

        try {
            val controllers = sessionManager.getActiveSessions(componentName)
            if (controllers.isNotEmpty()) {
                val primary = controllers[0]
                MediaManager.setActiveController(primary)
                val metadata = primary.metadata
                val state = primary.playbackState
                val isPlaying = state?.state == PlaybackState.STATE_PLAYING

                val title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: "Çalan Parça"
                val artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                MediaManager.updateMediaInfo(title, artist, isPlaying, appPackage = primary.packageName)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
