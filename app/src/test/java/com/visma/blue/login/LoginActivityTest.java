package com.visma.blue.login;

import android.app.Fragment;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;

import com.visma.blue.BaseActivityTest;
import com.visma.blue.R;
import com.visma.blue.custom.CustomLoginData;
import com.visma.blue.login.integrations.IntegrationData;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

public class LoginActivityTest extends BaseActivityTest {

    private ActivityController<LoginActivity> mLoginActivityController;
    private LoginActivity mLoginActivity;
    private Fragment mLoginFragment;


    private void cleanUpCurrentActivityIfNeeded() {
        if (mLoginActivityController != null) {
            mLoginActivityController.pause().stop().destroy();

            mLoginFragment = null;
            mLoginActivityController = null;
            mLoginActivity = null;
        }
    }

    private void createActivity(Intent intent) {
        cleanUpCurrentActivityIfNeeded();

        mLoginActivityController = Robolectric.buildActivity(LoginActivity.class)
                .withIntent(intent);

        mLoginActivity = mLoginActivityController
                .create()
                .start()
                .resume()
                .visible()
                .get();

        mLoginFragment = mLoginActivity.getFragmentManager().findFragmentById(android.R.id.content);
    }

    @Test
    public void shouldCreateLoginActivity() {
        createActivity(CustomLoginData.getDefaultIntent());
        assertNotNull("Login activity should be created!", mLoginActivity);
    }

    @Test
    public void shouldCreateLoginFragment() {
        createActivity(CustomLoginData.getDefaultIntent());
        assertNotNull("Login fragment should be created!", mLoginFragment);
    }

    @Test
    public void shouldCreateLoginFragmentWithEmptyUserName() {
        createActivity(CustomLoginData.getDefaultIntent());
        EditText userNameView = (EditText) mLoginFragment.getView()
                .findViewById(R.id.blue_fragment_login_user);
        assertEquals("User name should not be set!", "", userNameView.getText().toString());
    }

    @Test
    public void shouldCreateLoginFragmentWithSetUserName() {
        createActivity(CustomLoginData.getIntentWithUserName());
        EditText userNameView = (EditText) mLoginFragment.getView()
                .findViewById(R.id.blue_fragment_login_user);
        assertEquals("User name should not be set!", CustomLoginData.LOGIN_USER_NAME,
                userNameView.getText().toString());
    }

    @Test
    public void shouldCreateLoginFragmentWithSetIntegrationName() {
        IntegrationData testIntegration = CustomLoginData.getCustomIntegrationData();
        createActivity(CustomLoginData.getIntentWithoutUserName());

        assertEquals("Integration name should be set!", testIntegration.getName(),
                mLoginActivity.getTitle());
    }

    @Test
    public void shouldCreateLoginFragmentWithSetUserNameAndIntegrationName() {
        IntegrationData testIntegration = CustomLoginData.getCustomIntegrationData();
        createActivity(CustomLoginData.getIntentWithUserName());

        EditText userNameView = (EditText) mLoginFragment.getView()
                .findViewById(R.id.blue_fragment_login_user);
        assertEquals("User name should not be set!", CustomLoginData.LOGIN_USER_NAME,
                userNameView.getText().toString());
        assertEquals("Integration name should be set!", testIntegration.getName(),
                mLoginActivity.getTitle());
    }

    @Test
    public void shouldRecreateFragmentWhenLoginActivityRecreated() {
        createActivity(CustomLoginData.getIntentWithUserName());

        mLoginActivity.recreate();
        Fragment recreatedFragment = mLoginActivity.getFragmentManager().findFragmentById(android
                .R.id.content);

        assertNotNull("Recreated fragment should not be null!", recreatedFragment);
        assertNotSame("Recreated fragment should not be the same object!", mLoginFragment,
                recreatedFragment);
    }

    @Test
    public void shouldSendResultCodeCancelOnBackPress() {
        createActivity(CustomLoginData.getDefaultIntent());
        ShadowActivity shadowActivity = shadowOf(mLoginActivity);
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