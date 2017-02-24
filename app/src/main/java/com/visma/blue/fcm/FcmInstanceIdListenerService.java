package com.visma.blue.fcm;

import com.google.firebase.iid.FirebaseInstanceIdService;

public class FcmInstanceIdListenerService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        // For now we don't do anything here as we register the token at every resume of the MainActivity in the
        // current solution

        /*
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(getClass().getSimpleName(), "Refreshed token: " + refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
        */

        /*
        Intent intent = new Intent(this, FcmRegistrationIntentService.class);
        startService(intent);
        */
    }
}
