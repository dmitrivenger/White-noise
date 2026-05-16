package com.whitenoise.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class TimerManagerTest {
    private TimerManager timerManager;
    private List<Integer> ticks;
    private int finishCount;

    @Before
    public void setUp() {
        ticks = new ArrayList<>();
        finishCount = 0;
        timerManager = new TimerManager(
                ApplicationProvider.getApplicationContext(),
                new TimerManager.TimerCallback() {
                    @Override public void onTimerTick(int remaining) { ticks.add(remaining); }
                    @Override public void onTimerFinish() { finishCount++; }
                }
        );
    }

    @After
    public void tearDown() {
        timerManager.destroy();
    }

    @Test
    public void initialState_notRunning_zeroSeconds() {
        assertFalse(timerManager.isRunning());
        assertEquals(0, timerManager.getRemainingSeconds());
    }

    @Test
    public void setTimer_convertsMinutesToSeconds() {
        timerManager.setTimer(3);
        assertEquals(180, timerManager.getRemainingSeconds());
    }

    @Test
    public void startWithNoTime_doesNotRun() {
        timerManager.start();
        assertFalse(timerManager.isRunning());
    }

    @Test
    public void start_setsRunningState() {
        timerManager.setTimer(5);
        timerManager.start();
        assertTrue(timerManager.isRunning());
    }

    @Test
    public void stop_haltsCountdown() {
        timerManager.setTimer(5);
        timerManager.start();
        timerManager.stop();
        assertFalse(timerManager.isRunning());
    }

    @Test
    public void cancel_resetsToZeroAndStops() {
        timerManager.setTimer(5);
        timerManager.start();
        timerManager.cancel();
        assertFalse(timerManager.isRunning());
        assertEquals(0, timerManager.getRemainingSeconds());
    }

    @Test
    public void cancel_firesTickCallbackWithZero() {
        timerManager.setTimer(5);
        timerManager.cancel();
        assertFalse(ticks.isEmpty());
        assertEquals(0, (int) ticks.get(ticks.size() - 1));
    }

    @Test
    public void timerTick_callbackFiredEachSecond() {
        timerManager.setTimer(1);
        timerManager.start();
        ShadowLooper.idleFor(Duration.ofSeconds(3));
        assertTrue("Expected at least 3 ticks, got " + ticks.size(), ticks.size() >= 3);
    }

    @Test
    public void timerTick_valuesDecreaseByOne() {
        timerManager.setTimer(1);
        timerManager.start();
        ShadowLooper.idleFor(Duration.ofSeconds(5));
        for (int i = 0; i < ticks.size() - 1; i++) {
            assertEquals(ticks.get(i) - 1, (int) ticks.get(i + 1));
        }
    }

    @Test
    public void timerFinish_calledWhenTimeRunsOut() {
        timerManager.setTimer(1);
        timerManager.start();
        ShadowLooper.idleFor(Duration.ofSeconds(61));
        assertEquals(1, finishCount);
        assertFalse(timerManager.isRunning());
    }

    @Test
    public void timerFinish_calledExactlyOnce() {
        timerManager.setTimer(1);
        timerManager.start();
        ShadowLooper.idleFor(Duration.ofSeconds(120));
        assertEquals(1, finishCount);
    }

    @Test
    public void setTimerWhileRunning_resetsAndStops() {
        timerManager.setTimer(5);
        timerManager.start();
        timerManager.setTimer(2);
        assertEquals(120, timerManager.getRemainingSeconds());
        assertFalse(timerManager.isRunning());
    }

    @Test
    public void stopAfterFinish_doesNotThrow() {
        timerManager.setTimer(1);
        timerManager.start();
        ShadowLooper.idleFor(Duration.ofSeconds(61));
        timerManager.stop(); // should be a no-op, not throw
    }
}
