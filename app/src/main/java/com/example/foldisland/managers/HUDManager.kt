package com.example.foldisland.managers

import com.example.foldisland.core.model.ActiveHUD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object HUDManager {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var dismissJob: Job? = null

    private val _currentHUD = MutableStateFlow<ActiveHUD?>(null)
    val currentHUD: StateFlow<ActiveHUD?> = _currentHUD.asStateFlow()

    private val _hudDismissEvents = MutableSharedFlow<Unit>()
    val hudDismissEvents: SharedFlow<Unit> = _hudDismissEvents.asSharedFlow()

    fun showVolume(level: Int, max: Int = 100) {
        triggerHUD(ActiveHUD.Volume(level, max))
    }

    fun showBrightness(level: Int, max: Int = 100) {
        triggerHUD(ActiveHUD.Brightness(level, max))
    }

    fun showCharging(percentage: Int, isWireless: Boolean = false) {
        triggerHUD(ActiveHUD.Charging(percentage, isWireless))
    }

    fun showPinnedNotice(emoji: String, message: String) {
        triggerHUD(ActiveHUD.PinnedNotice(emoji, message))
    }

    private fun triggerHUD(hud: ActiveHUD, durationMs: Long = 2500L) {
        dismissJob?.cancel()
        _currentHUD.value = hud
        dismissJob = scope.launch {
            delay(durationMs)
            _currentHUD.value = null
            _hudDismissEvents.emit(Unit)
        }
    }

    fun dismiss() {
        dismissJob?.cancel()
        _currentHUD.value = null
        scope.launch { _hudDismissEvents.emit(Unit) }
    }
}
