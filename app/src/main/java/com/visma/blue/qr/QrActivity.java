package com.visma.blue.qr;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import com.visma.blue.R;
import com.visma.blue.misc.Logger;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

/* Import ZBar Class files */

public class QrActivity extends AppCompatActivity {
    public static final String ACTIVITY_RESULT_CODE_QR_MESSAGE = "ACTIVITY_RESULT_CODE_QR_MESSAGE";

    private Camera mCamera;
    private QrPreview mPreview;
    private Handler mAutoFocusHandler;

    ImageScanner mScanner;

    private boolean mPreviewing = true;

    static {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            System.loadLibrary("iconv");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.blue_activity_qr_camera);

        Logger.logPageView(Logger.VIEW_QR);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAutoFocusHandler = new Handler();

        setupCamera();
        mPreview = new QrPreview(this, mCamera, previewCb, mAutoFocusCallback);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            /* Instance barcode scanner */
            mScanner = new ImageScanner();
            /*
            scanner.setConfig(0, Config.X_DENSITY, 3);
            scanner.setConfig(0, Config.Y_DENSITY, 3);
            */

            mScanner.setConfig(0, Config.X_DENSITY, 1);
            mScanner.setConfig(0, Config.Y_DENSITY, 1);

            mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
            mScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);
        }
    }

    public void onPause() {
        super.onPause();

        this.finish();
        releaseCamera();
    }

    private void setupCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCamera = Camera.open(i);
                mCamera.setDisplayOrientation(cameraInfo.orientation);

                break;
            }
        }

        mCamera.setPreviewCallback(previewCb);
        mCamera.startPreview();
        mPreviewing = true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mPreviewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (mPreviewing) {
                mCamera.autoFocus(mAutoFocusCallback);
            }
        }
    };

    private String getQrStringUsingZbar(byte[] data, Camera.Parameters parameters) {
        Size size = parameters.getPreviewSize();

        Image barcode = new Image(size.width, size.height, "Y800");
        barcode.setData(data);

        int result = mScanner.scanImage(barcode);

        if (result != 0) {
            StringBuilder builder = new StringBuilder();
            SymbolSet syms = mScanner.getResults();
            for (Symbol sym : syms) {
                builder.append(sym.getData());
            }

            String qrContent = builder.toString();

            try {
                JSONObject object = new JSONObject(qrContent);
                if (object.has("uqr")) {
                    return qrContent;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private String getQrStringUsingPlayServices(byte[] data, Camera.Parameters parameters) {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        if (!barcodeDetector.isOperational()) {
            //txtView.setText("Could not set up the detector!");
        } else {
            int previewImageFormat = parameters.getPreviewFormat(); //should be NV21 as that is default
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;
            Frame frame = new Frame.Builder()
                    .setImageData(ByteBuffer.wrap(data), width, height, previewImageFormat) //ImageFormat.NV21
                    .build();
            SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);
            for (int i = 0; i < barcodes.size(); i++) {
                Barcode barcode = barcodes.valueAt(i);

                try {
                    JSONObject object = new JSONObject(barcode.rawValue);
                    if (object.has("uqr")) {
                        return barcode.rawValue;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    PreviewCallback previewCb = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            String qrContent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                qrContent = getQrStringUsingPlayServices(data, camera.getParameters());
            } else {
                qrContent = getQrStringUsingZbar(data, camera.getParameters());
            }

            if (qrContent == null) {
                return;
            }

            Logger.logAction(Logger.ACTION_QR);

            mPreviewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();

            Toast toast = Toast.makeText(getApplicationContext(), R.string.visma_blue_toast_read_from_qr, Toast.LENGTH_LONG);
            toast.show();

            Intent intent = new Intent();
            intent.putExtra(ACTIVITY_RESULT_CODE_QR_MESSAGE, qrContent);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    // Mimic continuous auto-focusing
    AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            mAutoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

}