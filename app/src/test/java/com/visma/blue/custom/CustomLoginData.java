package com.visma.blue.custom;

import android.content.Intent;

import com.visma.blue.login.LoginActivity;
import com.visma.blue.login.integrations.IntegrationData;
import com.visma.blue.login.integrations.IntegrationsActivity;

public class CustomLoginData {

    public static String LOGIN_USER_NAME = "testas@visma.com";

    public static Intent getDefaultIntent() {
        return new Intent(Intent.ACTION_VIEW);
    }

    public static Intent getIntentWithoutUserName() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(LoginActivity.EXTRA_DATA_USERNAME, LOGIN_USER_NAME);
        intent.putExtra(IntegrationsActivity.INTEGRATION_EXTRA, getCustomIntegrationData());
        return intent;
    }

    public static Intent getIntentWithUserName() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(LoginActivity.EXTRA_DATA_USERNAME, LOGIN_USER_NAME);
        intent.putExtra(IntegrationsActivity.INTEGRATION_EXTRA, getCustomIntegrationData());
        return intent;
    }

    public static IntegrationData getCustomIntegrationData() {
        return new IntegrationData("Test Visma", "blablabla", 0, -1000);
    }

}
