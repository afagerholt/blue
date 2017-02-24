package com.visma.blue.misc;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class FileManager {

    public static void saveDocumentInLocalStorage(Context context, byte[] documentData, String
            fileName) {
        if (fileName == null) {
            return;
        }

        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(documentData);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateLocalFileName(String originalFileName) {
        return System.currentTimeMillis() + originalFileName;
    }

    public static void removeDocumentFromLocalStorage(Context context, String fileName) {
        if (fileName == null) {
            return;
        }
        context.deleteFile(fileName);
    }

    public static File getLocallySavedFile(Context context, String fileName) {
        String filePath = context.getFilesDir() + "/" + fileName;
        return new File(filePath);
    }

    public static File getDownloadFolderPath(Context context) {
        File path = null;
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            path = context.getExternalFilesDir(Environment
                    .DIRECTORY_DOWNLOADS);
        }

        if (path == null) {
            path = context.getCacheDir();
        }

        if (path != null && !path.exists()) {
            path.mkdirs();
        }

        return path;
    }

    public static File exportFile(File src, File dst, String fileName) throws IOException {

        //if folder does not exist
        if (!dst.exists()) {
            if (!dst.mkdir()) {
                return null;
            }
        }

        File expFile = new File(dst.getPath() + fileName);
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }

            if (outChannel != null) {
                outChannel.close();
            }
        }

        return expFile;
    }

    public static String getFilePathFromUri(Uri fileUri, String fileName, Context context) {
        if (fileUri.toString().contains("content")) {
            try {
                InputStream stream = context.getContentResolver().openInputStream(fileUri);
                File tempFileDir = FileManager.getDownloadFolderPath(context);
                if (tempFileDir == null) {
                   return null;
                } else {
                    File tempFile;
                    if (fileName == null) {
                        tempFile = new File(tempFileDir, fileUri
                                .getLastPathSegment());
                    } else {
                        tempFile = new File(tempFileDir, fileName);
                    }

                    tempFile.setReadable(true, false);
                    if (!tempFile.exists()) {
                        copyInputStreamToFile(stream, tempFile);
                    }
                    return tempFile.getAbsolutePath();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            return fileUri.getPath();
        }

        return null;
    }

    private static void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFilesFromDownloads(final Context context) {
        Thread deleteThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    deleteDir(getDownloadFolderPath(context));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        deleteThread.start();


    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }
        return false;
    }

}
