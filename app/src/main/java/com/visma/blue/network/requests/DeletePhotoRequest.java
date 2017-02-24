package com.visma.blue.network.requests;

import android.content.Context;

import com.android.volley.Response;

import com.visma.blue.network.BaseRequest;

public class DeletePhotoRequest<T> extends BaseRequest<T> {
    private static final String mRequest = "photosng/";

    public DeletePhotoRequest(Context context,
                              String token,
                              String photoId,
                              Class<T> clazz,
                              Response.Listener<T> listener,
                              Response.ErrorListener errorListener) {
        super(context,
                token,
                Method.DELETE,
                getCompleteUrl(mRequest + photoId),
                clazz,
                listener,
                errorListener);
    }
}