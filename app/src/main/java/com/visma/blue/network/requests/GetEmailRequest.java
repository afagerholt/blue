package com.visma.blue.network.requests;

import android.content.Context;

import com.android.volley.Response;

import com.visma.blue.network.BaseRequest;

public class GetEmailRequest<T> extends BaseRequest<T> {
    private static final String mRequest = "inboundEmail";

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GetEmailRequest(Context context,
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