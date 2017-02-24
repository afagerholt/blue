package com.visma.blue.network.requests;

import android.content.Context;

import com.android.volley.Response;

import com.visma.blue.network.BaseRequest;

public class GetServerListRequest<T> extends BaseRequest<T> {

    private static final String mRequest =
            "https://photoserviceproxy.internaltest.vismaonline.com/servers";

    public GetServerListRequest(Context context,
                                Class<T> clazz,
                                Response.Listener<T> listener,
                                Response.ErrorListener errorListener) {
        super(context,
                null,
                Method.GET,
                mRequest,
                clazz,
                listener,
                errorListener);
    }

}
