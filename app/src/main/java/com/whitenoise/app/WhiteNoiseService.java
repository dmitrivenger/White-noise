package com.whitenoise.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class WhiteNoiseService extends Service {
    private static final String TAG = "WhiteNoiseService";
    private static final String CHANNEL_ID = "white_noise_playback";
    private static final int NOTIFICATION_ID = 1;

    private AudioGenerator audioGenerator;
    private PowerManager.WakeLock wakeLock;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private AudioManager.OnAudioFocusChangeListener focusChangeListener;
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        WhiteNoiseService getService() {
            return WhiteNoiseService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioGenerator = new AudioGenerator();
        createNotificationChannel();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WhiteNoise::AudioWakeLock");
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // For a sleep app we keep playing through transient focus losses (phone call will still preempt via system)
        focusChangeListener = focusChange -> Log.d(TAG, "Audio focus change: " + focusChange);
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void startAudio() {
        if (audioGenerator != null) {
            requestAudioFocus();
            audioGenerator.start();
            startForeground(NOTIFICATION_ID, buildNotification());
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
            Log.d(TAG, "Audio started");
        }
    }

    public void stopAudio() {
        if (audioGenerator != null) {
            audioGenerator.stop();
            releaseAudioFocus();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE);
            } else {
                stopForeground(true);
            }
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
            Log.d(TAG, "Audio stopped");
        }
    }

    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .setWillPauseWhenDucked(false)
                    .build();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    private void releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(focusChangeListener);
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
    public void onTaskRemoved(Intent rootIntent) {
        // Re-start ourselves so audio survives the user swiping the app out of recents
        startService(new Intent(this, WhiteNoiseService.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioGenerator != null) {
            audioGenerator.release();
            audioGenerator = null;
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        Log.d(TAG, "Service destroyed");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class), flags
        );
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }
}
