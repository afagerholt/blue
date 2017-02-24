package com.visma.blue.network.requests;

import android.content.Context;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import com.visma.blue.network.BaseRequest;

import java.util.Map;

public class GetCompaniesRequest<T> extends BaseRequest<T> {
    private static final String mRequest = "customersng";

    private final String mUser;
    private final String mPassword;
    private final int mAppId;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GetCompaniesRequest(Context context,
                               String username,
                               String password,
                               int appId,
                               Class<T> clazz,
                               Response.Listener<T> listener,
                               Response.ErrorListener errorListener) {
        super(context,
                null,
                Method.GET,
                getCompleteUrl(mRequest),
                clazz,
                listener,
                errorListener);

        this.mUser = username;
        this.mPassword = password;
        this.mAppId = appId;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        headers.put("VoUser", Base64.encodeToString(mUser.getBytes(), Base64.NO_WRAP));
        headers.put("VoPass", Base64.encodeToString(mPassword.getBytes(), Base64.NO_WRAP));
        headers.put("AppId",  Integer.toString(mAppId));
        return headers;
    }
}