package com.visma.blue;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import com.visma.blue.about.AboutActivity;
import com.visma.blue.archive.ArchiveFragment;
import com.visma.blue.background.MetadataUploadJob;
import com.visma.blue.fcm.FcmRegistrationIntentService;
import com.visma.blue.login.BaseLoginActivity;
import com.visma.blue.login.LoginActivity;
import com.visma.blue.login.LoginTutorialActivity;
import com.visma.blue.login.integrations.IntegrationData;
import com.visma.blue.login.integrations.IntegrationsActivity;
import com.visma.blue.metadata.BaseMetadataFragment;
import com.visma.blue.metadata.MetadataActivity;
import com.visma.blue.misc.AppId;
import com.visma.blue.misc.FileManager;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.NetworkStateReceiver;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.LastSyncTimestamps;
import com.visma.blue.provider.TempBitmaps;
import com.visma.blue.settings.SettingsActivity;
import com.visma.blue.settings.SettingsDownloadService;

import org.opencv.android.OpenCVLoader;

import java.util.Date;

public class BlueMainActivity extends AppCompatActivity implements NetworkStateReceiver
        .NetworkStateReceiverListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 100;

    // Activities
    private static final int ACTIVITY_REQUEST_LOGIN = 1;
    private static final int ACTIVITY_REQUEST_LOGIN_TUTORIAL = 2;
    private static final int ACTIVITY_REQUEST_INTEGRATIONS = 3;

    private long mLastSync;

    private NetworkStateReceiver networkStateReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileManager.deleteFilesFromDownloads(BlueMainActivity.this);
        handleNewIntent(getIntent());
        setContentView(R.layout.blue_activity_main);

        ArchiveFragment archiveFragment = new ArchiveFragment();
        changeFragmentWithoutBackStack(archiveFragment);

        updateTitle();
        registerNetworkStateListener();

        if (!VismaUtils.hasPdfFileSharing()) {
            disablePdfFileSharing();
        }

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final long now = System.currentTimeMillis();
        final long syncAge = now - mLastSync;
        if (syncAge > (DateUtils.MINUTE_IN_MILLIS * 15)) {
            registerDevice();
            downloadSettings();

            mLastSync = now; // This means we save the time regardless of if we succeed or not
        }
    }

    private void downloadSettings() {
        // Download an initial set of settings
        Intent intent = new Intent(this, SettingsDownloadService.class);
        startService(intent);
    }

    private void registerDevice() {
        if (TextUtils.isEmpty(VismaUtils.getToken())) {
            return;
        }

        switch (BlueConfig.getAppType()) {
            case UNKNOWN:
            case VISMA_ONLINE:
            case EACCOUNTING:
                if (checkPlayServices()) {
                    // Start IntentService to register this application with GCM.
                    Intent intent = new Intent(this, FcmRegistrationIntentService.class);
                    startService(intent);
                }
                break;
            case MAMUT:
            case EXPENSE_MANAGER:
            case ACCOUNTVIEW:
            case NETVISOR:
            case SEVERA:
                break;
            default:
                throw new UnsupportedOperationException("Not implemented.");
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("BlueMainActivity", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void changeFragmentWithoutBackStack(Fragment fragment) {
        String tag = fragment.getClass().getName();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, tag)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.blue_activity_main, menu);

        boolean hideEmailSetting = VismaUtils.isSyncMode(this);
        boolean hideInboundEmailSettings = false;

        switch (BlueConfig.getAppType()) {
            case UNKNOWN:
            case VISMA_ONLINE:
            case EACCOUNTING:
                hideInboundEmailSettings = false;
                break;
            case MAMUT:
            case EXPENSE_MANAGER:
            case ACCOUNTVIEW:
            case NETVISOR:
            case SEVERA:
                hideInboundEmailSettings = true;
                break;
            default:
                throw new UnsupportedOperationException("Not implemented.");
        }

        menu.findItem(R.id.menu_settings).setVisible(!(hideEmailSetting
                && hideInboundEmailSettings));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_about) {
            startAboutActivity();
            return true;
        } else if (itemId == R.id.menu_settings) {
            startSettingsActivity();
            return true;
        } else if (itemId == R.id.menu_logout) {
            Logger.logAction(Logger.ACTION_LOGOUT);
            startLoginTutorialActivity();
            resetTemporarySavedMetaData();
            disablePdfFileSharing();
            closeSearchView();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateTitle() {
        /*
        int serviceNameStringId = VismaUtils.getServiceName(VismaUtils.getAppId(this));
        getSupportActionBar().setTitle(serviceNameStringId);
        */
        getSupportActionBar().setTitle(R.string.visma_blue_main_activity_title);
    }

    private void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    protected void startLoginTutorialActivity() {
        VismaUtils.clearLoginData(this);

        Intent intent = new Intent(this, LoginTutorialActivity.class);
        startActivityForResult(intent, ACTIVITY_REQUEST_LOGIN_TUTORIAL);
    }

    protected void startLoginActivity(IntegrationData integrationData) {
        VismaUtils.clearLoginData(this);

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_DATA_USERNAME, VismaUtils.getCurrentEmail(this));
        intent.putExtra(IntegrationsActivity.INTEGRATION_EXTRA, integrationData);
        startActivityForResult(intent, ACTIVITY_REQUEST_LOGIN);
    }

    private void startIntegrationsActivity() {
        Intent intent = new Intent(this, IntegrationsActivity.class);
        startActivityForResult(intent, ACTIVITY_REQUEST_INTEGRATIONS);
    }

    protected IntegrationData getSelectedIntegration(Intent data) {
        if (data == null) {
            return null;
        }
        Bundle bundle = data.getExtras();
        return bundle.getParcelable(IntegrationsActivity.INTEGRATION_EXTRA);
    }

    protected void onReturnFromLoginTutorial(int resultCode, Intent data) {
        if (resultCode == FragmentActivity.RESULT_OK) {
            //the user pressed login button
            startIntegrationsActivity();
        } else {
            if (data != null && data.hasExtra(BaseLoginActivity.EXTRA_ON_BACK_PRESSED)) {
                //the user quit the login procedure
                finish();
            }
        }
    }

    private void onReturnFromIntegrations(int resultCode, Intent data) {
        if (resultCode == FragmentActivity.RESULT_OK) {
            //Start the login activity after selected integration
            startLoginActivity(getSelectedIntegration(data));
        } else {
            if (data != null && data.hasExtra(BaseLoginActivity.EXTRA_ON_BACK_PRESSED)) {
                //the user quit integrations selection
                startLoginTutorialActivity();
            }
        }
    }

    protected void onReturnFromLogin(int resultCode, Intent data) {
        if (resultCode == FragmentActivity.RESULT_OK) {
            // The user successfully logged in
            onLogin(data);
            handleImageOrPdfSharing(getIntent());
        } else {
            if (data != null && data.hasExtra(BaseLoginActivity.EXTRA_ON_BACK_PRESSED)) {
                //the user quit the login procedure
                startIntegrationsActivity();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_REQUEST_LOGIN) {
            onReturnFromLogin(resultCode, data);
        } else if (requestCode == ACTIVITY_REQUEST_LOGIN_TUTORIAL) {
            onReturnFromLoginTutorial(resultCode, data);
        } else if (requestCode == ACTIVITY_REQUEST_INTEGRATIONS) {
            onReturnFromIntegrations(resultCode, data);
        } else if ((requestCode == ArchiveFragment
                .ACTIVITY_REQUEST_METADATA || decodeRequestcode(requestCode) == ArchiveFragment
                .ACTIVITY_REQUEST_METADATA) && resultCode == FragmentActivity.RESULT_OK) {
            reloadMetadata();
            checkForUploadError(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onLogin(Intent data) {
        Bundle bundle = data.getExtras();
        String userName = bundle.getString(LoginActivity.ACTIVITY_RESULT_USERNAME);
        if (userName == null) {
            userName = "";
        }

        String companyName = bundle.getString(LoginActivity.ACTIVITY_RESULT_COMPANY_NAME);
        if (companyName == null) {
            companyName = "";
        }

        String companyCountryCodeAlpha2 = bundle.getString(LoginActivity
                .ACTIVITY_RESULT_COMPANY_COUNTRY_ALPHA2);
        if (companyCountryCodeAlpha2 == null) {
            companyCountryCodeAlpha2 = "";
        }

        String userId = bundle.getString(LoginActivity.ACTIVITY_RESULT_USER_ID);
        if (userId == null) {
            userId = "";
        }

        String companyId = bundle.getString(LoginActivity.ACTIVITY_RESULT_COMPANY_ID);
        if (companyId == null) {
            companyId = "";
        }

        String token = bundle.getString(LoginActivity.ACTIVITY_RESULT_TOKEN);
        if (token == null) {
            token = "";
        }

        final boolean useSync = bundle.getBoolean(LoginActivity.ACTIVITY_RESULT_SYNC, false);
        final boolean isDemo = bundle.getBoolean(LoginActivity.ACTIVITY_RESULT_DEMO, false);

        // Let the demo user run as Visma Online
        int appId = bundle.getInt(LoginActivity.ACTIVITY_RESULT_APP_ID, AppId.VISMA_ONLINE
                .getValue());
        BlueConfig.setAndSaveAppIdToSettings(this, appId);

        VismaUtils.setCurrentEmail(this, userName);
        VismaUtils.setToken(this, token);
        VismaUtils.setCurrentCompany(this, companyName);
        VismaUtils.setCurrentCompanyCountryCodeAlpha2(this, companyCountryCodeAlpha2);
        VismaUtils.setCurrentUserId(this, userId);
        if (BlueConfig.getAppType() != AppId.SEVERA) {
            // Severa only gives us a temporary id here, the real guid is available later when we
            // verify the token
            VismaUtils.setCurrentCompanyId(this, companyId);
        } else {
            // Need to reset Severa custom data because we don't have company id for severa. This is
            // temporally fix. Should be removed when company id is returned from backend.
            resetSeveraCustomData();
        }

        VismaUtils.setDemoMode(this, isDemo);
        VismaUtils.setSyncMode(this, useSync);

        this.invalidateOptionsMenu();

        // Trigger the registration of the new user
        registerDevice();

        downloadSettings();
        reloadMetadata();
        updateTitle();
        enablePdfFileSharing();
    }

    private void resetSeveraCustomData() {
        getContentResolver().delete(BlueContentProvider.CONTENT_URI_SEVERA_CASES, null, null);
        getContentResolver().delete(BlueContentProvider.CONTENT_URI_SEVERA_PRODUCTS, null, null);
        getContentResolver().delete(BlueContentProvider.CONTENT_URI_SEVERA_TAXES, null, null);

        String selection = LastSyncTimestamps.TYPE + " =?";
        String[] arguments = new String[]{"" + LastSyncTimestamps.Type.SEVERA_CUSTOM_DATA};
        getContentResolver().delete(BlueContentProvider.CONTENT_URI_LAST_SYNC_TIMESTAMPS, selection,
                arguments);

    }

    private void resetTemporarySavedMetaData() {
        getContentResolver().delete(BlueContentProvider.CONTENT_URI_METADATA_LIST_TEMP, null, null);
    }

    private void handleImageOrPdfSharing(Intent intent) {
        String type = intent.getType();
        if (type == null) {
            return;
        }

        if (intent.getType().contains("image/")) {
            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                Bitmap resizedBitmap = VismaUtils.getCompressedBitmapFromUri(imageUri,
                        this);
                if (resizedBitmap != null) {
                    getContentResolver().update(BlueContentProvider
                            .CONTENT_URI_METADATA_TEMP_BITMAP, TempBitmaps
                            .getTempBitmapValues(resizedBitmap), null, null);
                    openMetadataActivity();
                }
            }
            clearIntentType(intent);
        } else if (intent.getType().contains("application/")) {
            Uri pdfUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (pdfUri != null) {
                if (pdfUri.toString().toLowerCase().contains(".pdf")) {
                    openMetadataActivity(pdfUri, null);
                } else {
                    openMetadataActivity(pdfUri, intent.getExtras().getString(Intent
                            .EXTRA_SUBJECT));
                }

            }
        }
    }

    private void openMetadataActivity() {
        Intent intent = new Intent(this, MetadataActivity.class);

        OnlineMetaData metaData = new OnlineMetaData(true, "", new Date(), "", "", VismaUtils
                .getLastSelectedTypeOrDefault(this));
        intent.putExtra(BaseMetadataFragment.EXTRA_DATA_METADATA, metaData);
        intent.putExtra(BaseMetadataFragment.EXTRA_DATA_IMAGE_IS_SENT, false);
        intent.putExtra(BaseMetadataFragment.EXTRA_DATA_USE_TEMP_BITMAP, true);
        startActivityForResult(intent, ArchiveFragment.ACTIVITY_REQUEST_METADATA);
    }

    private void openMetadataActivity(Uri pdfFileUri, String fileName) {
        if (checkIfHasWritePermission()) {
            final String filePath = FileManager.getFilePathFromUri(pdfFileUri, fileName, this);
            if (filePath == null) {
                return;
            }

            final Intent intent = new Intent(this, MetadataActivity.class);
            OnlineMetaData metaData = new OnlineMetaData(true, "", new Date(), "", "", VismaUtils
                    .getLastSelectedTypeOrDefault(this));
            metaData.isVerified = true;
            metaData.originalFilename = fileName == null ? pdfFileUri.getLastPathSegment() :
                    fileName;
            metaData.contentType = "application/pdf";

            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_METADATA, metaData);
            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_IMAGE_IS_SENT, false);
            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_USE_TEMP_BITMAP, false);
            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_LOCAL_PDF, filePath);
            startActivityForResult(intent, ArchiveFragment.ACTIVITY_REQUEST_METADATA);
            clearIntentType(getIntent());
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNewIntent(intent);
    }

    private void handleNewIntent(Intent intent) {
        if (VismaUtils.isLoginNeeded(this)) {
            startLoginTutorialActivity();
        } else {
            handleImageOrPdfSharing(intent);
        }
    }

    private void reloadMetadata() {
        ArchiveFragment archiveFragment = (ArchiveFragment) getFragmentByTag(ArchiveFragment
                .class.getName());
        // Had a case where this was null, not sure why though
        if (archiveFragment != null) {
            archiveFragment.reLoadMetadata();
        }
    }

    private int decodeRequestcode(int changedRequestCode) {
        int fragmentIndex = changedRequestCode >> 16;
        return changedRequestCode - (fragmentIndex << 16);
    }

    private void clearIntentType(Intent intent) {
        if (intent != null) {
            intent.setType(null);
        }
    }

    private void registerNetworkStateListener() {
        if (networkStateReceiver == null) {
            networkStateReceiver = new NetworkStateReceiver();
            networkStateReceiver.addListener(this);
        }

        this.registerReceiver(networkStateReceiver,
                new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unRegisterNetworkStateListener() {
        if (networkStateReceiver != null) {
            networkStateReceiver.removeListener(this);
            unregisterReceiver(networkStateReceiver);
        }
    }

    @Override
    public void networkAvailable() {
        MetadataUploadJob.schedulePhotoUploadJob(MetadataUploadJob.INSTANT_PHOTO_UPLOAD_TIMEOUT);
    }

    @Override
    protected void onDestroy() {
        unRegisterNetworkStateListener();
        super.onDestroy();
    }

    private Fragment getFragmentByTag(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    private void closeSearchView() {
        ArchiveFragment archiveFragment = (ArchiveFragment) getFragmentByTag(ArchiveFragment
                .class.getName());
        if (archiveFragment != null) {
            archiveFragment.collapseSearchView();
        }
    }

    private void checkForUploadError(Intent data) {
        ArchiveFragment archiveFragment = (ArchiveFragment) getFragmentByTag(ArchiveFragment
                .class.getName());
        if (archiveFragment != null) {
            archiveFragment.checkForUploadError(data);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean checkIfHasWritePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest
                .permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        }

        return true;
    }

    private void showSnacBar(int messageId) {
        ArchiveFragment archiveFragment = (ArchiveFragment) getFragmentByTag(ArchiveFragment
                .class.getName());
        if (archiveFragment != null) {
            archiveFragment.showSnackBar(messageId);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            // Request for camera permission.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleImageOrPdfSharing(getIntent());
            } else {
                showSnacBar(R.string.visma_blue_error_missing_permission);
            }
        }
    }

    private void disablePdfFileSharing() {
        String packageName = getPackageName();
        ComponentName pdfFileShareComponent = new ComponentName(getPackageName(),
                packageName + ".PdfFileComponent");

        PackageManager manager = getPackageManager();
        manager.setComponentEnabledSetting(pdfFileShareComponent, PackageManager
                .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private void enablePdfFileSharing() {
        if (VismaUtils.hasPdfFileSharing()) {
            String packageName = getPackageName();
            ComponentName pdfFileShareComponent = new ComponentName(getPackageName(),
                    packageName + ".PdfFileComponent");

            PackageManager manager = getPackageManager();
            manager.setComponentEnabledSetting(pdfFileShareComponent, PackageManager
                    .COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        }
    }
}

