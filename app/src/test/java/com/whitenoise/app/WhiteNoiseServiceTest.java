package com.whitenoise.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class WhiteNoiseServiceTest {
    private WhiteNoiseService service;

    @Before
    public void setUp() {
        service = Robolectric.buildService(WhiteNoiseService.class)
                .create()
                .get();
    }

    @Test
    public void initialState_notPlaying() {
        assertFalse(service.isPlaying());
    }

    @Test
    public void startAudio_makesServicePlaying() {
        service.startAudio();
        assertTrue(service.isPlaying());
    }

    @Test
    public void stopAudio_afterStart_stopsPlayback() {
        service.startAudio();
        service.stopAudio();
        assertFalse(service.isPlaying());
    }

    @Test
    public void setVolume_whilePlaying_doesNotThrow() {
        service.startAudio();
        service.setVolume(0);
        service.setVolume(50);
        service.setVolume(100);
    }

    @Test
    public void setPitch_whilePlaying_doesNotThrow() {
        service.startAudio();
        service.setPitch(0);
        service.setPitch(50);
        service.setPitch(100);
    }

    @Test
    public void setVolume_beforeStart_doesNotThrow() {
        service.setVolume(50);
    }

    @Test
    public void setPitch_beforeStart_doesNotThrow() {
        service.setPitch(50);
    }

    @Test
    public void onBind_returnsNonNull() {
        assertNotNull(service.onBind(new Intent()));
    }

    @Test
    public void onDestroy_stopsAudio() {
        service.startAudio();
        service.onDestroy();
        assertFalse(service.isPlaying());
    }

    @Test
    public void isPlaying_reflectsStartStop() {
        assertFalse(service.isPlaying());
        service.startAudio();
        assertTrue(service.isPlaying());
        service.stopAudio();
        assertFalse(service.isPlaying());
    }

    @Test
    public void startAudio_twice_isIdempotent() {
        service.startAudio();
        service.startAudio();
        assertTrue(service.isPlaying());
    }
}
