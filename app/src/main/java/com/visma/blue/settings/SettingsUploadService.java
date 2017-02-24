package com.visma.blue.settings;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.visma.blue.R;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.Base;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.requests.settings.PutSettingsRequest;
import com.visma.blue.network.requests.settings.Settings;
import com.visma.common.util.Util;

public class SettingsUploadService extends IntentService {

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public SettingsUploadService() {
        super(SettingsUploadService.class.toString());
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns,
     * IntentService stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (!Util.isConnectedOrConnecting(this)) {
            return;
        }

        final String token = VismaUtils.getToken();
        if (TextUtils.isEmpty(token)) {
            return;
        }

        Settings settingsObject = new Settings();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SettingsUploadService.this);
        settingsObject.scanDirectlyToSystem = settings.getBoolean(getString(R.string.preference_automatically_verify_emailed_documents), false);
        settingsObject.defaultDocumentType = Integer.parseInt(
                settings.getString(getString(R.string.preference_type_for_automatically_verified_documents), "-1"));

        // Formulate the request and handle the response.
        PutSettingsRequest<Base> request = new PutSettingsRequest<>(this,
                token,
                settingsObject,
                Base.class,
                new Response.Listener<Base>() {
                    @Override
                    public void onResponse(final Base response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Blue", "Problem when uploading employee settings.");
                    }
                });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);
    }
}