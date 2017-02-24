package com.visma.blue.expense;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.evernote.android.job.JobManager;
import com.flurry.android.FlurryAgent;
import com.visma.blue.BlueConfig;
import com.visma.blue.BuildConfig;
import com.visma.blue.R;
import com.visma.blue.background.BackgroundJobCreator;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.VolleySingleton;

import io.fabric.sdk.android.Fabric;

public class BlueApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics(), new Answers())
                        //.debuggable(true)
                .build();
        Fabric.with(fabric);

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

        // Creates singleton for handling background tasks
        JobManager.create(this).addJobCreator(new BackgroundJobCreator());
    }
}