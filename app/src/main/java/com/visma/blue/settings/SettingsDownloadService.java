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
import com.visma.blue.events.CompanySettingsEvent;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.requests.settings.GetSettingsAnswer;
import com.visma.blue.network.requests.settings.GetSettingsRequest;
import com.visma.common.util.Util;

import org.greenrobot.eventbus.EventBus;

public class SettingsDownloadService extends IntentService {

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public SettingsDownloadService() {
        super(SettingsDownloadService.class.toString());
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

        // Formulate the request and handle the response.
        GetSettingsRequest<GetSettingsAnswer> request = new GetSettingsRequest<>(this,
                token,
                GetSettingsAnswer.class,
                new Response.Listener<GetSettingsAnswer>() {
                    @Override
                    public void onResponse(final GetSettingsAnswer response) {
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SettingsDownloadService.this);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(getString(R.string.preference_automatically_verify_emailed_documents), response.scanDirectlyToSystem);

                        //ListPreference always persists a string value even if we give it integer values as ids.
                        //Either do like http://kvance.livejournal.com/1039349.html
                        //or accept it
                        editor.putString(getString(R.string.preference_type_for_automatically_verified_documents),
                                Integer.toString(response.defaultDocumentType));
                        //editor.putString(getString(R.string.preference_type_for_automatically_verified_documents), "1");

                        editor.apply();

                        if (VismaUtils.isSupplierInvoiceApprovalEnabled(
                                SettingsDownloadService.this) != response
                                .usesSupplierInvoiceApproval) {
                            VismaUtils.setUsesSupplierInvoiceApproval(
                                    SettingsDownloadService.this, response
                                            .usesSupplierInvoiceApproval);
                            EventBus.getDefault().post(new CompanySettingsEvent(response
                                    .usesSupplierInvoiceApproval));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Blue", "Problem when downloading employee settings.");
                    }
                });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);
    }
}