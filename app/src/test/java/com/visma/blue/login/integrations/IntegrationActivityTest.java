package com.visma.blue.login.integrations;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.widget.ListView;

import com.visma.blue.BaseActivityTest;
import com.visma.blue.R;
import com.visma.blue.custom.CustomLoginData;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

public class IntegrationActivityTest extends BaseActivityTest {

    private ActivityController<IntegrationsActivity> mActivityController;
    private IntegrationsActivity mIntegrationsActivity;

    private void cleanUpCurrentActivityIfNeeded() {
        if (mActivityController != null) {
            mActivityController.pause().stop().destroy();

            mActivityController = null;
            mIntegrationsActivity = null;
        }
    }

    private void createActivity(Intent intent) {
        cleanUpCurrentActivityIfNeeded();

        mActivityController = Robolectric.buildActivity(IntegrationsActivity.class)
                .withIntent(intent);

        mIntegrationsActivity = mActivityController
                .create()
                .start()
                .resume()
                .visible()
                .get();
    }

    @Test
    public void shouldCreateIntegrationsActivity() {
        createActivity(CustomLoginData.getDefaultIntent());
        assertNotNull("Integrations activity should be created", mIntegrationsActivity);
    }

    @Test
    public void shouldSendResultCodeOk() {
        createActivity(CustomLoginData.getDefaultIntent());
        ShadowActivity shadowActivity = shadowOf(mIntegrationsActivity);
        ListView integrationsList = (ListView) mIntegrationsActivity
                .findViewById(R.id.blue_integrations_list);
        shadowOf(integrationsList).performItemClick(0);

        assertEquals("When pressed on integration should sent result code OK!", FragmentActivity
                .RESULT_OK, shadowActivity.getResultCode());
        assertTrue("Activity should be finishing", shadowActivity.isFinishing());
    }

    @Test
    public void shouldSendResultCodeCancelOnBackPress() {
        createActivity(CustomLoginData.getDefaultIntent());
        ShadowActivity shadowActivity = shadowOf(mIntegrationsActivity);
        shadowActivity.onBackPressed();
        assertEquals("When pressed on back button should sent result code cancel!", FragmentActivity
                .RESULT_CANCELED, shadowActivity.getResultCode());
        assertTrue("Activity should be finishing", shadowActivity.isFinishing());
    }

    @Override
    public void tearDown() {
        cleanUpCurrentActivityIfNeeded();
        super.tearDown();
    }
}
