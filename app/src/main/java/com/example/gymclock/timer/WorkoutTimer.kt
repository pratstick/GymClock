package com.example.gymclock.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class TimerState(
    val currentSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false
)

class WorkoutTimer(private val scope: CoroutineScope) {
    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var onTimerFinished: (() -> Unit)? = null

    fun startTimer(seconds: Int, onFinished: (() -> Unit)? = null) {
        timerJob?.cancel()
        onTimerFinished = onFinished

        _state.value = TimerState(
            currentSeconds = seconds,
            totalSeconds = seconds,
            isRunning = true,
            isPaused = false,
            isFinished = false
        )

        timerJob = scope.launch {
            runCountdown(seconds)
        }
    }

    fun pauseTimer() {
        // Cancel the active coroutine so it cannot resume on its own after the
        // next delay completes — previously only setting isPaused=true left the
        // job alive, causing a double-decrement once resumeTimer launched a
        // second coroutine.
        timerJob?.cancel()
        timerJob = null
        _state.value = _state.value.copy(isRunning = false, isPaused = true)
    }

    fun resumeTimer() {
        if (_state.value.isPaused) {
            val remaining = _state.value.currentSeconds
            _state.value = _state.value.copy(isRunning = true, isPaused = false)
            timerJob = scope.launch {
                runCountdown(remaining)
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.value = TimerState()
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.value = _state.value.copy(isRunning = false, isPaused = false)
    }

    fun addTime(seconds: Int) {
        val newSeconds = _state.value.currentSeconds + seconds
        _state.value = _state.value.copy(
            currentSeconds = newSeconds,
            totalSeconds = _state.value.totalSeconds + seconds
        )
    }

    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    /**
     * Runs a deadline-based countdown for [durationSeconds].
     * Using an absolute deadline avoids accumulated drift from coroutine
     * scheduling overhead that a plain delay(1000) loop would introduce.
     */
    private suspend fun runCountdown(durationSeconds: Int) {
        val endTime = System.currentTimeMillis() + durationSeconds * 1000L
        while (true) {
            val remaining = ((endTime - System.currentTimeMillis()) / 1000L).toInt()
            if (remaining <= 0) break
            _state.value = _state.value.copy(currentSeconds = remaining)
            // Sleep until the next whole-second boundary.
            val sleepMs = (endTime - System.currentTimeMillis()) % 1000L
            delay(if (sleepMs > 0) sleepMs else 100L)
        }
        _state.value = _state.value.copy(
            currentSeconds = 0,
            isRunning = false,
            isFinished = true
        )
        onTimerFinished?.invoke()
    }
}
