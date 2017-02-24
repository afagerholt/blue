package com.visma.blue.custom;

import android.app.Application;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.flurry.android.FlurryAgent;
import com.visma.blue.BlueConfig;
import com.visma.blue.BuildConfig;
import com.visma.blue.R;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.GetCompaniesAnswer;

public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        BlueConfig.readAppIdFromSettings(this);

        // configure and init Flurry
        if (BuildConfig.DEBUG) {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .withLogLevel(Log.VERBOSE)
                    .withCaptureUncaughtExceptions(false)
                    .build(this, getResources().getString(R.string.com_visma_app_flurry_id_test));
            FlurryAgent.setLogEvents(true);
        }

        VolleySingleton.init(this);
        VismaUtils.init(this);
    }
}
