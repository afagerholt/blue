package com.visma.blue.main;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.View;

import com.visma.blue.archive.ArchiveFragment;
import com.visma.blue.BaseActivityTest;
import com.visma.blue.BlueMainActivity;
import com.visma.blue.R;
import com.visma.blue.camera.CameraActivity;
import com.visma.blue.events.MetadataEvent;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ActivityController;

import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

public class ArchiveFragmentTest extends BaseActivityTest {

    private static final String preferenceFile = "MobileScanner";
    private static final String demoKey = "Demo";
    private static final String syncMode = "SyncMode";

    private ActivityController<BlueMainActivity> mBlueActivityController;
    private BlueMainActivity mBlueMainActivity;
    private ArchiveFragment mArchiveFragment;
    private SharedPreferences mSharedPreferences;

    private void cleanUpCurrentActivityIfNeeded() {
        if (mBlueActivityController != null) {
            mBlueActivityController.pause().stop().destroy();

            mArchiveFragment = null;
            mBlueActivityController = null;
            mBlueMainActivity = null;
        }
    }

    private void createActivity() {
        cleanUpCurrentActivityIfNeeded();

        mBlueActivityController = Robolectric.buildActivity(BlueMainActivity.class);

        mBlueMainActivity = mBlueActivityController
                .create()
                .start()
                .resume()
                .visible()
                .get();

        mArchiveFragment = (ArchiveFragment) mBlueMainActivity.getSupportFragmentManager()
                .findFragmentById(android.R.id.content);
    }

    @Before
    public void setup() {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void shouldCreateArchiveFragment() {
        // when
        createActivity();

        // then
        assertNotNull("Archive fragment should be created!", mArchiveFragment);
    }

    @Test
    public void shouldCreateViews() {
        // when
        createActivity();

        // then
        View rootView = mArchiveFragment.getView();
        assertNotNull("Root view in ArchiveFragment is null!",
                rootView);
        assertNotNull("Swipe refresh layout is null!",
                rootView.findViewById(R.id.blue_fragment_archive_swipe_refresh_layout));
        assertNotNull("Archive list layout is null!",
                rootView.findViewById(android.R.id.list));
        assertNotNull("Empty archive layout is null!",
                rootView.findViewById(android.R.id.empty));
    }

    @Test
    public void shouldCreateOptionsMenu() throws InterruptedException {
        // when
        createActivity();
        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);
        Menu menu = shadowActivity.getOptionsMenu();

        // then
        assertNotNull("Menu is not created !", menu);
        assertNotNull("Add menu item not found!", menu.findItem(R.id
                .blue_fragment_menu_add));
    }

    @Test
    public void shouldOpenCameraActivityOnMenuItemClickWithGrantedPermission() {
        // given
        mSharedPreferences = RuntimeEnvironment.application.getSharedPreferences(preferenceFile,
                Context.MODE_PRIVATE);
        mSharedPreferences.edit().putBoolean(syncMode, true).commit();
        mSharedPreferences.edit().putBoolean(demoKey, true).commit();

        // when
        createActivity();

        // Grant camera permission
        ShadowApplication application = shadowOf(mBlueMainActivity.getApplication());
        String[] permissions = new String[]{Manifest.permission.CAMERA};
        application.grantPermissions(permissions);

        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);
        shadowActivity.clickMenuItem(R.id.blue_fragment_menu_add);

        // then
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals("Should start Camera activity!", CameraActivity.class.getName(),
                startedIntent.getComponent().getClassName());
    }

    @Test
    public void shouldNotOpenCameraActivityOnMenuItemClickWithoutGrantedPermission() {
        // given
        mSharedPreferences = RuntimeEnvironment.application.getSharedPreferences(preferenceFile,
                Context.MODE_PRIVATE);
        mSharedPreferences.edit().putBoolean(syncMode, true).commit();
        mSharedPreferences.edit().putBoolean(demoKey, true).commit();

        // when
        createActivity();

        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);
        shadowActivity.clickMenuItem(R.id.blue_fragment_menu_add);

        // then
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNull("Should  not start Camera activity!", startedIntent);
    }

    @Test
    public void shouldShowEmptyDataView() {
        // when
        createActivity();

        // then
        View rootView = mArchiveFragment.getView();
        assertTrue("Empty archive layout should be visible!",
                rootView.findViewById(android.R.id.empty).getVisibility() == View.VISIBLE);
    }

    @Test
    public void shouldShowRefreshIndicatorOnMetadataUpdate() {
        // when
        createActivity();
        View rootView = mArchiveFragment.getView();
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id
                .blue_fragment_archive_swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(false);
        mArchiveFragment.onMetadataUpdate(new MetadataEvent(MetadataEvent.UpdateStatus
                .STARTED_UPDATE.getValue()));
        // then
        assertTrue("Update indicator should be visible!", swipeRefreshLayout.isRefreshing());
    }

    @Test
    public void shouldHideRefreshIndicatorOnMetadataUpdateError() {
        // when
        createActivity();
        View rootView = mArchiveFragment.getView();
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id
                .blue_fragment_archive_swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(true);
        mArchiveFragment.onMetadataUpdate(new MetadataEvent(MetadataEvent.UpdateStatus
                .UPDATE_ERROR.getValue(), -1));
        // then
        assertFalse("Update indicator should be hidden!", swipeRefreshLayout.isRefreshing());
    }

    @Test
    public void shouldHideRefreshIndicatorOnMetadataUpdateFinished() {
        // when
        createActivity();
        View rootView = mArchiveFragment.getView();
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id
                .blue_fragment_archive_swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(true);
        mArchiveFragment.onMetadataUpdate(new MetadataEvent(MetadataEvent.UpdateStatus
                .FINISHED_UPDATE.getValue()));
        // then
        assertFalse("Update indicator should be hidden!", swipeRefreshLayout.isRefreshing());
    }

    @Override
    public void tearDown() {
        cleanUpCurrentActivityIfNeeded();
        super.tearDown();
    }
}