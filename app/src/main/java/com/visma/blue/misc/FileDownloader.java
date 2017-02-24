package com.visma.blue.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloader {
    public static boolean downloadFile(URL url, File file) {
        boolean success = false;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("GET");
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.connect();
            InputStream in = httpUrlConnection.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len1);
            }
            fileOutputStream.close();
            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }
}
