package com.visma.blue.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class TestUtils {

    public static void runThroughLongLifecycle(Instrumentation instrumentation, Activity activity) {
        instrumentation.callActivityOnPause(activity);
        instrumentation.callActivityOnStop(activity);
        instrumentation.callActivityOnRestart(activity);
        instrumentation.callActivityOnStart(activity);
        instrumentation.callActivityOnResume(activity);
    }

    public static void runThroughShortLifecycle(Instrumentation instrumentation, Activity activity) {
        instrumentation.callActivityOnPause(activity);
        instrumentation.callActivityOnResume(activity);
    }

    public static View waitForView(Activity activity, int viewId, int timeout) {
        long endTime = System.currentTimeMillis() + timeout;

        View view = null;
        while (view == null && System.currentTimeMillis() < endTime) {
            view = activity.findViewById(viewId);
        }

        return view;
    }

    public static Fragment waitForFragmentToAppear(AppCompatActivity activity, Class className, int timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        String tag = className.getName(); // This is the tag that is supposed to be used in our code

        Fragment fragment = null;
        while (fragment == null && System.currentTimeMillis() < endTime) {
            fragment = fragmentManager.findFragmentByTag(tag);
        }

        return fragment;
    }

    public static Fragment waitForFragmentToDisappear(AppCompatActivity activity, Class className, int timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        String tag = className.getName(); // This is the tag that is supposed to be used in our code

        Fragment fragment = null;
        do {
            fragment = fragmentManager.findFragmentByTag(tag);
        }
        while (fragment != null && System.currentTimeMillis() < endTime);

        return fragment;
    }
}
