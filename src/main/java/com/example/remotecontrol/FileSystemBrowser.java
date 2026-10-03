package com.example.remotecontrol;

import android.os.Environment;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileSystemBrowser {

    public static class FileInfo {
        public String name;
        public String path;
        public long size;
        public long modifiedTime;
        public boolean isDirectory;

        public FileInfo(String name, String path, long size, long modifiedTime, boolean isDirectory) {
            this.name = name;
            this.path = path;
            this.size = size;
            this.modifiedTime = modifiedTime;
            this.isDirectory = isDirectory;
        }

        public String getFormattedSize() {
            if (isDirectory) return "[DIR]";
            if (size < 1024) return size + "B";
            if (size < 1024 * 1024) return (size / 1024) + "KB";
            return (size / (1024 * 1024)) + "MB";
        }

        public String getFormattedTime() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(modifiedTime));
        }
    }

    public static List<FileInfo> listFiles(String path) {
        List<FileInfo> fileList = new ArrayList<>();
        File dir = new File(path);

        if (!dir.exists() || !dir.isDirectory()) {
            return fileList;
        }

        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.sort(files, (a, b) -> b.getName().compareTo(a.getName()));
            for (File file : files) {
                fileList.add(new FileInfo(
                        file.getName(),
                        file.getAbsolutePath(),
                        file.length(),
                        file.lastModified(),
                        file.isDirectory()
                ));
            }
        }

        return fileList;
    }

    public static String getStoragePath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    public static String getRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static long getDirectorySize(File dir) {
        long size = 0;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += file.isDirectory() ? getDirectorySize(file) : file.length();
                }
            }
        }
        return size;
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteFile(child.getAbsolutePath());
                }
            }
        }
        return file.delete();
    }
}
