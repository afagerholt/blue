package com.visma.blue.network;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.visma.blue.network.requests.RegisterDeviceRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.io.UnsupportedEncodingException;
import java.util.Map;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class RegisterDeviceWsTest extends BaseWsTest {

    private RegisterDeviceRequest<Base> getRegisterDeviceRequest(String token, String deviceToken) {
        return new RegisterDeviceRequest<>(
                RuntimeEnvironment.application,
                token,
                deviceToken,
                Base.class,
                null,
                null);
    }

    @Test
    public void shouldHaveRequiredHeaders() throws AuthFailureError {
        RegisterDeviceRequest<Base> request = getRegisterDeviceRequest(TOKEN, DEVICE_TOKEN);
        assertTrue("Register device request must have token header!",
                request.getHeaders().containsKey("VoToken"));
    }

    @Test
    public void shouldHaveRequiredHeaderCorrectValues() throws AuthFailureError {
        RegisterDeviceRequest<Base> request = getRegisterDeviceRequest(TOKEN, DEVICE_TOKEN);
        Map<String, String> headers = request.getHeaders();

        assertEquals("Set wrong token header value!",
                Base64.encodeToString(TOKEN.getBytes(), Base64.NO_WRAP),
                headers.get("VoToken"));
    }

    @Test
    public void shouldCreateRequestBodyWithDeviceInfo() throws AuthFailureError, JSONException,
            UnsupportedEncodingException {
        RegisterDeviceRequest<Base> request = getRegisterDeviceRequest(TOKEN, DEVICE_TOKEN);
        JSONObject bodyJSON = new JSONObject(new String(request.getBody(), "UTF-8"));
        assertNotNull("Request body should have device information!",
                bodyJSON.getJSONObject("registerDeviceRequest"));
        assertEquals("Request body contains wrong device token!",
                DEVICE_TOKEN,
                bodyJSON.getJSONObject("registerDeviceRequest").getString("DeviceToken"));
    }

    @Test
    public void shouldCreatePostRequest() throws AuthFailureError {
        RegisterDeviceRequest<Base> request = getRegisterDeviceRequest(TOKEN, DEVICE_TOKEN);
        assertEquals("Request method should be POST!",
                Request.Method.POST,
                request.getMethod());
    }
}