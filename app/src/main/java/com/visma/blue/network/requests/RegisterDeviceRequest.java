package com.visma.blue.network.requests;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import com.visma.blue.network.BaseRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterDeviceRequest<T> extends BaseRequest<T> {
    private static final String mRequest = "registerDevice";

    private final String mDeviceToken;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param clazz Relevant class object, for Gson's reflection
     */
    public RegisterDeviceRequest(Context context,
                                 String token,
                                 String deviceToken,
                                 Class<T> clazz,
                                 Response.Listener<T> listener,
                                 Response.ErrorListener errorListener) {
        super(context,
                token,
                Method.POST,
                getCompleteUrl(mRequest),
                clazz,
                listener,
                errorListener);

        this.mDeviceToken = deviceToken;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        JSONObject body = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("DeviceToken", mDeviceToken);
            object.put("Family", 1); // 1 == Android
            body.put("registerDeviceRequest", object);
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

