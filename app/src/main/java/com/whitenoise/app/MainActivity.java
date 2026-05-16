package com.whitenoise.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TimerManager.TimerCallback {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Volume slider shows 0-100% but maps to 0-10% of actual max volume for sleep use
    private static final float VOLUME_SCALE = 0.1f;

    private WhiteNoiseService whiteNoiseService;
    private TimerManager timerManager;
    private boolean serviceBound = false;
    private boolean isPlaying = false;

    // UI Components
    private LinearLayout rootLayout;
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
            // Sync slider position to actual (scaled) volume on connect
            whiteNoiseService.setVolume(volumeSlider.getProgress() * VOLUME_SCALE);
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
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestAudioPermissions();
        initializeUI();
        applyDailyBackground();
        showCurrentDate();

        timerManager = new TimerManager(this, this);
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

    private void applyDailyBackground() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int[] colors = getDayGradientColors(dayOfWeek);
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        rootLayout.setBackground(gradient);
    }

    private int[] getDayGradientColors(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:    return new int[]{0xFF1F0A15, 0xFF0D1420}; // warm burgundy night
            case Calendar.MONDAY:    return new int[]{0xFF0A1020, 0xFF162032}; // cool deep navy
            case Calendar.TUESDAY:   return new int[]{0xFF051820, 0xFF0D2030}; // ocean teal night
            case Calendar.WEDNESDAY: return new int[]{0xFF12081E, 0xFF1A0D2A}; // deep violet
            case Calendar.THURSDAY:  return new int[]{0xFF081408, 0xFF0D1B1A}; // forest night
            case Calendar.FRIDAY:    return new int[]{0xFF06040E, 0xFF0F0D1E}; // cosmic indigo
            case Calendar.SATURDAY:  return new int[]{0xFF1A0E05, 0xFF1E1508}; // amber twilight
            default:                 return new int[]{0xFF0D1B2A, 0xFF162032};
        }
    }

    private void showCurrentDate() {
        TextView dateDisplay = findViewById(R.id.dateDisplay);
        TextView dayDisplay = findViewById(R.id.dayDisplay);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dayFmt = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat dateFmt = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        Date now = cal.getTime();

        dateDisplay.setText(dayFmt.format(now));
        dayDisplay.setText(dateFmt.format(now));
    }

    private void initializeUI() {
        rootLayout = findViewById(R.id.rootLayout);
        volumeSlider = findViewById(R.id.volumeSlider);
        pitchSlider = findViewById(R.id.pitchSlider);
        timerSlider = findViewById(R.id.timerSlider);
        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);
        volumeValue = findViewById(R.id.volumeValue);
        pitchValue = findViewById(R.id.pitchValue);
        timerValue = findViewById(R.id.timerValue);
        timerStatus = findViewById(R.id.timerStatus);

        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeValue.setText(progress + "%");
                if (serviceBound) {
                    // Map slider 0-100% to actual volume 0-10% for ultra-fine sleep control
                    whiteNoiseService.setVolume(progress * VOLUME_SCALE);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

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

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        timerSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timerValue.setText(progress == 0 ? "Off" : progress + " min");
                if (progress == 0) {
                    timerStatus.setText(getString(R.string.timer_off));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0) {
                    timerStatus.setText(getString(R.string.timer_off));
                    timerManager.cancel();
                } else {
                    timerManager.setTimer(progress);
                    if (isPlaying) {
                        timerManager.start();
                    }
                }
            }
        });

        playButton.setOnClickListener(v -> {
            if (serviceBound) {
                whiteNoiseService.startAudio();
                isPlaying = true;
                updateUIState();
                if (timerSlider.getProgress() > 0) {
                    timerManager.start();
                }
                Log.d(TAG, "Play clicked");
            }
        });

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
        timerStatus.setText(String.format("Timer: %d:%02d", minutes, seconds));
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
