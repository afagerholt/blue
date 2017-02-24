package com.visma.blue.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseLoginActivity extends AppCompatActivity {

    public static final String EXTRA_ON_BACK_PRESSED = "EXTRA_ON_BACK_PRESSED";

    private void setCancelResultOnBackPress() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_ON_BACK_PRESSED, true);
        Intent cancelIntent = new Intent();
        cancelIntent.putExtras(bundle);
        setResult(RESULT_CANCELED, cancelIntent);
    }

    @Override
    public void onBackPressed() {
        setCancelResultOnBackPress();
        super.onBackPressed();
    }
}
