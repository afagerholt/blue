package com.visma.blue.network.requests.settings;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.google.gson.Gson;

import com.visma.blue.network.BaseRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class PutSettingsRequest<T> extends BaseRequest<T> {
    private static final String mRequest = "employeeSettings";
    private Settings mSettings;

    public PutSettingsRequest(Context context,
                              String token,
                              Settings settings,
                              Class<T> clazz,
                              Response.Listener<T> listener,
                              Response.ErrorListener errorListener) {
        super(context,
                token,
                Method.PUT,
                getCompleteUrl(mRequest),
                clazz,
                listener,
                errorListener);

        mSettings = settings;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        JSONObject body = new JSONObject();
        try {
            Gson gson = new Gson();
            body.put("settings", new JSONObject(gson.toJson(mSettings)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return body.toString().getBytes();
    }

    @Override
    public String getBodyContentType() {
        return "application/json";
    }
}