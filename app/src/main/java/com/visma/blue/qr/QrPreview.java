package com.visma.blue.qr;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QrPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private PreviewCallback previewCallback;
    private AutoFocusCallback autoFocusCallback;
    private boolean mIsUsingContinousAutoFocus;

    public QrPreview(Context context) throws Exception {
        super(context);

        if (!isInEditMode()) {
            throw new Exception("Do not use this constructor.");
        }
    }

    public QrPreview(Context context, Camera camera,
                     PreviewCallback previewCb,
                     AutoFocusCallback autoFocusCb) {
        super(context);
        mCamera = camera;
        previewCallback = previewCb;
        autoFocusCallback = autoFocusCb;

        setupContinuousFocus();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        setHolderType(mHolder);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setupContinuousFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { // FOCUS_MODE_CONTINUOUS_PICTURE is added in 14
            Parameters parameters = mCamera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            for (String focusMode : focusModes) {
                if (focusMode.equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    mIsUsingContinousAutoFocus = true;
                    mCamera.setParameters(parameters);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void setHolderType(SurfaceHolder holder) {
        // This is needed on gingerbread devices, otherwise it will crash
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            if (!mIsUsingContinousAutoFocus) {
                mCamera.autoFocus(autoFocusCallback);
            }
        } catch (IOException e) {
            Log.d("DBG", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Camera preview released in activity
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*
         * If your preview can change or rotate, take care of those events here.
         * Make sure to stop the preview before resizing or reformatting it.
         */
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        try {
            Parameters parameters = mCamera.getParameters();
            List<Size> previewSizes = parameters.getSupportedPreviewSizes();
            float targetRatio = (float) height / width;
            Size previewSize = getBestMatchingSize(previewSizes, targetRatio, height);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            mCamera.setParameters(parameters);

            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
            if (!mIsUsingContinousAutoFocus) {
                mCamera.autoFocus(autoFocusCallback);
            }
        } catch (Exception e) {
            Log.d("DBG", "Error starting camera preview: " + e.getMessage());
        }
    }

    private Size getBestMatchingSize(List<Size> sizes, float targetRatio, int desiredImageHeight) {
        Size bestSize = sizes.get(0);
        float bestRatioDiff = 100;

        //Must sort the sizes as they come in different order on different devices
        Collections.sort(sizes, new Comparator<Size>() {

            @Override
            public int compare(Size lhs, Size rhs) {
                return rhs.width - lhs.width;
            }
        });

        // Find the one that has the smallest diff to the desired aspect ratio.
        // If more than one, the largest will be chosen because the list is sorted on "width".
        for (Size size : sizes) {
            //Log.d("Blue", "width: " + size.width + " height: " + size.height + " ratio: " + (float) size.width / size.height);
            float ratio = (float) size.width / size.height;
            float ratioDiff = Math.abs(targetRatio - ratio);
            if (ratioDiff < bestRatioDiff || ratioDiff == bestRatioDiff && size.width >= desiredImageHeight) {
                bestRatioDiff = ratioDiff;
                bestSize = size;
            }
        }

        Log.d("Blue", "target ratio: " + targetRatio + " achieved ratio: " + (float) bestSize.width / bestSize.height);
        Log.d("Blue", "Chooses width: " + bestSize.width + " height: " + bestSize.height + " ratio: " + (float) bestSize.width / bestSize.height);
        return bestSize;
    }
}
