package com.example.foldisland.managers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object TimerCenter {
    private val scope = CoroutineScope(Dispatchers.Default)

    // Stopwatch state
    private val _stopwatchSeconds = MutableStateFlow(0L)
    val stopwatchSeconds: StateFlow<Long> = _stopwatchSeconds.asStateFlow()

    private val _isStopwatchRunning = MutableStateFlow(false)
    val isStopwatchRunning: StateFlow<Boolean> = _isStopwatchRunning.asStateFlow()

    private var stopwatchJob: Job? = null

    // Countdown Timer state
    private val _countdownRemainingSeconds = MutableStateFlow(0L)
    val countdownRemainingSeconds: StateFlow<Long> = _countdownRemainingSeconds.asStateFlow()

    private val _isCountdownRunning = MutableStateFlow(false)
    val isCountdownRunning: StateFlow<Boolean> = _isCountdownRunning.asStateFlow()

    private var countdownJob: Job? = null

    // Water tracker & Tally counter
    private val _waterGlasses = MutableStateFlow(4)
    val waterGlasses: StateFlow<Int> = _waterGlasses.asStateFlow()

    private val _tallyCount = MutableStateFlow(0)
    val tallyCount: StateFlow<Int> = _tallyCount.asStateFlow()

    fun toggleStopwatch() {
        if (_isStopwatchRunning.value) {
            stopwatchJob?.cancel()
            _isStopwatchRunning.value = false
        } else {
            _isStopwatchRunning.value = true
            stopwatchJob = scope.launch {
                while (isActive) {
                    delay(1000)
                    _stopwatchSeconds.value += 1
                }
            }
        }
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        _isStopwatchRunning.value = false
        _stopwatchSeconds.value = 0L
    }

    fun startCountdown(totalSeconds: Long) {
        countdownJob?.cancel()
        _countdownRemainingSeconds.value = totalSeconds
        _isCountdownRunning.value = true

        countdownJob = scope.launch {
            while (isActive && _countdownRemainingSeconds.value > 0) {
                delay(1000)
                _countdownRemainingSeconds.value -= 1
            }
            _isCountdownRunning.value = false
            if (_countdownRemainingSeconds.value <= 0) {
                HUDManager.showPinnedNotice("⏰", "Süre doldu!")
            }
        }
    }

    fun stopCountdown() {
        countdownJob?.cancel()
        _isCountdownRunning.value = false
    }

    fun incrementWater() {
        _waterGlasses.value = (_waterGlasses.value + 1).coerceAtMost(16)
    }

    fun decrementWater() {
        _waterGlasses.value = (_waterGlasses.value - 1).coerceAtLeast(0)
    }

    fun incrementTally() {
        _tallyCount.value += 1
    }

    fun resetTally() {
        _tallyCount.value = 0
    }

    fun formatSecondsToTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
