package com.visma.blue.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.visma.blue.R;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Preview extends ViewGroup implements SurfaceHolder.Callback {
    private static final String TAG = "Preview";

    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Size mPreviewSize;
    private Camera mCamera;
    private PreviewCallback previewCallback;
    private boolean mSurfaceCreated = false;
    private Point mDisplaySize;

    public Preview(Context context) throws Exception {
        super(context);

        if (!isInEditMode()) {
            throw new Exception("Do not use this constructor.");
        }
    }

    public Preview(Context context, PreviewCallback previewCb) {
        super(context);

        this.mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        this.mHolder = mSurfaceView.getHolder();
        this.mHolder.addCallback(this);
        setHolderType(mHolder);

        this.previewCallback = previewCb;

        // Get the wanted size of the preview in fullscreen
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mDisplaySize = new Point();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(mDisplaySize);
        } else {
            display.getSize(mDisplaySize);
        }
    }

    @SuppressWarnings("deprecation")
    private void setHolderType(SurfaceHolder holder) {
        // This is needed on gingerbread devices, otherwise it will crash
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void clearSurfaceView() {
        this.removeAllViews();
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            if (mSurfaceCreated) {
                requestLayout();
            }
        }
    }

    public void switchCamera(Camera camera) {
        setCamera(camera);
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }

        if (camera != null) {
            setupContinuousFocus();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        int previewContainerWidth = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int previewContainerHeight = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(previewContainerWidth, previewContainerHeight);

        if (mCamera != null) {
            Parameters parameters = mCamera.getParameters();
            List<Size> previewSizes = parameters.getSupportedPreviewSizes();
            List<Size> pictureSizes = parameters.getSupportedPictureSizes();

            removeUnwantedAspectRatios(previewSizes, pictureSizes);

            previewContainerWidth = mDisplaySize.x;
            previewContainerHeight = mDisplaySize.y;

            float targetAspectRatio = (float) previewContainerHeight / previewContainerWidth;
            Log.d("Ruby", "Selecting preview size");
            mPreviewSize = getBestMatchingSize(previewSizes, targetAspectRatio, previewContainerHeight);

            targetAspectRatio = (float) mPreviewSize.width / mPreviewSize.height;
            int targetImageHeight = this.getResources().getInteger(R.integer.targetImageHeight);
            Log.d("Ruby", "Selecting picture size");
            Size pictureSize = getBestMatchingSize(pictureSizes, targetAspectRatio, targetImageHeight);

            // If neither the width nor the height is larger than 1024 then we don't care about the aspect ratio
            // when we choose the sizes anymore.
            if (!(pictureSize.width >= 1024 || pictureSize.height >= 1024)) {
                pictureSize = getBackupPictureSize(pictureSizes, 1024, targetImageHeight);
                float pictureAspectRatio = (float) pictureSize.width / pictureSize.height;
                mPreviewSize = getBestMatchingSize(previewSizes, pictureAspectRatio, previewContainerHeight);
            }

            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            setAspectRatioOfSurfaceView(previewContainerWidth, previewContainerHeight, mPreviewSize.width, mPreviewSize.height);

            mCamera.setParameters(parameters);
        }
    }

    /**
     * Intended to be called whenever the setPreviewSize has been called in order to make the
     * SurfaceView
     * have the same aspect ratio as the preview so that we don't get a stretched image.
     */
    private void setAspectRatioOfSurfaceView(int previewContainerWidth,
                                             int previewContainerHeight,
                                             int previewWidth,
                                             int previewHeight) {
        // width/height for camera
        float previewProportion = (float) previewWidth / (float) previewHeight;
        // height/width for views
        float containerProportion = (float) previewContainerHeight / (float) previewContainerWidth;

        // Get the SurfaceView layout parameters
        android.view.ViewGroup.LayoutParams lp = ((View) mSurfaceView.getParent()).getLayoutParams();
        if (previewProportion > containerProportion) {
            lp.width = (int) (previewContainerHeight / previewProportion);
            lp.height = previewContainerHeight;
        } else {
            lp.width = previewContainerWidth;
            lp.height = (int) (previewContainerWidth * previewProportion);
        }

        // Commit the layout parameters
        ((View) mSurfaceView.getParent()).setLayoutParams(lp);
    }

    private void removeUnwantedAspectRatios(List<Size> previewSizes, List<Size> pictureSizes) {
        // Remove all aspect ratios that are not available in both of the lists.
        Map<Float, Size> previewRatios = new HashMap<Float, Size>();
        Map<Float, Size> pictureRatios = new HashMap<Float, Size>();

        for (Size size : previewSizes) {
            float ratio = (float) size.width / size.height;
            if (!previewRatios.containsKey(ratio)) {
                previewRatios.put(ratio, null);
            }
        }

        for (Size size : pictureSizes) {
            float ratio = (float) size.width / size.height;
            if (!pictureRatios.containsKey(ratio)) {
                pictureRatios.put(ratio, null);
            }
        }

        Iterator<Size> iterator = pictureSizes.iterator();
        while (iterator.hasNext()) {
            Size size = iterator.next();
            float ratio = (float) size.width / size.height;
            if (!previewRatios.containsKey(ratio)) {
                iterator.remove();
            }
        }

        iterator = previewSizes.iterator();
        while (iterator.hasNext()) {
            Size size = iterator.next();
            float ratio = (float) size.width / size.height;
            if (!pictureRatios.containsKey(ratio)) {
                iterator.remove();
            }
        }
    }

    private Size getBackupPictureSize(List<Size> sizes, int suggestedMin, int suggestedMax) {
        boolean suggestedMinFound = false;

        Size bestSize = sizes.get(0);

        // Sort the list from the smallest to the largest width
        Collections.sort(sizes, new Comparator<Size>() {

            @Override
            public int compare(Size lhs, Size rhs) {
                return lhs.width - rhs.width;
            }
        });

        // Find one that is at least as large as "suggestedMin"
        for (Size size : sizes) {
            if (size.width > suggestedMax && suggestedMinFound) {
                break; // We have found our minimum size and all the others are too large.
            }

            bestSize = size;

            if (size.width >= suggestedMin) {
                suggestedMinFound = true;
            }
        }

        return bestSize;
    }

    private Size getBestMatchingSize(List<Size> sizes, float targetRatio, int desiredLargestSide) {
        Size bestSize = sizes.get(0);
        float bestRatioDiff = 100;
        boolean largeEnoughSizeFound = false;

        // Sort the list from the smallest to the largest width
        Collections.sort(sizes, new Comparator<Size>() {

            @Override
            public int compare(Size lhs, Size rhs) {
                return lhs.width - rhs.width;
            }
        });

        // Find the one that has the smallest diff to the desired aspect ratio.
        // If more than one, the largest will be chosen because the list is sorted on "width".
        for (Size size : sizes) {
            float ratio = (float) size.width / size.height;
            float ratioDiff = Math.abs(targetRatio - ratio);

            Log.d("Ruby", "width: " + size.width + "\theight: " + size.height);
            Log.d("Ruby", "\tratio: " + ratio);
            Log.d("Ruby", "\tratio diff: " + ratioDiff);

            if (ratioDiff < bestRatioDiff // Better fit to the aspect ratio
                    // Equal fit to the aspect ratio, but we would like a larger size
                    || ratioDiff == bestRatioDiff && largeEnoughSizeFound == false) {
                bestRatioDiff = ratioDiff;
                bestSize = size;

                if (size.width >= desiredLargestSide) {
                    largeEnoughSizeFound = true;
                }
            }
        }

        return bestSize;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            child.layout(0, 0, width, height);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
        if (mPreviewSize == null) {
            requestLayout();
        }
        mSurfaceCreated = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        if (mCamera != null) {
            Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();

            mCamera.setParameters(parameters);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
        }
    }

    // QR
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setupContinuousFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { // FOCUS_MODE_CONTINUOUS_PICTURE is added in 14
            Parameters parameters = mCamera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            for (String focusMode : focusModes) {
                if (focusMode.equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    mCamera.setParameters(parameters);
                    break;
                }
            }
        }
    }
}