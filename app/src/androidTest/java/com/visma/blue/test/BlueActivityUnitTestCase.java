package com.visma.blue.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.test.ActivityUnitTestCase;

import java.util.concurrent.atomic.AtomicReference;


/**
 * When using junit 4 it looks as if we need to run on the main thread when creating activities
 * @param <T>
 */
public class BlueActivityUnitTestCase<T extends Activity> extends ActivityUnitTestCase<T> {

    public BlueActivityUnitTestCase(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected T startActivity(final Intent intent, final Bundle savedInstanceState,
                              final Object lastNonConfigurationInstance) {
        return startActivityOnMainThread(intent, savedInstanceState, lastNonConfigurationInstance);
    }

    private T startActivityOnMainThread(final Intent intent, final Bundle savedInstanceState,
                                        final Object lastNonConfigurationInstance) {
        final AtomicReference<T> activityRef = new AtomicReference<>();
        final Runnable activityRunnable = new Runnable() {
            @Override
            public void run() {
                activityRef.set(BlueActivityUnitTestCase.super.startActivity(
                        intent, savedInstanceState, lastNonConfigurationInstance));
            }
        };

        if (Looper.myLooper() != Looper.getMainLooper()) {
            getInstrumentation().runOnMainSync(activityRunnable);
        } else {
            activityRunnable.run();
        }

        return activityRef.get();
    }
}
