package com.visma.blue.network.requests;

import android.content.Context;
import android.support.v4.util.Pair;

import com.android.volley.Response;

import com.visma.blue.network.BaseRequest;

public class GetMetadataListRequest<T> extends BaseRequest<T> {
    private static final String mRequest = "metadatang";

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GetMetadataListRequest(Context context,
                                  String token,
                                  Class<T> clazz,
                                  Response.Listener<T> listener,
                                  Response.ErrorListener errorListener) {
        super(context,
                token,
                Method.GET,
                getCompleteUrl(mRequest,
                        Pair.create("pageNumber", Integer.toString(0)),
                        Pair.create("pageSize", Integer.toString(100000))
                ),
                clazz,
                listener,
                errorListener);
    }
}