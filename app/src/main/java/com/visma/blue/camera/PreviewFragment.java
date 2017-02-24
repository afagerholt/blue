package com.visma.blue.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;

import com.visma.blue.R;
import com.visma.blue.misc.ChangeFragment;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.TempBitmaps;
import com.visma.blue.qr.UsingQr;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.Date;

public class PreviewFragment extends Fragment {
    public static final String USING_QR_STRING = "USING_QR_STRING";
    public static final String DOCUMENT_CREATION_DATE = "DOCUMENT_CREATION_DATE";

    private ChangeFragment mChangeFragmentCallback;

    private Bitmap bitmap = null;
    private String mUsingQrString;
    private Date mDocumentCreationDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.blue_fragment_preview, container, false);

        Bundle bundle = null;
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            bundle = savedInstanceState;
        } else if (getArguments() != null) {
            bundle = getArguments();
        }

        if (bundle != null) {
            this.mUsingQrString = bundle.getString(USING_QR_STRING);
            if (bundle.containsKey(DOCUMENT_CREATION_DATE)) {
                this.mDocumentCreationDate = new Date(bundle.getLong(DOCUMENT_CREATION_DATE));
            }
        }

        setupUserButton(view);
        setupCropButton(view);
        setupImage(view);

        return view;
    }

    private void setupImage(View view) {
        ImageView imageViewPreview = (ImageView) view.findViewById(R.id.imageViewPreview);
        byte[] bitmapData = TempBitmaps.getBitmapData(getContext().getContentResolver()
                .query(BlueContentProvider.CONTENT_URI_METADATA_TEMP_BITMAP, TempBitmaps
                .getAllColumnNames(), null, null, null));
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false; // http://developer.android.com/guide/practices/screens_support
        // .html#scaling
        bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length, opts);

        //todo: Test code trying to manipulate bitmap and see what happens.

        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Mat invertedMat = EdgeDetector.getEdges(mat, 1);
        Utils.matToBitmap(invertedMat, bitmap);

        if (bitmap != null) {
            imageViewPreview.setImageBitmap(bitmap);
        }
    }

    private void setupCropButton(View view) {
        View buttonCrop = view.findViewById(R.id.blue_fragment_preview_crop);
        buttonCrop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                CropFragment cropFragment = new CropFragment();
                mChangeFragmentCallback.changeFragmentWithBackStack(cropFragment);
            }
        });
    }

    private void setupUserButton(View view) {
        View buttonUse = view.findViewById(R.id.buttonUse);
        buttonUse.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                returnActivityResult();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(USING_QR_STRING, mUsingQrString);
        if (mDocumentCreationDate != null) {
            outState.putLong(DOCUMENT_CREATION_DATE, mDocumentCreationDate.getTime());
        }
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

    private void returnActivityResult() {
        Date date = new Date();
        CameraActivity cameraActivity = (CameraActivity) getActivity();
        // Maybe the metadata creation should be broken out and put in the activity that started us.
        OnlineMetaData onlineMetaData = new OnlineMetaData(true, "", date, "", "", cameraActivity
                .getPhotoType());
        onlineMetaData.usingQrString = this.mUsingQrString;
        if (mUsingQrString != null) {
            Gson gson = new Gson();
            UsingQr temp = gson.fromJson(onlineMetaData.usingQrString, UsingQr.class);
            temp.updateFieldsIn(onlineMetaData);
        }

        Intent intent = new Intent();
        intent.putExtra(CameraActivity.ACTIVITY_RESULT_CODE_METADATA, onlineMetaData);
        if (mDocumentCreationDate != null) {
            intent.putExtra(CameraActivity.ACTIVITY_RESULT_CODE_METADATA_DOCUMENT_CREATION_DATE,
                    mDocumentCreationDate.getTime());
        }
        getActivity().setResult(FragmentActivity.RESULT_OK, intent);
        getActivity().finish();
    }
}
