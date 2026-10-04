package com.example.remotecontrol;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView statusText;
    private TextView gpsText, micText, fileText, netText, sysText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        gpsText = findViewById(R.id.gpsText);
        micText = findViewById(R.id.micText);
        fileText = findViewById(R.id.fileText);
        netText = findViewById(R.id.netText);
        sysText = findViewById(R.id.sysText);

        Button gpsBtn = findViewById(R.id.gpsBtn);
        Button micBtn = findViewById(R.id.micBtn);
        Button fileBtn = findViewById(R.id.fileBtn);
        Button netBtn = findViewById(R.id.netBtn);
        Button sysBtn = findViewById(R.id.sysBtn);
        Button camBtn = findViewById(R.id.camBtn);
        Button syncBtn = findViewById(R.id.syncBtn);
        Button notifBtn = findViewById(R.id.notifBtn);

        gpsBtn.setOnClickListener(v -> {
            statusText.setText("Status: GPS ACTIVE");
            gpsText.setText("Lat: 37.7749 | Lon: -122.4194 | Alt: 52m");
            Toast.makeText(this, "GPS Tracking Started", Toast.LENGTH_SHORT).show();
            startService(new Intent(this, LocationService.class));
        });

        micBtn.setOnClickListener(v -> {
            statusText.setText("Status: RECORDING");
            micText.setText("Recording: audio_001.3gp | Size: 512 KB");
            Toast.makeText(this, "Microphone Activated", Toast.LENGTH_SHORT).show();
            startService(new Intent(this, AudioRecordingService.class));
        });

        fileBtn.setOnClickListener(v -> {
            statusText.setText("Status: FILE BROWSER OPEN");
            fileText.setText("Files: 247 | Total: 4.2 GB | Last: photo.jpg (2.5MB)");
            Toast.makeText(this, "File System Browsed", Toast.LENGTH_SHORT).show();
        });

        netBtn.setOnClickListener(v -> {
            statusText.setText("Status: NETWORK MONITOR");
            netText.setText("RX: 125.4 MB | TX: 87.2 MB | Type: WiFi");
            Toast.makeText(this, "Network Monitored", Toast.LENGTH_SHORT).show();
        });

        sysBtn.setOnClickListener(v -> {
            statusText.setText("Status: SYSTEM INFO");
            sysText.setText("RAM: 4.2/8 GB | CPU: 8 cores | Android 13");
            Toast.makeText(this, "System Data Retrieved", Toast.LENGTH_SHORT).show();
        });

        camBtn.setOnClickListener(v -> {
            statusText.setText("Status: CAMERA ACTIVE");
            Toast.makeText(this, "Camera Activated", Toast.LENGTH_SHORT).show();
            Intent camera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            if (camera.resolveActivity(getPackageManager()) != null) {
                startActivity(camera);
            }
        });

        syncBtn.setOnClickListener(v -> {
            statusText.setText("Status: SYNCING DEVICES");
            Toast.makeText(this, "Devices Syncing", Toast.LENGTH_SHORT).show();
        });

        notifBtn.setOnClickListener(v -> {
            statusText.setText("Status: NOTIFICATION LOG");
            Toast.makeText(this, "Notifications Logged", Toast.LENGTH_SHORT).show();
        });
    }
}
