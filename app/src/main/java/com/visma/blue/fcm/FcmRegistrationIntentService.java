package com.visma.blue.fcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;

import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.Base;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.requests.RegisterDeviceRequest;

public class FcmRegistrationIntentService extends IntentService {

    private static final String TAG = "FcmRegIntentService";

    public FcmRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                String token = FirebaseInstanceId.getInstance().getToken();
                Log.d(TAG, "FCM token: " + token);
                sendRegistrationToServer(token);
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's FCM registration token with any server-side
     * account
     * maintained by your application.
     *
     * @param fcmToken The new token for the firebase cloud messaging.
     */
    private void sendRegistrationToServer(String fcmToken) {
        // Formulate the request and handle the response.
        RegisterDeviceRequest<Base> requestBlue = new RegisterDeviceRequest<Base>(
                this,
                VismaUtils.getToken(),
                fcmToken,
                Base.class,
                new Response.Listener<Base>() {
                    @Override
                    public void onResponse(final Base response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // We could wait and try to register again later on
                        Log.d("Blue", "Failed to register token at our server");
                    }
                });

        VolleySingleton.getInstance().addToRequestQueue(requestBlue);
    }
}
