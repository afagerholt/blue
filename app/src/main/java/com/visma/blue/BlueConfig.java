package com.visma.blue;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.visma.blue.misc.AppId;
import com.visma.blue.misc.VismaUtils;

public class BlueConfig {
    private static final String VISMA_BLUE_APP_ID = "VISMA_BLUE_APP_ID";

    private static AppId APP_ID = AppId.UNKNOWN;

    public static int getAppId() {
        return APP_ID.getValue();
    }

    public static AppId getAppType() {
        return APP_ID;
    }

    public static String getLoggerBackendName() {
        return APP_ID.getName();
    }

    public static void setAppId(int appId) {
        APP_ID = VismaUtils.getAppId(appId);
    }

    public static void readAppIdFromSettings(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        APP_ID = VismaUtils.getAppId(settings.getInt(VISMA_BLUE_APP_ID, AppId.UNKNOWN.getValue()));
    }

    public static void setAndSaveAppIdToSettings(Context context, int appId) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(VISMA_BLUE_APP_ID, appId);
        editor.apply();

        APP_ID = VismaUtils.getAppId(appId);
    }
}