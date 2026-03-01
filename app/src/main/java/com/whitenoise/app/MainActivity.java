package com.whitenoise.app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements TimerManager.TimerCallback {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    private WhiteNoiseService whiteNoiseService;
    private TimerManager timerManager;
    private boolean serviceBound = false;
    private boolean isPlaying = false;
    
    // UI Components
    private SeekBar volumeSlider;
    private SeekBar pitchSlider;
    private SeekBar timerSlider;
    private Button playButton;
    private Button stopButton;
    private TextView volumeValue;
    private TextView pitchValue;
    private TextView timerValue;
    private TextView timerStatus;
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WhiteNoiseService.LocalBinder binder = (WhiteNoiseService.LocalBinder) service;
            whiteNoiseService = binder.getService();
            serviceBound = true;
            Log.d(TAG, "Service connected");
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            Log.d(TAG, "Service disconnected");
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Request permissions
        requestAudioPermissions();
        
        // Initialize UI components
        initializeUI();
        
        // Initialize timer manager
        timerManager = new TimerManager(this, this);
        
        // Bind to service
        bindService();
    }
    
    private void requestAudioPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    private void initializeUI() {
        // Find views
        volumeSlider = findViewById(R.id.volumeSlider);
        pitchSlider = findViewById(R.id.pitchSlider);
        timerSlider = findViewById(R.id.timerSlider);
        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);
        volumeValue = findViewById(R.id.volumeValue);
        pitchValue = findViewById(R.id.pitchValue);
        timerValue = findViewById(R.id.timerValue);
        timerStatus = findViewById(R.id.timerStatus);
        
        // Volume slider listener
        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeValue.setText(progress + "%");
                if (serviceBound) {
                    whiteNoiseService.setVolume(progress);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Pitch slider listener
        pitchSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String pitchLabel;
                if (progress < 33) {
                    pitchLabel = "Low";
                } else if (progress < 66) {
                    pitchLabel = "Medium";
                } else {
                    pitchLabel = "High";
                }
                pitchValue.setText(pitchLabel);
                if (serviceBound) {
                    whiteNoiseService.setPitch(progress);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Timer slider listener
        timerSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    timerValue.setText("Off");
                    timerStatus.setText(getString(R.string.timer_off));
                    timerManager.cancel();
                } else {
                    timerValue.setText(progress + " min");
                    timerManager.setTimer(progress);
                    if (isPlaying) {
                        timerManager.start();
                    }
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Play button listener
        playButton.setOnClickListener(v -> {
            if (serviceBound) {
                whiteNoiseService.startAudio();
                isPlaying = true;
                updateUIState();
                
                // Start timer if set
                if (timerSlider.getProgress() > 0) {
                    timerManager.start();
                }
                Log.d(TAG, "Play clicked");
            }
        });
        
        // Stop button listener
        stopButton.setOnClickListener(v -> {
            if (serviceBound) {
                whiteNoiseService.stopAudio();
                isPlaying = false;
                timerManager.stop();
                updateUIState();
                Log.d(TAG, "Stop clicked");
            }
        });
    }
    
    private void updateUIState() {
        playButton.setEnabled(!isPlaying);
        stopButton.setEnabled(isPlaying);
    }
    
    private void bindService() {
        Intent intent = new Intent(this, WhiteNoiseService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    public void onTimerTick(int remainingSeconds) {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String timeString = String.format("Timer: %d:%02d", minutes, seconds);
        timerStatus.setText(timeString);
    }
    
    @Override
    public void onTimerFinish() {
        timerStatus.setText(getString(R.string.timer_off));
        if (serviceBound) {
            whiteNoiseService.stopAudio();
            isPlaying = false;
            updateUIState();
        }
        Log.d(TAG, "Timer finished");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        
        if (timerManager != null) {
            timerManager.destroy();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Keep audio playing in background
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (serviceBound && whiteNoiseService.isPlaying()) {
            isPlaying = true;
            updateUIState();
        }
    }
}
