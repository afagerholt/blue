package com.visma.blue.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.View;

import com.visma.blue.BaseActivityTest;
import com.visma.blue.BlueConfig;
import com.visma.blue.BlueMainActivity;
import com.visma.blue.R;
import com.visma.blue.about.AboutActivity;
import com.visma.blue.login.LoginTutorialActivity;
import com.visma.blue.misc.AppId;
import com.visma.blue.settings.SettingsActivity;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;

import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

public class BlueMainActivityTest extends BaseActivityTest {

    private static final String preferenceFile = "MobileScanner";
    private static final String tokenKey = "Token";
    private static final String demoKey = "Demo";
    private static final String syncMode = "SyncMode";

    private ActivityController<BlueMainActivity> mBlueActivityController;
    private BlueMainActivity mBlueMainActivity;
    private SharedPreferences mSharedPreferences;

    private void cleanUpCurrentActivityIfNeeded() {
        if (mBlueActivityController != null) {
            mBlueActivityController.pause().stop().destroy();

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
    }

    private boolean getMenuItemVisibility(int appId, int menuId){
        BlueConfig.setAppId(appId);
        mSharedPreferences.edit().putBoolean(syncMode, true).commit();
        mSharedPreferences.edit().putBoolean(demoKey, true).commit();

        // when
        createActivity();
        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);
        Menu menu = shadowActivity.getOptionsMenu();
        return menu.findItem(menuId).isVisible();
    }

    @Before
    public void setup() {
        Locale.setDefault(Locale.US);
        mSharedPreferences = RuntimeEnvironment.application.getSharedPreferences(preferenceFile,
                Context.MODE_PRIVATE);
    }

    @Test
    public void shouldCreateBlueMainActivity() {
        // when
        createActivity();

        // then
        assertNotNull("Main activity should be created!", mBlueMainActivity);
    }

    @Test
    public void shouldStartTutorialWhenSyncModeFalse() {
        // given
        mSharedPreferences.edit().putBoolean(syncMode, false).commit();

        // when
        createActivity();
        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);

        // then
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals("Should start tutorial activity when sync mode is false!", startedIntent
                .getComponent().getClassName(), LoginTutorialActivity.class.getName());
    }

    @Test
    public void shouldNotStartTutorialActivityInDemoMode() {
        // given
        mSharedPreferences.edit().putBoolean(syncMode, true).commit();
        mSharedPreferences.edit().putBoolean(demoKey, true).commit();

        // when
        createActivity();
        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);

        // then
        assertNull("Should not start tutorial activity in demo mode!", shadowActivity
                .getNextStartedActivity());
    }

    @Test
    public void shouldNotStartTutorialWhenTokenIsNotEmpty() {
        // given
        mSharedPreferences.edit().putBoolean(syncMode, true).commit();
        mSharedPreferences.edit().putBoolean(demoKey, true).commit();
        mSharedPreferences.edit().putString(tokenKey, "TEST_TOKEN").commit();

        // when
        createActivity();
        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);

        // then
        assertNull("Should not start tutorial activity when token is not empty!", shadowActivity
                .getNextStartedActivity());
    }

    @Test
    public void shouldStartTutorialWhenTokenIsEmpty() {
        // given
        mSharedPreferences.edit().putBoolean(syncMode, false).commit();
        mSharedPreferences.edit().putBoolean(demoKey, false).commit();
        mSharedPreferences.edit().putString(tokenKey, "").commit();

        // when
        createActivity();
        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);

        // then
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals("Should start tutorial activity when token is empty!", startedIntent
                .getComponent().getClassName(), LoginTutorialActivity.class.getName());
    }

    @Test
    public void shouldStartTutorialWhenAppIdUnknown() {
        // given
        BlueConfig.setAppId(AppId.UNKNOWN.getValue());
        mSharedPreferences.edit().putBoolean(syncMode, false).commit();
        mSharedPreferences.edit().putBoolean(demoKey, false).commit();
        mSharedPreferences.edit().putString(tokenKey, "TEST_TOKEN").commit();

        // when
        createActivity();
        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);

        // then
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals("Should start tutorial activity when App Id Unknown!", startedIntent
                .getComponent().getClassName(), LoginTutorialActivity.class.getName());
    }

    @Test
    public void shouldCreateOptionsMenu() throws InterruptedException {
        // when
        createActivity();
        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);
        Menu menu = shadowActivity.getOptionsMenu();

        // then
        assertNotNull("Menu is not created !", menu);
        assertNotNull("About menu item not found!", menu.findItem(R.id
                .menu_about));
        assertNotNull("Settings menu item not found!", menu.findItem(R.id
                .menu_settings));
        assertNotNull("Logout menu item not found!", menu.findItem(R.id
                .menu_logout));
    }

    @Test
    public void shouldOpenAboutActivityOnMenuItemClick() {
        // given
        // Prevents starting LoginTutorialActivity on BlueMainActivityCreation
        mSharedPreferences.edit().putBoolean(syncMode, true).commit();
        mSharedPreferences.edit().putBoolean(demoKey, true).commit();

        // when
        createActivity();
        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);
        shadowActivity.clickMenuItem(R.id.menu_about);

        // then
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals("Should start About activity!", AboutActivity.class.getName(),
                startedIntent.getComponent().getClassName());
    }

    @Test
    public void shouldOpenSettingsActivityOnMenuItemClick() {
        // given
        // Prevents starting LoginTutorialActivity on BlueMainActivityCreation
        mSharedPreferences.edit().putBoolean(syncMode, true).commit();
        mSharedPreferences.edit().putBoolean(demoKey, true).commit();

        // when
        createActivity();
        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);
        shadowActivity.clickMenuItem(R.id.menu_settings);

        // then
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals("Should start About activity!", SettingsActivity.class.getName(),
                startedIntent.getComponent().getClassName());
    }

    @Test
    public void shouldOpenLoginTutorialActivityOnMenuItemClick() {
        // given
        // Prevents starting LoginTutorialActivity on BlueMainActivityCreation
        mSharedPreferences.edit().putBoolean(syncMode, true).commit();
        mSharedPreferences.edit().putBoolean(demoKey, true).commit();

        // when
        createActivity();
        ShadowActivity shadowActivity = shadowOf(mBlueMainActivity);
        shadowActivity.clickMenuItem(R.id.menu_logout);

        // then
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals("Should start About activity!", LoginTutorialActivity.class.getName(),
                startedIntent.getComponent().getClassName());
    }

    @Test
    public void shouldHideSettingsMenuWhenAccountviewSelected() {
        // given
        boolean isMenuItemVisible = getMenuItemVisibility(AppId.ACCOUNTVIEW.getValue(),
                R.id.menu_settings);

        //then
        assertFalse("Settings menu item should be hidden!", isMenuItemVisible);
    }

    @Test
    public void shouldHideSettingsMenuWhenMamutSelected() {
        // given
        boolean isMenuItemVisible = getMenuItemVisibility(AppId.MAMUT.getValue(),
                R.id.menu_settings);

        //then
        assertFalse("Settings menu item should be hidden!", isMenuItemVisible);
    }

    @Test
    public void shouldHideSettingsMenuWhenExpenseSelected() {
        // given
        boolean isMenuItemVisible = getMenuItemVisibility(AppId.EXPENSE_MANAGER.getValue(),
                R.id.menu_settings);

        //then
        assertFalse("Settings menu item should be hidden!", isMenuItemVisible);
    }

    @Test
    public void shouldHideSettingsMenuWhenNetvisorSelected() {
        // given
        boolean isMenuItemVisible = getMenuItemVisibility(AppId.NETVISOR.getValue(),
                R.id.menu_settings);

        //then
        assertFalse("Settings menu item should be hidden!", isMenuItemVisible);
    }

    @Test
    public void shouldHideSettingsMenuWheSeveraSelected() {
        // given
        boolean isMenuItemVisible = getMenuItemVisibility(AppId.SEVERA.getValue(),
                R.id.menu_settings);

        //then
        assertFalse("Settings menu item should be hidden!", isMenuItemVisible);
    }

    @Test
    public void shouldShowSettingsMenuWheVismaOnlineSelected() {
        // given
        boolean isMenuItemVisible = getMenuItemVisibility(AppId.VISMA_ONLINE.getValue(),
                R.id.menu_settings);

        //then
        assertTrue("Settings menu item should be visible!", isMenuItemVisible);
    }

    @Test
    public void shouldShowSettingsMenuWheEAccountingSelected() {
        // given
        boolean isMenuItemVisible = getMenuItemVisibility(AppId.EACCOUNTING.getValue(),
                R.id.menu_settings);

        //then
        assertTrue("Settings menu item should be visible!", isMenuItemVisible);
    }

    @Test
    public void shouldShowSettingsMenuWhenUnknownIntegrationSelected() {
        // given
        boolean isMenuItemVisible = getMenuItemVisibility(AppId.UNKNOWN.getValue(),
                R.id.menu_settings);

        //then
        assertTrue("Settings menu item should be visible!", isMenuItemVisible);
    }

    @Override
    public void tearDown() {
        cleanUpCurrentActivityIfNeeded();
        super.tearDown();
    }
}