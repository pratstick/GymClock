package com.example.gymclock

import com.example.gymclock.timer.WorkoutTimer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutTimerTest {

    private lateinit var testScope: TestScope
    private lateinit var timer: WorkoutTimer

    @Before
    fun setUp() {
        testScope = TestScope(StandardTestDispatcher())
        timer = WorkoutTimer(testScope)
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    // ── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `initial state is idle`() {
        val state = timer.state.value
        assertFalse(state.isRunning)
        assertFalse(state.isPaused)
        assertFalse(state.isFinished)
        assertEquals(0, state.currentSeconds)
        assertEquals(0, state.totalSeconds)
    }

    // ── startTimer ───────────────────────────────────────────────────────────

    @Test
    fun `startTimer sets isRunning and totalSeconds`() = testScope.runTest {
        timer.startTimer(30)
        val state = timer.state.value
        assertTrue(state.isRunning)
        assertFalse(state.isPaused)
        assertEquals(30, state.totalSeconds)
    }

    @Test
    fun `timer counts down over time`() = testScope.runTest {
        timer.startTimer(5)
        advanceTimeBy(3_000)
        val remaining = timer.state.value.currentSeconds
        assertTrue("Expected remaining ≤ 3, got $remaining", remaining <= 3)
    }

    @Test
    fun `timer finishes when it reaches zero`() = testScope.runTest {
        var finished = false
        timer.startTimer(3) { finished = true }
        advanceTimeBy(5_000)
        assertTrue("Timer should have fired onFinished callback", finished)
        val state = timer.state.value
        assertTrue(state.isFinished)
        assertFalse(state.isRunning)
        assertEquals(0, state.currentSeconds)
    }

    // ── pauseTimer / resumeTimer ─────────────────────────────────────────────

    @Test
    fun `pauseTimer stops countdown`() = testScope.runTest {
        timer.startTimer(10)
        advanceTimeBy(2_000)
        timer.pauseTimer()
        val secondsAtPause = timer.state.value.currentSeconds
        advanceTimeBy(3_000) // time passes but timer is paused
        assertEquals(
            "Seconds should not change while paused",
            secondsAtPause,
            timer.state.value.currentSeconds
        )
        assertTrue(timer.state.value.isPaused)
        assertFalse(timer.state.value.isRunning)
    }

    @Test
    fun `resumeTimer continues countdown from paused position`() = testScope.runTest {
        timer.startTimer(10)
        advanceTimeBy(3_000)
        timer.pauseTimer()
        val secondsAtPause = timer.state.value.currentSeconds
        timer.resumeTimer()
        advanceTimeBy(2_000)
        val secondsAfterResume = timer.state.value.currentSeconds
        assertTrue(
            "Timer should have decremented after resume; was $secondsAtPause, now $secondsAfterResume",
            secondsAfterResume < secondsAtPause
        )
        assertTrue(timer.state.value.isRunning)
        assertFalse(timer.state.value.isPaused)
    }

    @Test
    fun `pause then resume does not cause double-speed ticking`() = testScope.runTest {
        // This was the original bug: pauseTimer only set a flag without cancelling
        // the running coroutine, so resumeTimer launched a second coroutine while the
        // first one was still alive — both would decrement simultaneously.
        timer.startTimer(20)
        advanceTimeBy(1_000)
        timer.pauseTimer()
        timer.resumeTimer()
        advanceTimeBy(1_000)
        val remaining = timer.state.value.currentSeconds
        // With the correct fix, 2 seconds have elapsed in total so remaining ≈ 18
        // If the bug were present, remaining could be as low as 16.
        assertTrue(
            "Expected remaining around 18 but got $remaining — possible double-tick",
            remaining >= 17
        )
    }

    // ── resetTimer ───────────────────────────────────────────────────────────

    @Test
    fun `resetTimer returns to idle state`() = testScope.runTest {
        timer.startTimer(10)
        advanceTimeBy(3_000)
        timer.resetTimer()
        val state = timer.state.value
        assertFalse(state.isRunning)
        assertFalse(state.isPaused)
        assertFalse(state.isFinished)
        assertEquals(0, state.currentSeconds)
        assertEquals(0, state.totalSeconds)
    }

    // ── stopTimer ────────────────────────────────────────────────────────────

    @Test
    fun `stopTimer halts without finishing`() = testScope.runTest {
        timer.startTimer(10)
        advanceTimeBy(2_000)
        timer.stopTimer()
        val state = timer.state.value
        assertFalse(state.isRunning)
        assertFalse(state.isPaused)
        assertFalse(state.isFinished)
    }

    // ── addTime ──────────────────────────────────────────────────────────────

    @Test
    fun `addTime increases currentSeconds and totalSeconds`() = testScope.runTest {
        timer.startTimer(10)
        val beforeTotal = timer.state.value.totalSeconds
        timer.addTime(15)
        assertEquals(beforeTotal + 15, timer.state.value.totalSeconds)
        // currentSeconds also increases (starting position was 10, now 25)
        assertTrue(timer.state.value.currentSeconds > 10)
    }

    // ── formatTime ───────────────────────────────────────────────────────────

    @Test
    fun `formatTime formats seconds correctly`() {
        assertEquals("00:00", timer.formatTime(0))
        assertEquals("01:00", timer.formatTime(60))
        assertEquals("01:30", timer.formatTime(90))
        assertEquals("10:05", timer.formatTime(605))
        assertEquals("00:45", timer.formatTime(45))
    }
}
