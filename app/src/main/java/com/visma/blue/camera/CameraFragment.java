package com.visma.blue.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.misc.AppId;
import com.visma.blue.misc.ChangeFragment;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.TempBitmaps;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CameraFragment extends Fragment {
    private ChangeFragment mChangeFragmentCallback;

    private AppId mAppId;
    private Preview mPreview;
    private View view;
    private Camera mCamera;
    private Boolean mDontPopBackstack = false;
    private String mUsingQrString;
    private Date mDocumentCreationDate;
    private List mPhonesWithBuggyFlash;

    // QR
    private ImageScanner mScanner;

    static {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            System.loadLibrary("iconv");
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.logPageView(Logger.VIEW_CAMERA);

        // Some samsung phones seem to have a bug that makes the phone crash if
        // using continous autofocus and "flash on" at the same time.
        // Samsung Galaxy S6 Edge, Samsung Galaxy S6, Samsung Galaxy S5 Neo, Samsung Galaxy S7,
        // Samsung Galaxy S7 Edge
        mPhonesWithBuggyFlash = Arrays.asList("SM-G925F", "SM G920F", "SM G903F", "SM-G930F", "SM-G935F");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setupScanner();
        }

        // Create a container that will hold a SurfaceView for camera previews
        mPreview = new Preview(getActivity(), previewCb);
    }

    // QR
    private void setupScanner() {
        mScanner = new ImageScanner();

        mScanner.setConfig(0, Config.X_DENSITY, 1);
        mScanner.setConfig(0, Config.Y_DENSITY, 1);

        mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
        mScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);
    }

    // This snippet hides the system bars.
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void hideSystemUi(View view) {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void showSystemUi(View view) {
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        view = inflater.inflate(R.layout.blue_fragment_camera, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hideSystemUi(view);
        }

        FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);

        FrameLayout layout = (FrameLayout) mPreview.getParent();
        if (layout != null) {
            layout.removeAllViews();
        }

        preview.addView(mPreview);

        View buttonPhoto = view.findViewById(R.id.blue_fragment_camera_button_photo);
        buttonPhoto.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                v.setEnabled(false); // We don't want more than one click on this one.

                Logger.logAction(Logger.ACTION_CAMERA, Pair.create("Flash setting", VismaUtils
                        .getCurrentFlash(inflater.getContext())));

                final ShutterCallback shutterCallback = new ShutterCallback() {
                    public void onShutter() {
                        Context context = getActivity();
                        if (context != null) {
                            AudioManager mgr = (AudioManager) context.getSystemService(Context
                                    .AUDIO_SERVICE);
                            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
                        }
                    }
                };

                final PictureCallback picture = new PictureCallback() {
                    public void onPictureTaken(byte[] data, Camera camera) {
                        // Trying to solve problem with small heap phones.
                        // We sample it harder before the resize and hope for the best.
                        long maxVmHeap = Runtime.getRuntime().maxMemory() / 1024;
                        if (maxVmHeap <= 25 * 1024) {
                            BitmapFactory.Options opts = new BitmapFactory.Options();
                            opts.inScaled = false;
                            opts.inSampleSize = 1;
                            opts.inJustDecodeBounds = true;
                            BitmapFactory.decodeByteArray(data, 0, data.length, opts);
                            while (opts.outHeight > 1024 || opts.outWidth > 1024) {
                                opts.inSampleSize *= 2;
                                BitmapFactory.decodeByteArray(data, 0, data.length, opts);
                            }

                            opts.inJustDecodeBounds = false;
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                                    opts);
                            data = null;

                            if (VismaUtils.shouldRotateBitmap(bitmap)) {
                                bitmap = VismaUtils.getResizedBitmap(bitmap, 1024, true);
                            }

                            showNextFragment(bitmap);
                            return;
                        }

                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inScaled = false; // http://developer.android
                        // .com/guide/practices/screens_support.html#scaling
                        opts.inSampleSize = 1;
                        Bitmap resizedBitmap = null;
                        while (resizedBitmap == null) {
                            Bitmap bitmap = null;
                            try {
                                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
                                resizedBitmap = bitmap;
                                resizedBitmap = VismaUtils.getResizedBitmap(bitmap, getResources()
                                        .getInteger(R.integer.targetImageHeight), VismaUtils
                                        .shouldRotateBitmap(bitmap));
                            } catch (OutOfMemoryError e) {
                                if (bitmap != null) {
                                    bitmap.recycle();
                                    bitmap = null;
                                }
                                if (resizedBitmap != null) {
                                    resizedBitmap.recycle();
                                    resizedBitmap = null;
                                }
                                opts.inSampleSize *= 2;
                            }

                            if (bitmap != null && bitmap != resizedBitmap) {
                                bitmap.recycle();
                                bitmap = null;
                            }
                        }
                        data = null; // Don't hang on to this one. Let the GC work.
                        //alert.closeDialog();
                        showNextFragment(resizedBitmap);
                    }
                };

                mCamera.autoFocus(new AutoFocusCallback() {
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (mCamera != null) {
                            mCamera.takePicture(shutterCallback, null, picture);
                        }
                    }
                });
            }
        });

        View buttonLoad = view.findViewById(R.id.blue_fragment_camera_button_gallery);
        buttonLoad.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Logger.logAction(Logger.ACTION_GALLERY);

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);
                mDontPopBackstack = true; // Prevent the Fragment from being popped in onPause

                /*
                //todo start of test code
                Parameters oldParameters = mCamera.getParameters();
                Parameters newParameters = oldParameters;
                newParameters.setPreviewFrameRate(2);
                mCamera.setParameters(newParameters);
                //todo end of test code
                */
            }
        });

        RelativeLayout buttonFlash = (RelativeLayout) view.findViewById(R.id.buttonFlash);
        buttonFlash.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Parameters parameters = mCamera.getParameters();
                String currentFlashMode = getFilteredFlashMode(parameters.getFlashMode());
                List<String> supportedFlashModes = parameters.getSupportedFlashModes();

                // Some samsung phones seem to have a bug that makes the phone crash if
                // using continous autofocus and flash at the same time.
                String[] flashModeCycle;
                if (mPhonesWithBuggyFlash.contains(Build.MODEL)) {
                    flashModeCycle = new String[]{
                            Parameters.FLASH_MODE_OFF,
                            Parameters.FLASH_MODE_AUTO,};
                } else {
                    flashModeCycle = new String[]{
                            Parameters.FLASH_MODE_OFF,
                            Parameters.FLASH_MODE_AUTO,
                            Parameters.FLASH_MODE_ON};
                }

                // Find the index of the current flash mode
                int flashModeIndex;
                for (flashModeIndex = 0; flashModeIndex < flashModeCycle.length; flashModeIndex++) {
                    if (flashModeCycle[flashModeIndex].equalsIgnoreCase(currentFlashMode)) {
                        break;
                    }
                }

                // Find the next supported flash mode index
                int newFlashModeIndex = (flashModeIndex + 1) % flashModeCycle.length;
                while (newFlashModeIndex != flashModeIndex) {
                    if (supportedFlashModes.contains(flashModeCycle[newFlashModeIndex])) {
                        break;
                    } else {
                        newFlashModeIndex = (newFlashModeIndex + 1) % flashModeCycle.length;
                    }
                }
                String newFlashMode = flashModeCycle[newFlashModeIndex];
                parameters.setFlashMode(newFlashMode);
                //updateFlashButtonText(newFlashMode); //original statement

                //todo: Change the comments from the first to the second statement after this one.
                //int previewFrameRate = mCamera.getParameters().getPreviewFrameRate();
                updateFlashButtonText(Long.toString(numberOfFrames));
                //updateFlashButtonText(Integer.toString(previewFrameRate));
                VismaUtils.setCurrentFlash(getActivity(), newFlashMode);
                //mCamera.setParameters(oldParameters);
                //todo: End of test code

                mCamera.setParameters(parameters);
            }
        });

        return view;
    }

    private String getFilteredFlashMode(String flashMode) {
        if (!flashMode.equalsIgnoreCase(Parameters.FLASH_MODE_ON)) {
            return flashMode;
        }

        if (mPhonesWithBuggyFlash.contains(Build.MODEL)) {
            return Parameters.FLASH_MODE_AUTO;
        } else {
            return Parameters.FLASH_MODE_ON;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case 10:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    releaseCamera();

                    Uri selectedImage = imageReturnedIntent.getData();
                    mDocumentCreationDate = getDocumentCreatedDate(selectedImage);
                    Bitmap resizedBitmap = VismaUtils.getCompressedBitmapFromUri(selectedImage,
                            getContext());


                    if (resizedBitmap != null) {
                        showNextFragment(resizedBitmap);
                    }
                }
                break;
            default:
                break;
        }
    }

    private Date getDocumentCreatedDate(Uri selectedImage) {
        try {
            InputStream inputStream = getActivity().getContentResolver()
                    .openInputStream(selectedImage);
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);

            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory
                    .class);
            if (directory != null) {
                Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (date != null) {
                    return date;
                } else {
                    return directory.getDate(ExifSubIFDDirectory.TAG_DATETIME);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mChangeFragmentCallback = (ChangeFragment) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement ChangeFragment interface.");
        }
    }

    private void updateFlashButtonText(String flashMode) {
        TextView flashLabel = (TextView) view.findViewById(R.id.flashTextView);


        flashLabel.setText(flashMode);

        /*
        if (flashMode.equalsIgnoreCase(Parameters.FLASH_MODE_OFF)) {
            flashLabel.setText(R.string.visma_blue_flash_off);
        } else if (flashMode.equalsIgnoreCase(Parameters.FLASH_MODE_AUTO)) {
            flashLabel.setText(R.string.visma_blue_flash_auto);
        } else {
            //flashLabel.setText(R.string.visma_blue_flash_on);
            flashLabel.setText(flashMode);
        }
        */
    }

    private boolean supportsFlash(Camera camera) {
        return camera.getParameters().getSupportedFlashModes() != null;
    }

    @Override
    public void onResume() {
        super.onResume();

        mDontPopBackstack = false;

        if (mCamera == null) {

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

            mPreview.switchCamera(mCamera);

            Parameters parameters = mCamera.getParameters();
            parameters.setRotation(cameraInfo.orientation);

            if (supportsFlash(mCamera)) {
                String flashMode = getFilteredFlashMode(VismaUtils.getCurrentFlash(getActivity())
                ); // default: off
                parameters.setFlashMode(flashMode);
                updateFlashButtonText(flashMode);
            } else {
                view.findViewById(R.id.buttonFlash).setVisibility(View.GONE);
            }

            mCamera.setParameters(parameters);
        }

        mAppId = BlueConfig.getAppType();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!mDontPopBackstack) { // We don't support pausing with the camera active
            getActivity().finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }



    private void showNextFragment(Bitmap bitmap) {
        mDontPopBackstack = true;

        getContext().getContentResolver().update(BlueContentProvider
                .CONTENT_URI_METADATA_TEMP_BITMAP, TempBitmaps
                .getTempBitmapValues(bitmap), null, null);
        bitmap.recycle();
        bitmap = null;

        PreviewFragment previewFragment = new PreviewFragment();
        Bundle args = new Bundle();
        args.putString(PreviewFragment.USING_QR_STRING, mUsingQrString);
        if (mDocumentCreationDate != null) {
            args.putLong(PreviewFragment.DOCUMENT_CREATION_DATE, mDocumentCreationDate.getTime());
        }
        previewFragment.setArguments(args);
        mChangeFragmentCallback.changeFragmentWithoutBackStack(previewFragment);
    }

    private String getQrStringUsingZbar(byte[] data, Parameters parameters) {
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

    private String getQrStringUsingPlayServices(byte[] data, Parameters parameters) {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getActivity()
                .getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        if (!barcodeDetector.isOperational()) {
            //txtView.setText("Could not set up the detector!");
        } else {
            int previewImageFormat = parameters.getPreviewFormat(); //should be NV21 as that is
            // default
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;
            Frame frame = new Frame.Builder()
                    .setImageData(ByteBuffer.wrap(data), width, height, previewImageFormat)
                    //ImageFormat.NV21
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

    //Todo: Test code
    private long numberOfFrames = 0;
    //Todo: End of test code

    // QR
    PreviewCallback previewCb = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            //Todo: This is where test code starts.

            numberOfFrames += 1;



            //Todo: This is where test code ends.
            if (mAppId != AppId.EACCOUNTING && mAppId != AppId.VISMA_ONLINE) {
                return;
            }

            String qrContent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                qrContent = getQrStringUsingPlayServices(data, camera.getParameters());
            } else {
                qrContent = getQrStringUsingZbar(data, camera.getParameters());
            }

            if (qrContent == null) {
                return;
            }

            if (mUsingQrString != null && mUsingQrString.equals(qrContent)) {
                return;
            }

            Logger.logAction(Logger.ACTION_QR);
            CameraFragment.this.mUsingQrString = qrContent;
            FragmentActivity activity = getActivity();
            if (activity != null) {
                Toast toast = Toast.makeText(activity.getApplicationContext(), R.string
                        .visma_blue_toast_read_from_qr, Toast.LENGTH_SHORT);
                int extraOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        50, getResources().getDisplayMetrics());
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, toast.getXOffset(),
                        toast.getYOffset() + extraOffset);
                toast.show();
            }
        }
    };
}
