package com.example.foldisland.core.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.foldisland.R
import com.example.foldisland.core.model.IslandMode
import com.example.foldisland.managers.BatteryMonitor
import com.example.foldisland.managers.HUDManager
import com.example.foldisland.managers.SystemEventsMonitor
import com.example.foldisland.ui.island.IslandRootView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Foreground service hosting the island overlay.
 *
 * Stability rules (root cause of earlier flicker):
 *  - The window NEVER uses WRAP_CONTENT. Compose animations happen inside a fixed-size window,
 *    so animation frames never trigger a WindowManager relayout.
 *  - The window is only resized on discrete state changes (pill / HUD / expanded),
 *    and the pill stays visually in place because content is centered.
 *  - A display-bound window context is used so fold/unfold delivers correct metrics.
 */
class IslandOverlayService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner {

    private enum class WindowSpec { PILL, HUD, EXPANDED }

    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private lateinit var windowContext: Context
    private var windowManager: WindowManager? = null
    private var container: IslandContainer? = null
    private var isViewAdded = false
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var currentSpec = WindowSpec.PILL

    private val _islandMode = MutableStateFlow<IslandMode>(IslandMode.Collapsed)
    val islandMode: StateFlow<IslandMode> = _islandMode.asStateFlow()

    /** Distance (dp) from physical top of screen to the top of the pill. Used by expanded layout. */
    private val _pillTopDp = MutableStateFlow(8f)
    val pillTopDp: StateFlow<Float> = _pillTopDp.asStateFlow()

    companion object {
        const val CHANNEL_ID = "dynamic_island_overlay_channel"
        const val NOTIFICATION_ID = 1001

        const val PILL_WIDTH_DP = 224
        const val PILL_HEIGHT_DP = 36
        private const val HUD_WINDOW_WIDTH_DP = 320
        private const val COMPACT_WINDOW_HEIGHT_DP = 40
        private const val HUD_EXIT_ANIM_MS = 450L

        @Volatile
        var isRunning = false
            private set

        fun startService(context: Context) {
            val intent = Intent(context, IslandOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, IslandOverlayService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        BatteryMonitor.start(this)
        SystemEventsMonitor.start(this)

        windowContext = createOverlayContext()
        windowManager = windowContext.getSystemService(WINDOW_SERVICE) as WindowManager

        setupOverlayView()
        observeWindowSpec()
        isRunning = true
    }

    private fun createOverlayContext(): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val dm = getSystemService(DisplayManager::class.java)
            val display = dm.getDisplay(Display.DEFAULT_DISPLAY) ?: return this
            createDisplayContext(display)
                .createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null)
        } else {
            this
        }
    }

    private fun dp(value: Number): Int =
        (value.toFloat() * windowContext.resources.displayMetrics.density + 0.5f).toInt()

    private fun screenWidthPx(): Int = windowContext.resources.displayMetrics.widthPixels

    /** Places the pill vertically centered on the front camera punch-hole when one exists. */
    private fun computePillTopPx(): Int {
        val fallback = dp(6)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return fallback
        return try {
            val metrics = windowManager?.currentWindowMetrics ?: return fallback
            val cutout = metrics.windowInsets.displayCutout ?: return fallback
            val screenW = metrics.bounds.width()
            val hole = cutout.boundingRects
                .filter { it.top < dp(80) }
                .minByOrNull { kotlin.math.abs(it.centerX() - screenW / 2) }
                ?: return fallback
            (hole.centerY() - dp(PILL_HEIGHT_DP) / 2).coerceAtLeast(0)
        } catch (e: Exception) {
            fallback
        }
    }

    private fun overlayType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    private fun setupOverlayView() {
        if (isViewAdded) return

        layoutParams = WindowManager.LayoutParams(
            dp(PILL_WIDTH_DP + 8),
            dp(COMPACT_WINDOW_HEIGHT_DP),
            overlayType(),
            compactFlags(),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = computePillTopPx()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        updatePillTopDp()

        val composeView = ComposeView(windowContext).apply {
            setContent {
                IslandRootView(
                    service = this@IslandOverlayService,
                    onModeChanged = { newMode -> _islandMode.value = newMode }
                )
            }
        }

        container = IslandContainer(windowContext).apply {
            setViewTreeLifecycleOwner(this@IslandOverlayService)
            setViewTreeViewModelStoreOwner(this@IslandOverlayService)
            setViewTreeSavedStateRegistryOwner(this@IslandOverlayService)
            addView(
                composeView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        try {
            windowManager?.addView(container, layoutParams)
            isViewAdded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun compactFlags(): Int =
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

    private fun expandedFlags(): Int =
        // Focusable so the notes text field and the back key work while expanded
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

    private fun observeWindowSpec() {
        lifecycleScope.launch {
            combine(_islandMode, HUDManager.currentHUD) { mode, hud ->
                when {
                    mode is IslandMode.Expanded -> WindowSpec.EXPANDED
                    hud != null -> WindowSpec.HUD
                    else -> WindowSpec.PILL
                }
            }
                .distinctUntilChanged()
                .collectLatest { spec ->
                    // Shrinking HUD -> pill waits for the fade-out so content is never clipped
                    if (spec == WindowSpec.PILL && currentSpec == WindowSpec.HUD) {
                        delay(HUD_EXIT_ANIM_MS)
                    }
                    applyWindowSpec(spec)
                }
        }
    }

    private fun applyWindowSpec(spec: WindowSpec, force: Boolean = false) {
        if (!isViewAdded || container == null) return
        if (spec == currentSpec && !force) return
        currentSpec = spec

        val pillTop = computePillTopPx()
        when (spec) {
            WindowSpec.PILL -> {
                layoutParams.width = dp(PILL_WIDTH_DP + 8)
                layoutParams.height = dp(COMPACT_WINDOW_HEIGHT_DP)
                layoutParams.y = pillTop
                layoutParams.flags = compactFlags()
            }
            WindowSpec.HUD -> {
                layoutParams.width = minOf(dp(HUD_WINDOW_WIDTH_DP), screenWidthPx())
                layoutParams.height = dp(COMPACT_WINDOW_HEIGHT_DP)
                layoutParams.y = pillTop
                layoutParams.flags = compactFlags()
            }
            WindowSpec.EXPANDED -> {
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.y = 0
                layoutParams.flags = expandedFlags()
            }
        }
        updatePillTopDp()
        try {
            windowManager?.updateViewLayout(container, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePillTopDp() {
        _pillTopDp.value = computePillTopPx() / windowContext.resources.displayMetrics.density
    }

    /** Fold / unfold / rotation: collapse and re-anchor to the new display's camera cutout. */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        _islandMode.value = IslandMode.Collapsed
        container?.post { applyWindowSpec(currentSpec, force = true) }
    }

    override fun onDestroy() {
        isRunning = false
        SystemEventsMonitor.stop(this)
        if (isViewAdded && container != null) {
            try {
                windowManager?.removeView(container)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isViewAdded = false
        }
        container = null
        super.onDestroy()
    }

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    /** Root view that turns the BACK key into "collapse island" while expanded. */
    private inner class IslandContainer(context: Context) : FrameLayout(context) {
        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if (event.keyCode == KeyEvent.KEYCODE_BACK && _islandMode.value is IslandMode.Expanded) {
                if (event.action == KeyEvent.ACTION_UP) {
                    _islandMode.value = IslandMode.Collapsed
                }
                return true
            }
            return super.dispatchKeyEvent(event)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dynamic Island Aktif")
            .setContentText("Fold Island çentik servisi çalışıyor")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
