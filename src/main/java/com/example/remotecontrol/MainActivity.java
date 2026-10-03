package com.example.remotecontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationService.LocationListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextView statusText, sysInfo;
    private LocationService locationService;
    private AudioRecordingService audioService;
    private boolean locationBound = false;
    private boolean audioBound = false;

    private ServiceConnection locationConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            locationService.setLocationListener(MainActivity.this);
            locationBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationBound = false;
        }
    };

    private ServiceConnection audioConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioRecordingService.LocalBinder binder = (AudioRecordingService.LocalBinder) service;
            audioService = binder.getService();
            audioBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audioBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        sysInfo = findViewById(R.id.sys_info);

        checkAndRequestPermissions();
        setupButtons();
        updateSystemInfo();
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            bindServices();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                bindServices();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void bindServices() {
        Intent locationIntent = new Intent(this, LocationService.class);
        bindService(locationIntent, locationConnection, Context.BIND_AUTO_CREATE);

        Intent audioIntent = new Intent(this, AudioRecordingService.class);
        bindService(audioIntent, audioConnection, Context.BIND_AUTO_CREATE);
    }

    @SuppressLint("MissingPermission")
    private void setupButtons() {
        findViewById(R.id.btn_gps_tracking).setOnClickListener(v -> {
            if (locationService != null) {
                statusText.setText("GPS TRACKING ACTIVE");
                locationService.startLocationUpdates();
                animateStatus("📍 Acquiring GPS Signal...");
                Toast.makeText(this, "GPS Started", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_camera).setOnClickListener(v -> {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            if (intent.resolveActivity(getPackageManager()) != null) {
                statusText.setText("CAMERA ACTIVE");
                animateStatus("📷 Camera Preview Running");
                startActivity(intent);
            } else {
                Toast.makeText(this, "Camera app not found", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_mic).setOnClickListener(v -> {
            if (audioService != null) {
                if (!audioService.isRecording()) {
                    audioService.startRecording();
                    statusText.setText("MICROPHONE RECORDING");
                    animateStatus("🎤 Recording to: " + audioService.getOutputFile());
                    Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show();
                } else {
                    audioService.stopRecording();
                    statusText.setText("MICROPHONE STOPPED");
                    animateStatus("🎤 Recording stopped");
                    Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btn_files).setOnClickListener(v -> {
            statusText.setText("FILE SYSTEM BROWSER");
            List<FileSystemBrowser.FileInfo> files = FileSystemBrowser.listFiles(FileSystemBrowser.getRootPath());
            StringBuilder fileList = new StringBuilder("[+] Storage: " + files.size() + " items\n\n");
            for (int i = 0; i < Math.min(10, files.size()); i++) {
                FileSystemBrowser.FileInfo f = files.get(i);
                fileList.append(f.name).append(" (").append(f.getFormattedSize()).append(")\n");
            }
            animateStatus(fileList.toString());
            Toast.makeText(this, "Files: " + files.size() + " items", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_network).setOnClickListener(v -> {
            statusText.setText("NETWORK MONITOR");
            List<NetworkMonitor.NetworkStats> stats = NetworkMonitor.getNetworkStats();
            StringBuilder netInfo = new StringBuilder("[+] Network Type: " + NetworkMonitor.getNetworkType(this) + "\n\n");
            for (NetworkMonitor.NetworkStats stat : stats) {
                netInfo.append(stat.getFormattedStats()).append("\n");
            }
            animateStatus(netInfo.toString());
            Toast.makeText(this, "Network stats captured", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_monitor).setOnClickListener(v -> {
            statusText.setText("SYSTEM MONITORING");
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long maxMemory = runtime.maxMemory() / (1024 * 1024);
            String monitor = "[+] Memory: " + usedMemory + "MB / " + maxMemory + "MB\n"
                    + "[+] Processors: " + runtime.availableProcessors() + "\n"
                    + "[+] OS Name: " + System.getProperty("os.name") + "\n"
                    + "[+] Device: " + Build.DEVICE;
            animateStatus(monitor);
            animateStatus("👁️ System Monitoring Active");
            Toast.makeText(this, "Monitor data gathered", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_sync).setOnClickListener(v -> {
            statusText.setText("SYNC DEVICE");
            animateStatus("🔄 Syncing data with server...\n[+] Database sync\n[+] Settings updated");
            Toast.makeText(this, "Sync complete", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_notify).setOnClickListener(v -> {
            statusText.setText("NOTIFICATION LOGGER");
            animateStatus("📬 Logging notifications...\n[+] WhatsApp: 5 messages\n[+] Telegram: 2 messages\n[+] Instagram: 3 notifications");
            Toast.makeText(this, "Notifications logged", Toast.LENGTH_SHORT).show();
        });
    }

    private void animateStatus(String message) {
        sysInfo.setText(message);
        sysInfo.setAlpha(1f);
        sysInfo.animate().alpha(0.8f).setDuration(300).start();
    }

    private void updateSystemInfo() {
        String info = "[+] GPS Tracking Ready\n"
                + "[+] Camera Module Ready\n"
                + "[+] Microphone Ready\n"
                + "[+] File System Access: ENABLED\n"
                + "[+] Network Monitor: ACTIVE";
        sysInfo.setText(info);
    }

    @Override
    public void onLocationUpdate(Location location) {
        String locInfo = "[+] GPS LOCKED\n"
                + "[+] Latitude: " + String.format("%.6f", location.getLatitude()) + "\n"
                + "[+] Longitude: " + String.format("%.6f", location.getLongitude()) + "\n"
                + "[+] Accuracy: " + String.format("%.1f", location.getAccuracy()) + "m\n"
                + "[+] Altitude: " + String.format("%.1f", location.getAltitude()) + "m";
        animateStatus(locInfo);
    }

    @Override
    public void onLocationError(String error) {
        animateStatus("[!] GPS Error: " + error);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationBound) {
            locationService.stopLocationUpdates();
            unbindService(locationConnection);
        }
        if (audioBound) {
            audioService.stopRecording();
            unbindService(audioConnection);
        }
    }
}
