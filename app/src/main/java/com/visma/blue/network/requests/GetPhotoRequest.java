package com.visma.blue.network.requests;

import android.content.Context;

import com.android.volley.Response;

import com.visma.blue.network.BaseRequest;

public class GetPhotoRequest<T> extends BaseRequest<T> {
    private static final String mRequest = "photosng";

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GetPhotoRequest(Context context,
                           String token,
                           String photoId,
                           Class<T> clazz,
                           Response.Listener<T> listener,
                           Response.ErrorListener errorListener) {
        super(context,
                token,
                Method.GET,
                getCompleteUrl(mRequest + "/" + photoId),
                clazz,
                listener,
                errorListener);
    }
}