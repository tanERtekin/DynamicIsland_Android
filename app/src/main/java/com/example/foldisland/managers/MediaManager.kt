package com.example.foldisland.managers

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat
import com.example.foldisland.core.model.MediaTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MediaManager {
    private val _currentTrack = MutableStateFlow(MediaTrack())
    val currentTrack: StateFlow<MediaTrack> = _currentTrack.asStateFlow()

    private var activeController: MediaController? = null

    fun hasNotificationAccess(context: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    fun updateMediaInfo(
        title: String,
        artist: String,
        isPlaying: Boolean,
        durationMs: Long = 0,
        appPackage: String? = null,
        albumArt: Bitmap? = null
    ) {
        _currentTrack.value = MediaTrack(
            title = title.ifBlank { "Bilinmeyen Parça" },
            artist = artist.ifBlank { "Bilinmeyen Sanatçı" },
            isPlaying = isPlaying,
            durationMs = durationMs,
            appPackage = appPackage,
            albumArt = albumArt
        )
    }

    fun clear() {
        _currentTrack.value = MediaTrack()
    }

    fun togglePlayPause() {
        val controls = activeController?.transportControls
        if (controls == null) {
            // Simulation mode (no real session attached)
            _currentTrack.value = _currentTrack.value.copy(isPlaying = !_currentTrack.value.isPlaying)
            return
        }
        if (_currentTrack.value.isPlaying) controls.pause() else controls.play()
    }

    fun skipNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    internal fun setActiveController(controller: MediaController?) {
        activeController = controller
    }
}

/**
 * Tracks the active media session LIVE (Spotify, YouTube Music, etc.).
 * Previously the session was read only once at connect, so track/play state never updated.
 */
class MediaNotificationListenerService : NotificationListenerService() {

    private var sessionManager: MediaSessionManager? = null
    private var current: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
        override fun onSessionDestroyed() = attach(null)
    }

    private val sessionsListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers -> pick(controllers) }

    override fun onListenerConnected() {
        super.onListenerConnected()
        val manager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
        sessionManager = manager
        val component = ComponentName(this, MediaNotificationListenerService::class.java)
        try {
            manager.addOnActiveSessionsChangedListener(sessionsListener, component)
            pick(manager.getActiveSessions(component))
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onListenerDisconnected() {
        sessionManager?.removeOnActiveSessionsChangedListener(sessionsListener)
        attach(null)
        super.onListenerDisconnected()
    }

    private fun pick(controllers: List<MediaController>?) {
        val playing = controllers?.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
        attach(playing ?: controllers?.firstOrNull())
    }

    private fun attach(controller: MediaController?) {
        if (controller != null && controller.sessionToken == current?.sessionToken) {
            publish()
            return
        }
        current?.unregisterCallback(controllerCallback)
        current = controller
        controller?.registerCallback(controllerCallback)
        MediaManager.setActiveController(controller)
        publish()
    }

    private fun publish() {
        val controller = current
        if (controller == null) {
            MediaManager.clear()
            return
        }
        val metadata = controller.metadata
        val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
        MediaManager.updateMediaInfo(
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty(),
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
            isPlaying = isPlaying,
            durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
            appPackage = controller.packageName,
            albumArt = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        )
    }
}
