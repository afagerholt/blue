package com.visma.blue.metadata.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.visma.blue.metadata.PhotoActivity;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.TempBitmaps;
import com.visma.blue.test.BaseActivityUnitTest;

public class PhotoActivityUnitTest extends BaseActivityUnitTest<PhotoActivity> {
    public PhotoActivityUnitTest() {
        super(PhotoActivity.class);
    }

    @Override
    protected Context getThemedContext() {
        Context context = super.getThemedContext();

        // We need a bitmap saved in the database for the PhotoActivity to start up properly
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.GREEN);
        context.getContentResolver().update(BlueContentProvider
                .CONTENT_URI_METADATA_TEMP_BITMAP, TempBitmaps
                .getTempBitmapValues(bitmap), null, null);
        return context;
    }
}
