package com.visma.blue.network.requests.settings;

import android.content.Context;

import com.android.volley.Response;

import com.visma.blue.network.BaseRequest;

public class GetSettingsRequest<T> extends BaseRequest<T> {
    private static final String mRequest = "employeeSettings";

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GetSettingsRequest(Context context,
                              String token,
                              Class<T> clazz,
                              Response.Listener<T> listener,
                              Response.ErrorListener errorListener) {
        super(context,
                token,
                Method.GET,
                getCompleteUrl(mRequest),
                clazz,
                listener,
                errorListener);
    }
}