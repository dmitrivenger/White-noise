package com.whitenoise.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class AudioGeneratorTest {
    private AudioGenerator audioGenerator;

    @Before
    public void setUp() {
        audioGenerator = new AudioGenerator();
    }

    @After
    public void tearDown() {
        audioGenerator.release();
    }

    @Test
    public void initialState_notPlaying() {
        assertFalse(audioGenerator.isPlaying());
    }

    @Test
    public void start_beginsPlayback() {
        audioGenerator.start();
        assertTrue(audioGenerator.isPlaying());
    }

    @Test
    public void stop_endsPlayback() {
        audioGenerator.start();
        audioGenerator.stop();
        assertFalse(audioGenerator.isPlaying());
    }

    @Test
    public void doubleStart_isIdempotent() {
        audioGenerator.start();
        audioGenerator.start(); // should be a no-op
        assertTrue(audioGenerator.isPlaying());
    }

    @Test
    public void setVolume_minValue_doesNotThrow() {
        audioGenerator.start();
        audioGenerator.setVolume(0);
    }

    @Test
    public void setVolume_midValue_doesNotThrow() {
        audioGenerator.start();
        audioGenerator.setVolume(50);
    }

    @Test
    public void setVolume_maxValue_doesNotThrow() {
        audioGenerator.start();
        audioGenerator.setVolume(100);
    }

    @Test
    public void setVolume_beforeStart_doesNotThrow() {
        audioGenerator.setVolume(50);
    }

    @Test
    public void setPitch_lowValue_doesNotThrow() {
        audioGenerator.start();
        audioGenerator.setPitch(0);
    }

    @Test
    public void setPitch_midValue_doesNotThrow() {
        audioGenerator.start();
        audioGenerator.setPitch(50);
    }

    @Test
    public void setPitch_highValue_doesNotThrow() {
        audioGenerator.start();
        audioGenerator.setPitch(100);
    }

    @Test
    public void releaseAfterStop_doesNotThrow() {
        audioGenerator.start();
        audioGenerator.stop();
        audioGenerator.release();
    }

    @Test
    public void releaseWithoutStart_doesNotThrow() {
        audioGenerator.release();
    }

    @Test
    public void isPlaying_afterRelease_returnsFalse() {
        audioGenerator.start();
        audioGenerator.release();
        assertFalse(audioGenerator.isPlaying());
    }

    @Test
    public void startAfterStop_resumesPlayback() {
        audioGenerator.start();
        audioGenerator.stop();
        audioGenerator.start();
        assertTrue(audioGenerator.isPlaying());
    }
}
