package com.example.remotecontrol;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class AudioRecordingService extends Service {

    private MediaRecorder mediaRecorder;
    private String outputFile;
    private static final String TAG = "AudioRecordingService";
    private final IBinder binder = new LocalBinder();
    private boolean isRecording = false;

    public class LocalBinder extends Binder {
        AudioRecordingService getService() {
            return AudioRecordingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupOutputFile();
    }

    private void setupOutputFile() {
        File recordDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "RemoteControl");
        if (!recordDir.exists()) {
            recordDir.mkdirs();
        }
        outputFile = recordDir.getAbsolutePath() + "/recording_" + System.currentTimeMillis() + ".3gp";
    }

    public void startRecording() {
        if (isRecording) {
            Log.w(TAG, "Recording already in progress");
            return;
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(outputFile);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioEncodingBitRate(128000);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Recording started: " + outputFile);
        } catch (IOException e) {
            Log.e(TAG, "Recording preparation failed", e);
            stopRecording();
        }
    }

    public void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                isRecording = false;
                Log.d(TAG, "Recording stopped");
            } catch (RuntimeException e) {
                Log.e(TAG, "Stop recording error", e);
            }
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public String getOutputFile() {
        return outputFile;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        super.onDestroy();
    }
}
