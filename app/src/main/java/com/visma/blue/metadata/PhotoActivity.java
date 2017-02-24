package com.visma.blue.metadata;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.visma.blue.R;
import com.visma.blue.misc.ErrorMessage;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.BlueNetworkError;
import com.visma.blue.network.OnlineResponseCodes;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.GetPhotoAnswer;
import com.visma.blue.network.requests.GetPhotoRequest;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.TempBitmaps;
import com.visma.common.VismaAlertDialog;

public class PhotoActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO_ID = "EXTRA_PHOTO_ID";

    private ImageView mPhotoImageHolder;
    private VismaAlertDialog alert;
    private int mScreenWight = 0;
    private int mScreenHeight = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.blue_fragment_photo);

        Logger.logPageView(Logger.VIEW_DOCUMENT);
        mPhotoImageHolder = (ImageView) findViewById(R.id.photoImageView);
        alert = new VismaAlertDialog(PhotoActivity.this);

        if (getIntent().hasExtra(EXTRA_PHOTO_ID)) {
            loadImageFromUrl(getIntent().getStringExtra(EXTRA_PHOTO_ID));
        } else {
            loadImageFromLocalDatabase();
        }
    }

    private void loadImageFromLocalDatabase() {
        byte[] bitmapData = TempBitmaps.getBitmapData(getContentResolver()
                .query(BlueContentProvider.CONTENT_URI_METADATA_TEMP_BITMAP, TempBitmaps
                        .getAllColumnNames(), null, null, null));
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false; // http://developer.android.com/guide/practices/screens_support
        // .html#scaling
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length, opts);
        mPhotoImageHolder.setImageBitmap(bitmap);
    }

    private void loadImageFromUrl(String finalPhotoId) {
        disableZoom();
        setFullScreenPlaceHolder(mPhotoImageHolder);
        final String cachedPhotoUrl = VismaUtils.getCachedImageUrl(PhotoActivity.this);
        if (cachedPhotoUrl != null) {
            loadImageFromCache(cachedPhotoUrl);
        } else {
            loadImageFromBackEnd(finalPhotoId);
        }

    }

    private void setFullScreenPlaceHolder(ImageView photoView) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mScreenHeight = metrics.heightPixels;
        mScreenWight = metrics.widthPixels;
        Bitmap placeHolder = Bitmap.createBitmap(mScreenWight, mScreenHeight, Bitmap
                .Config
                .RGB_565);
        placeHolder.eraseColor(Color.BLACK);
        photoView.setImageBitmap(placeHolder);
    }

    private void loadImageFromCache(String cachedPhotoUrl) {
        Picasso.with(PhotoActivity.this)
                .load(cachedPhotoUrl)
                .noPlaceholder()
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .centerInside()
                .noFade()
                .into(mPhotoImageHolder, new Callback() {
                    @Override
                    public void onSuccess() {
                        enableZoom();
                    }

                    @Override
                    public void onError() {
                        showErrorMessage(null);
                    }
                });
    }

    private void loadImageFromBackEnd(final String finalPhotoId) {
        final String finalToken = VismaUtils.getToken();
        alert.showProgressBar();

        GetPhotoRequest<GetPhotoAnswer> request = new GetPhotoRequest<>(PhotoActivity.this,
                finalToken,
                finalPhotoId,
                GetPhotoAnswer.class,
                new Response.Listener<GetPhotoAnswer>() {
                    @Override
                    public void onResponse(final GetPhotoAnswer response) {
                        loadPhoto(response.photoUrl);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showErrorMessage(error);
                    }
                });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);
    }

    private void loadPhoto(final String photoUrl) {
        Picasso.with(PhotoActivity.this)
                .load(photoUrl)
                .noPlaceholder()
                .fit()
                .centerInside()
                .into(mPhotoImageHolder, new Callback() {
                    @Override
                    public void onSuccess() {
                        enableZoom();
                        VismaUtils.setCachedImageUrl(PhotoActivity.this, photoUrl);
                        alert.dismiss();
                    }

                    @Override
                    public void onError() {
                        showErrorMessage(null);
                    }
                });
    }

    private void showErrorMessage(VolleyError error) {
        final Activity activity = PhotoActivity.this;
        if (activity == null || activity.isFinishing()) {
            return;
        }

        int errorMessageId;

        if (error != null && error instanceof BlueNetworkError) {
            BlueNetworkError blueNetworkError = (BlueNetworkError) error;
            errorMessageId = ErrorMessage.getErrorMessage(blueNetworkError.blueError, false);
        } else {
            errorMessageId = ErrorMessage.getErrorMessage(OnlineResponseCodes.NotSet, false);
        }

        alert.setOnCloseListener(new VismaAlertDialog.OnCloseListener() {
            @Override
            public void onClose() {
                finish();
            }
        });

        alert.showError(errorMessageId);
    }

    private void enableZoom() {
        mPhotoImageHolder.setScaleType(ImageView.ScaleType.MATRIX);
        mPhotoImageHolder.setLayoutParams(new LinearLayout.LayoutParams(mScreenWight,
                mScreenHeight));
    }

    private void disableZoom() {
        mPhotoImageHolder.setScaleType(ImageView.ScaleType.CENTER);
    }
}
