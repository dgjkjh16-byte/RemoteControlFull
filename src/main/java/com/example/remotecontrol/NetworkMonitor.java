package com.example.remotecontrol;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NetworkMonitor {

    public static class NetworkStats {
        public String interfaceName;
        public long receivedBytes;
        public long transmittedBytes;
        public String protocolType;

        public NetworkStats(String interfaceName, long receivedBytes, long transmittedBytes, String protocolType) {
            this.interfaceName = interfaceName;
            this.receivedBytes = receivedBytes;
            this.transmittedBytes = transmittedBytes;
            this.protocolType = protocolType;
        }

        public String getFormattedStats() {
            return interfaceName + ": " + formatBytes(receivedBytes) + " RX / " + formatBytes(transmittedBytes) + " TX";
        }

        private static String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + "B";
            if (bytes < 1024 * 1024) return (bytes / 1024) + "KB";
            if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)) + "MB";
            return (bytes / (1024 * 1024 * 1024)) + "GB";
        }
    }

    public static List<NetworkStats> getNetworkStats() {
        List<NetworkStats> statsList = new ArrayList<>();

        try {
            File procFile = new File("/proc/net/dev");
            if (!procFile.exists()) {
                return statsList;
            }

            BufferedReader reader = new BufferedReader(new FileReader(procFile));
            String line;
            boolean skipHeaders = true;

            while ((line = reader.readLine()) != null) {
                if (skipHeaders && line.contains("Iface")) {
                    skipHeaders = false;
                    continue;
                }

                if (skipHeaders || line.trim().isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 10) {
                    String interfaceName = parts[0].replace(":", "");
                    long receivedBytes = Long.parseLong(parts[1]);
                    long transmittedBytes = Long.parseLong(parts[9]);

                    if (!interfaceName.equals("lo")) {
                        statsList.add(new NetworkStats(interfaceName, receivedBytes, transmittedBytes, "IPv4"));
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return statsList;
    }

    public static String getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork == null || !activeNetwork.isConnected()) {
            return "No Connection";
        }

        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
            return "WiFi";
        } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = tm.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return "GPRS";
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return "EDGE";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return "UMTS";
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return "CDMA";
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return "EVDO_0";
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return "HSDPA";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "LTE";
                default:
                    return "Mobile";
            }
        }
        return "Unknown";
    }
}
