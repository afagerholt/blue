package com.visma.blue.metadata;

import android.content.ContentProvider;
import android.content.pm.ProviderInfo;
import android.support.v4.app.Fragment;

import com.visma.blue.BlueConfig;
import com.visma.blue.BuildConfig;
import com.visma.blue.custom.CustomApplication;
import com.visma.blue.custom.CustomMetaData;
import com.visma.blue.metadata.accountview.AccountViewMetadataFragment;
import com.visma.blue.metadata.eaccounting.EAcountingMetadataFragment;
import com.visma.blue.metadata.expense.ExpenseMetadataFragment;
import com.visma.blue.metadata.mamut.MamutMetadataFragment;
import com.visma.blue.metadata.netvisor.NetvisorMetadaFragment;
import com.visma.blue.metadata.severa.SeveraMetadataFragment;
import com.visma.blue.metadata.vismaonline.VismaOnlineMetadataFragment;
import com.visma.blue.misc.AppId;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.provider.BlueContentProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.util.ActivityController;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = CustomApplication.class)
public class MetadaActivityTest {

    // ActivityController is a Robolectric class that drives the Activity lifecycle
    private ActivityController<MetadataActivity> controller;
    private MetadataActivity mMetaDataActivity;
    private OnlineMetaData mOnlineMetaData;

    @Before
    public void setUp() {
        shadowBlueContentProvider();
        controller = Robolectric.buildActivity(MetadataActivity.class);
        mOnlineMetaData = CustomMetaData.getDefaultMetaData(RuntimeEnvironment.application);
    }

    // Need to shadow BlueContentProvider for Expense integration.
    private void shadowBlueContentProvider() {
        final ContentProvider contentProvider = new BlueContentProvider();
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = BlueContentProvider.AUTHORITY;
        contentProvider.attachInfo(RuntimeEnvironment.application, providerInfo);
        contentProvider.onCreate();
        ShadowContentResolver.registerProvider(BlueContentProvider.AUTHORITY, contentProvider);
    }

    private void checkShownIntegration(int appType, String expectedFragmentClass) {
        BlueConfig.setAppId(appType);
        mMetaDataActivity = controller.withIntent(CustomMetaData.getDefaultIntent(mOnlineMetaData))
                .create()
                .start()
                .resume()
                .get();
        assertNotNull("Failed creating MetadataActivity for " + BlueConfig.getAppType().getName()
                + " integration!", mMetaDataActivity);
        Fragment fragment = mMetaDataActivity.getSupportFragmentManager().findFragmentById
                (android.R.id.content);
        assertNotNull("Failed creating " + BlueConfig.getAppType().getName() + " integration meta" +
                " data fragment!", fragment);
        assertEquals("Created wrong integration fragment", expectedFragmentClass, fragment
                .getClass().getName());
    }

    @Test
    public void createActivityWithoutExtras() {
        mMetaDataActivity = controller.create().start().resume().visible().get();
        assertNotNull("MetadataActivity creation failed", mMetaDataActivity);
    }

    @Test
    public void createActivityWithExtras() {
        mMetaDataActivity = controller.withIntent(CustomMetaData.getDefaultIntent
                (mOnlineMetaData)).create().start().resume().visible().get();
        assertNotNull("MetadataActivity with extra data creation failed", mMetaDataActivity);
    }

    @Test
    public void checkUnknownIntegrationFragmentCreation() {
        checkShownIntegration(AppId.UNKNOWN.getValue(), MetadataFragment.class.getName());
    }

    @Test
    public void checkVismaOnlineIntegrationFragmentCreation() {
        checkShownIntegration(AppId.VISMA_ONLINE.getValue(), VismaOnlineMetadataFragment.class
                .getName());
    }

    @Test
    public void checkEaccountingIntegrationFragmentCreation() {
        checkShownIntegration(AppId.EACCOUNTING.getValue(), EAcountingMetadataFragment.class.getName());
    }

    @Test
    public void checkMamutIntegrationFragmentCreation() {
        checkShownIntegration(AppId.MAMUT.getValue(), MamutMetadataFragment.class.getName());
    }

    @Test
    public void checkAccountViewIntegrationFragmentCreation() {
        checkShownIntegration(AppId.ACCOUNTVIEW.getValue(), AccountViewMetadataFragment.class
                .getName());
    }

    @Test
    public void checkNetvisorViewIntegrationFragmentCreation() {
        checkShownIntegration(AppId.NETVISOR.getValue(), NetvisorMetadaFragment.class.getName());
    }

    @Test
    public void checkExpenseIntegrationFragmentCreation() {
        checkShownIntegration(AppId.EXPENSE_MANAGER.getValue(), ExpenseMetadataFragment.class
                .getName());
    }

    @Test
    public void checkSeveraIntegrationFragmentCreation() {
        checkShownIntegration(AppId.SEVERA.getValue(), SeveraMetadataFragment.class.getName());
    }

    @After
    public void tearDown() {
        // Destroy activity after every test
        controller.pause().stop().destroy();
    }
}
