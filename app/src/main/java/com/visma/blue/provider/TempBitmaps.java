package com.visma.blue.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;

import com.visma.blue.misc.VismaUtils;

public class TempBitmaps implements BaseColumns {
    public static final String TEMP_BITMAP_IMAGE = "image";

    public static String[] getAllColumnNames() {
        return new String[]{
                TEMP_BITMAP_IMAGE,
        };
    }

    public static ContentValues getTempBitmapValues(Bitmap bitmap) {
        byte[] bitmapData = VismaUtils.compress(bitmap);
        ContentValues values = new ContentValues();
        values.put(TEMP_BITMAP_IMAGE, bitmapData);
        return values;
    }

    public static ContentValues getTempBitmapValues(byte[] bitmapData) {
        ContentValues values = new ContentValues();
        values.put(TEMP_BITMAP_IMAGE, bitmapData);
        return values;
    }

    public static byte[] getBitmapData(Cursor cursor) {
        byte[] bitmapData = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(TEMP_BITMAP_IMAGE);
                bitmapData = cursor.getBlob(columnIndex);
                cursor.close();
            }
        }

        return bitmapData;
    }
}
