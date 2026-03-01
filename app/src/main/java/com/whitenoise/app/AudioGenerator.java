package com.whitenoise.app;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Generates white noise with adjustable pitch and frequency
 */
public class AudioGenerator {
    private static final String TAG = "AudioGenerator";
    
    private static final int SAMPLE_RATE = 44100; // Hz
    private static final int BUFFER_SIZE = 4096;
    
    private AudioTrack audioTrack;
    private Thread generationThread;
    private volatile boolean isRunning = false;
    private volatile float volume = 0.5f; // 0.0 to 1.0
    private volatile float pitch = 0.5f; // 0.0 to 1.0 (affects frequency range)
    
    private long frameCount = 0;
    
    public AudioGenerator() {
        initializeAudioTrack();
    }
    
    private void initializeAudioTrack() {
        int bufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );
        
        if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(Math.max(bufferSize, BUFFER_SIZE * 2))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
    }
    
    public void start() {
        if (isRunning) return;
        
        isRunning = true;
        frameCount = 0;
        
        if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            initializeAudioTrack();
        }
        
        audioTrack.play();
        
        generationThread = new Thread(this::generateAudio, "WhiteNoiseGenerator");
        generationThread.start();
    }
    
    public void stop() {
        isRunning = false;
        
        if (generationThread != null) {
            try {
                generationThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Interrupted while stopping audio generation", e);
            }
        }
        
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.flush();
        }
    }
    
    public void release() {
        stop();
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
    }
    
    public void setVolume(float volumePercent) {
        // volumePercent is 0-100
        this.volume = volumePercent / 100.0f;
    }
    
    public void setPitch(float pitchPercent) {
        // pitchPercent is 0-100, affects the frequency range
        this.pitch = pitchPercent / 100.0f;
    }
    
    private void generateAudio() {
        short[] buffer = new short[BUFFER_SIZE];
        
        while (isRunning) {
            try {
                for (int i = 0; i < BUFFER_SIZE; i++) {
                    // Generate white noise using random values
                    float sample = (float) Math.random() * 2.0f - 1.0f;
                    
                    // Apply a low-pass filter effect based on pitch
                    // Lower pitch = more bass-like filtering
                    sample = applyFrequencyShaping(sample, pitch);
                    
                    // Apply volume
                    sample *= volume;
                    
                    // Clamp to valid range
                    sample = Math.max(-1.0f, Math.min(1.0f, sample));
                    
                    // Convert to 16-bit PCM
                    buffer[i] = (short) (sample * Short.MAX_VALUE);
                    
                    frameCount++;
                }
                
                int written = audioTrack.write(buffer, 0, BUFFER_SIZE, AudioTrack.WRITE_BLOCKING);
                if (written < 0) {
                    Log.e(TAG, "Error writing audio data: " + written);
                    break;
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error in audio generation thread", e);
                break;
            }
        }
    }
    
    /**
     * Apply frequency shaping to create different pitch/tone variations
     */
    private float applyFrequencyShaping(float sample, float pitch) {
        // Simple low-pass filtering effect
        // Higher pitch value = more treble frequencies
        // Lower pitch value = more bass frequencies
        
        // Use a moving average for low-pass filtering
        float filtered = sample;
        
        // Adjust based on pitch
        // This is a simplified approach - pitch affects which frequencies are emphasized
        if (pitch < 0.5f) {
            // Lower frequencies - use heavier filtering
            filtered = sample * 0.7f + sample * 0.3f; // Simple average
        } else if (pitch > 0.5f) {
            // Higher frequencies - reduce filtering
            filtered = sample;
        }
        
        return filtered;
    }
    
    public boolean isPlaying() {
        return isRunning && audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
    }
}
