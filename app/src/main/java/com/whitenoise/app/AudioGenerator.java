package com.whitenoise.app;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import java.util.Random;

public class AudioGenerator {
    private static final String TAG = "AudioGenerator";
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 4096;

    private AudioTrack audioTrack;
    private Thread generationThread;
    private volatile boolean isRunning = false;
    private volatile float volume = 0.5f;
    private volatile float pitch = 0.5f;

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
        if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            initializeAudioTrack();
        }
        audioTrack.play();
        audioTrack.setVolume(volume);
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
        this.volume = volumePercent / 100.0f;
        if (audioTrack != null) {
            audioTrack.setVolume(this.volume);
        }
    }

    public void setPitch(float pitchPercent) {
        this.pitch = pitchPercent / 100.0f;
    }

    private void generateAudio() {
        short[] buffer = new short[BUFFER_SIZE];
        Random random = new Random();
        float prevSample = 0f;

        while (isRunning) {
            try {
                // IIR coefficient: pitch=0 → heavy bass filter, pitch=1 → flat white noise
                float alpha = 0.05f + 0.95f * pitch;

                for (int i = 0; i < BUFFER_SIZE; i++) {
                    float sample = random.nextFloat() * 2.0f - 1.0f;
                    // First-order IIR low-pass: y[n] = alpha*x[n] + (1-alpha)*y[n-1]
                    prevSample = alpha * sample + (1f - alpha) * prevSample;
                    buffer[i] = (short) (prevSample * Short.MAX_VALUE);
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

    public boolean isPlaying() {
        return isRunning && audioTrack != null
                && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
    }
}
