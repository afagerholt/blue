package com.visma.blue.network.requests;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import com.visma.blue.network.BaseRequest;
import com.visma.blue.network.containers.OnlineMetaData;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdatePhotoRequest<T> extends BaseRequest<T> {
    private static final String mRequest = "photosng/";
    private OnlineMetaData mOnlineMetaData;

    public UpdatePhotoRequest(Context context,
                              String token,
                              OnlineMetaData onlineMetaData,
                              Class<T> clazz,
                              Response.Listener<T> listener,
                              Response.ErrorListener errorListener) {
        super(context,
                token,
                Method.PUT,
                getCompleteUrl(mRequest + onlineMetaData.photoId),
                clazz,
                listener,
                errorListener);

        this.mOnlineMetaData = onlineMetaData;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        JSONObject jsonRequest = new JSONObject();
        try {
            // The server specifies BodyStyle =
            // WebMessageBodyStyle.WrappedRequest
            // and UpdatePhoto(string token, string id, UpdatePhotoRequest
            // request);, i.e., parameter named request
            jsonRequest.put("request", mOnlineMetaData.getUpdateJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonRequest.toString().getBytes();
    }

    @Override
    public String getBodyContentType() {
        return "application/json";
    }
}