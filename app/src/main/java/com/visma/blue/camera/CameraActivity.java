package com.visma.blue.camera;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.visma.blue.R;
import com.visma.blue.misc.ChangeFragment;

public class CameraActivity extends FragmentActivity implements ChangeFragment {
    public static final String EXTRA_DATA_PHOTO_TYPE = "EXTRA_DATA_PHOTO_TYPE";

    public static final String ACTIVITY_RESULT_CODE_METADATA = "ACTIVITY_RESULT_CODE_METADATA";
    public static final String ACTIVITY_RESULT_CODE_METADATA_DOCUMENT_CREATION_DATE = "ACTIVITY_RESULT_CODE_METADATA_DOCUMENT_CREATION_DATE";

    private int mPhotoType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.blue_activity_camera);

        Bundle bundle = null;
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            bundle = savedInstanceState;
        } else if (getIntent().getExtras() != null && !getIntent().getExtras().isEmpty()) {
            bundle = getIntent().getExtras();
        }

        if (bundle != null) {
            this.mPhotoType = bundle.getInt(EXTRA_DATA_PHOTO_TYPE, 0);
        }

        boolean featureFlagCamera2 = getResources().getBoolean(R.bool.blue_feature_flag_camera2);
        if (featureFlagCamera2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            changeFragmentWithoutBackStack(new Camera2Fragment());
        } else {
            changeFragmentWithoutBackStack(new CameraFragment());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_DATA_PHOTO_TYPE, this.mPhotoType);
    }

    protected int getPhotoType() {
        return this.mPhotoType;
    }

    public void changeFragmentWithBackStack(Fragment fragment) {
        String tag = fragment.getClass().getName();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    public void changeFragmentWithoutBackStack(Fragment fragment) {
        String tag = fragment.getClass().getName();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, tag)
                .commit();
    }

    @Override
    public void onBackPressed() {
        // The CameraFragment is not added to the backstack because of problems with the resume.
        // Therefore we need to add it manually to the backstack if we are moving backwards from
        // the PreviewFragment
        String tag = PreviewFragment.class.getName();
        FragmentManager fm = this.getSupportFragmentManager();
        // The backstack is empty and preview is active
        if (fm.getBackStackEntryCount() == 0 && fm.findFragmentByTag(tag) != null) {
            boolean featureFlagCamera2 = getResources().getBoolean(R.bool.blue_feature_flag_camera2);
            if (featureFlagCamera2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                changeFragmentWithoutBackStack(new Camera2Fragment());
            } else {
                changeFragmentWithoutBackStack(new CameraFragment());
            }
        } else {
            super.onBackPressed();
        }
    }
}
