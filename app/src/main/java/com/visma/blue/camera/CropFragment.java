package com.visma.blue.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.edmodo.cropper.CropImageView;

import com.visma.blue.R;
import com.visma.blue.misc.Logger;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.TempBitmaps;

public class CropFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.blue_fragment_crop, container, false);

        Logger.logPageView(Logger.VIEW_CROP);

        Button buttonUse = (Button) view.findViewById(R.id.blue_fragment_button_use);
        buttonUse.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPreviewFragment();
                Logger.logAction(Logger.ACTION_CROP);
            }
        });

        CropImageView cropImageView = (CropImageView) view.findViewById(R.id
                .blue_fragment_crop_cropImageView);
        byte[] bitmapData = TempBitmaps.getBitmapData(getContext()
                .getContentResolver().query(BlueContentProvider
                .CONTENT_URI_METADATA_TEMP_BITMAP, TempBitmaps
                .getAllColumnNames(), null, null, null));

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false; // http://developer.android.com/guide/practices/screens_support
        // .html#scaling
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length, opts);
        if (bitmap != null) {
            cropImageView.setImageBitmap(bitmap);
        }

        Button buttonCancel = (Button) view.findViewById(R.id.blue_fragment_button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });

        return view;
    }

    private void showPreviewFragment() {
        CropImageView cropImageView = (CropImageView) this.getView().findViewById(R.id
                .blue_fragment_crop_cropImageView);
        try {
            getContext().getContentResolver().update(BlueContentProvider
                    .CONTENT_URI_METADATA_TEMP_BITMAP, TempBitmaps
                    .getTempBitmapValues(cropImageView.getCroppedImage()), null, null);
        } catch (OutOfMemoryError e) {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string
                    .visma_blue_error_unknown_error, Toast.LENGTH_SHORT);
            toast.show();
        }

        cropImageView.setImageBitmap(null);

        FragmentManager fm = getFragmentManager();
        fm.popBackStack();
    }
}
