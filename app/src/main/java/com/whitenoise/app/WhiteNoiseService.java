package com.whitenoise.app;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class WhiteNoiseService extends Service {
    private static final String TAG = "WhiteNoiseService";
    
    private AudioGenerator audioGenerator;
    private final IBinder binder = new LocalBinder();
    
    public class LocalBinder extends Binder {
        WhiteNoiseService getService() {
            return WhiteNoiseService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        audioGenerator = new AudioGenerator();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    public void startAudio() {
        if (audioGenerator != null) {
            audioGenerator.start();
            Log.d(TAG, "Audio started");
        }
    }
    
    public void stopAudio() {
        if (audioGenerator != null) {
            audioGenerator.stop();
            Log.d(TAG, "Audio stopped");
        }
    }
    
    public void setVolume(float volumePercent) {
        if (audioGenerator != null) {
            audioGenerator.setVolume(volumePercent);
        }
    }
    
    public void setPitch(float pitchPercent) {
        if (audioGenerator != null) {
            audioGenerator.setPitch(pitchPercent);
        }
    }
    
    public boolean isPlaying() {
        return audioGenerator != null && audioGenerator.isPlaying();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioGenerator != null) {
            audioGenerator.release();
            audioGenerator = null;
        }
        Log.d(TAG, "Service destroyed");
    }
}
