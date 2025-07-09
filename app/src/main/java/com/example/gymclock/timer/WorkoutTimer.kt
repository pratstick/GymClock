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
            var remainingSeconds = seconds
            while (remainingSeconds > 0 && _state.value.isRunning && !_state.value.isPaused) {
                delay(1000)
                remainingSeconds--
                _state.value = _state.value.copy(currentSeconds = remainingSeconds)
            }

            if (remainingSeconds == 0) {
                _state.value = _state.value.copy(
                    isRunning = false,
                    isFinished = true
                )
                onTimerFinished?.invoke()
            }
        }
    }

    fun pauseTimer() {
        _state.value = _state.value.copy(isPaused = true)
    }

    fun resumeTimer() {
        if (_state.value.isPaused) {
            _state.value = _state.value.copy(isPaused = false)

            timerJob = scope.launch {
                var remainingSeconds = _state.value.currentSeconds
                while (remainingSeconds > 0 && _state.value.isRunning && !_state.value.isPaused) {
                    delay(1000)
                    remainingSeconds--
                    _state.value = _state.value.copy(currentSeconds = remainingSeconds)
                }

                if (remainingSeconds == 0) {
                    _state.value = _state.value.copy(
                        isRunning = false,
                        isFinished = true
                    )
                    onTimerFinished?.invoke()
                }
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _state.value = TimerState()
    }

    fun stopTimer() {
        timerJob?.cancel()
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
}
