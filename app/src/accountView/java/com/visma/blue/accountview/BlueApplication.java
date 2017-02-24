package com.visma.blue.accountview;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.visma.blue.BlueConfig;
import com.visma.blue.BuildConfig;
import com.visma.blue.R;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.VolleySingleton;

import java.util.Locale;

public class BlueApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        BlueConfig.setAppId(getResources().getInteger(R.integer.com_visma_photoservice_app_id));

        // configure and init Flurry
        if (BuildConfig.DEBUG) {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .withLogLevel(Log.VERBOSE)
                    .withCaptureUncaughtExceptions(false)
                    .build(this, getResources().getString(R.string.com_visma_app_flurry_id_test));
        } else {
            new FlurryAgent.Builder()
                    .withLogEnabled(false)
                    .withCaptureUncaughtExceptions(false)
                    .build(this, getResources().getString(R.string.com_visma_app_flurry_id_live));
        }

        FlurryAgent.setLogEvents(true);

        VolleySingleton.init(this);
        VismaUtils.init(this);
    }

}