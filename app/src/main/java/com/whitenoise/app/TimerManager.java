package com.whitenoise.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.core.content.ContextCompat;

public class TimerManager {
    private Context context;
    private Handler handler;
    private Runnable timerRunnable;
    private TimerCallback callback;
    
    private int totalSeconds = 0;
    private int remainingSeconds = 0;
    private boolean isRunning = false;
    
    public interface TimerCallback {
        void onTimerTick(int remainingSeconds);
        void onTimerFinish();
    }
    
    public TimerManager(Context context, TimerCallback callback) {
        this.context = context;
        this.callback = callback;
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    public void setTimer(int minutes) {
        if (isRunning) {
            stop();
        }
        this.totalSeconds = minutes * 60;
        this.remainingSeconds = totalSeconds;
        updateCallbacks();
    }
    
    public void start() {
        if (remainingSeconds <= 0) {
            return;
        }
        
        isRunning = true;
        startTimerTick();
    }
    
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
    }
    
    public void cancel() {
        stop();
        totalSeconds = 0;
        remainingSeconds = 0;
        updateCallbacks();
    }
    
    private void startTimerTick() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning && remainingSeconds > 0) {
                    remainingSeconds--;
                    updateCallbacks();
                    
                    if (remainingSeconds <= 0) {
                        isRunning = false;
                        if (callback != null) {
                            callback.onTimerFinish();
                        }
                    } else {
                        handler.postDelayed(this, 1000);
                    }
                }
            }
        };
        handler.post(timerRunnable);
    }
    
    private void updateCallbacks() {
        if (callback != null) {
            callback.onTimerTick(remainingSeconds);
        }
    }
    
    public int getRemainingSeconds() {
        return remainingSeconds;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public void destroy() {
        stop();
        handler.removeCallbacksAndMessages(null);
    }
}
