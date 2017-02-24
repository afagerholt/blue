package com.visma.blue.misc;

import android.support.annotation.StringDef;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.flurry.android.FlurryAgent;
import com.visma.blue.BlueConfig;

import io.fabric.sdk.android.Fabric;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Logger {

    @StringDef({
            ACTION_SEARCH,
            ACTION_FETCH_METADATA_LIST,
            ACTION_CAMERA,
            ACTION_CROP,
            ACTION_QR,
            ACTION_GALLERY,
            ACTION_EMAIL,
            ACTION_CREATE,
            ACTION_UPDATE,
            ACTION_DELETE,
            ACTION_FETCH_DYNAMIC_DATA_LIST,
            ACTION_GET_INBOUND_EMAIL,
            ACTION_LOGOUT,
            ACTION_TRY_THE_APP,

    })
    //Tell the compiler not to store annotation data in the .class file
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionEvent {
    }

    @StringDef({
            VIEW_ARCHIVE,
            VIEW_CAMERA,
            VIEW_CROP,
            VIEW_METADATA,
            VIEW_QR,
            VIEW_DOCUMENT,
            VIEW_ABOUT,
            VIEW_WHATS_NEW,
            VIEW_SETTINGS,
            VIEW_THIRD_PARTY_LICENSES,
            VIEW_LOGIN,
            VIEW_LOGIN_COMPANY_LIST,
            VIEW_INTEGRATION_CHOICE})
    //Tell the compiler not to store annotation data in the .class file
    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewEvent {
    }

    public static final String VIEW_ARCHIVE = "View archive";
    public static final String ACTION_SEARCH = "Action search";
    public static final String ACTION_FETCH_METADATA_LIST = "Action fetch metadata list";
    public static final String VIEW_CAMERA = "View camera";
    public static final String ACTION_CAMERA = "Action camera";
    public static final String VIEW_CROP = "View crop";
    public static final String ACTION_CROP = "Action crop";
    public static final String ACTION_QR = "Action qr";
    public static final String ACTION_GALLERY = "Action gallery";
    public static final String VIEW_METADATA = "View metadata";
    public static final String VIEW_QR = "View qr";
    public static final String ACTION_EMAIL = "Action email";
    public static final String ACTION_CREATE = "Action create";
    public static final String ACTION_UPDATE = "Action update";
    public static final String ACTION_DELETE = "Action delete";
    public static final String ACTION_FETCH_DYNAMIC_DATA_LIST = "Action fetch dynamic data list";
    public static final String VIEW_DOCUMENT = "View document";
    public static final String VIEW_ABOUT = "View about";
    public static final String VIEW_WHATS_NEW = "View what’s new";
    public static final String VIEW_SETTINGS = "View settings";
    public static final String ACTION_GET_INBOUND_EMAIL = "Action get inbound email";
    public static final String VIEW_THIRD_PARTY_LICENSES = "View third party licenses";
    public static final String ACTION_LOGOUT = "Action logout";
    public static final String VIEW_LOGIN = "View login";
    public static final String ACTION_TRY_THE_APP = "Action try the app";
    public static final String VIEW_LOGIN_COMPANY_LIST = "View login company list";
    public static final String VIEW_INTEGRATION_CHOICE = "View integration choice";

    @SuppressWarnings("NullArgumentToVariableArgMethod")
    public static void logPageView(@ViewEvent String event) {
        logPageView(event, (Pair<String, String>) null);
    }

    @SafeVarargs
    public static void logPageView(@ViewEvent String event, Pair<String, String>... params) {
        final String loggerBackendName = BlueConfig.getLoggerBackendName();

        ContentViewEvent contentViewEvent = new ContentViewEvent()
                .putContentName(event)
                .putCustomAttribute("Service", loggerBackendName);

        if (params != null && params.length != 0 && params[0] != null) {
            ArrayMap<String, String> flurryParameters = new ArrayMap<>(params.length + 1);
            flurryParameters.put("Service", loggerBackendName);

            for (Pair<String, String> param : params) {
                contentViewEvent.putCustomAttribute(param.first, param.second);
                flurryParameters.put(param.first, param.second);
            }

            FlurryAgent.logEvent(event, flurryParameters);
        } else {
            FlurryAgent.logEvent(event);
        }

        // Don't log when running from, e.g., automatic tests
        if (Fabric.isInitialized()) {
            Answers.getInstance().logContentView(contentViewEvent);
        }
    }

    @SuppressWarnings("NullArgumentToVariableArgMethod")
    public static void logAction(@ActionEvent String event) {
        logAction(event, (Pair<String, String>) null);
    }

    @SafeVarargs
    public static void logAction(@ActionEvent String event, Pair<String, String>... params) {
        final String loggerBackendName = BlueConfig.getLoggerBackendName();

        CustomEvent customEvent = new CustomEvent(event)
                .putCustomAttribute("Service", loggerBackendName);

        if (params != null && params.length != 0 && params[0] != null) {
            ArrayMap<String, String> flurryParameters = new ArrayMap<>(params.length + 1);
            flurryParameters.put("Service", loggerBackendName);

            for (Pair<String, String> param : params) {
                customEvent.putCustomAttribute(param.first, param.second);
                flurryParameters.put(param.first, param.second);
            }

            FlurryAgent.logEvent(event, flurryParameters);
        } else {
            FlurryAgent.logEvent(event);
        }

        // Don't log when running from, e.g., automatic tests
        if (Fabric.isInitialized()) {
            Answers.getInstance().logCustom(customEvent);
        }
    }

    public static void logError(Throwable throwable) {
        Crashlytics.logException(throwable);
    }

    public static String getLoggerTypeName(int type) {
        switch (type) {
            case 0:
                return "Invoice";
            case 1:
                return "Receipt";
            case 2:
                return "Document";
            default:
                return "Unknown";
        }
    }
}
