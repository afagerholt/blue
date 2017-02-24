package com.visma.blue.login;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.visma.blue.R;
import com.visma.blue.login.integrations.IntegrationData;
import com.visma.blue.login.integrations.IntegrationsActivity;

public class LoginActivity extends BaseLoginActivity implements ChangeFragment {
    public static final String EXTRA_DATA_USERNAME = "EXTRA_DATA_USERNAME";

    public static final String ACTIVITY_RESULT_TOKEN = "ACTIVITY_RESULT_TOKEN";
    public static final String ACTIVITY_RESULT_COMPANY_NAME = "ACTIVITY_RESULT_COMPANY_NAME";
    public static final String ACTIVITY_RESULT_COMPANY_COUNTRY_ALPHA2 = "ACTIVITY_RESULT_COMPANY_COUNTRY_ALPHA2";
    public static final String ACTIVITY_RESULT_USER_ID = "ACTIVITY_RESULT_USER_ID";
    public static final String ACTIVITY_RESULT_COMPANY_ID = "ACTIVITY_RESULT_COMPANY_ID";
    public static final String ACTIVITY_RESULT_DEMO = "ACTIVITY_RESULT_DEMO";
    public static final String ACTIVITY_RESULT_SYNC = "ACTIVITY_RESULT_SYNC";
    public static final String ACTIVITY_RESULT_USERNAME = "ACTIVITY_RESULT_USERNAME";
    public static final String ACTIVITY_RESULT_APP_ID = "ACTIVITY_RESULT_APP_ID";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.blue_activity_login);
        setActionBar();
        String userName = "";
        IntegrationData integrationData = null;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(EXTRA_DATA_USERNAME)) {
            userName = bundle.getString(EXTRA_DATA_USERNAME);
        }

        if (bundle != null && bundle.containsKey(IntegrationsActivity.INTEGRATION_EXTRA)) {
            integrationData = bundle.getParcelable(IntegrationsActivity.INTEGRATION_EXTRA);
        }

        Bundle args = new Bundle();
        args.putString(LoginFragment2.USERNAME, userName);
        args.putParcelable(IntegrationsActivity.INTEGRATION_EXTRA, integrationData);
        LoginFragment2 loginFragment = new LoginFragment2();
        loginFragment.setArguments(args);
        changeFragmentWithoutBackStack(loginFragment);
    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void changeFragmentWithBackStack(Fragment fragment) {
        String tag = fragment.getClass().getName();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_from_right, R.animator.slide_out_to_left,
                        R.animator.slide_in_from_left, R.animator.slide_out_to_right)
                .replace(android.R.id.content, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    public void changeFragmentWithoutBackStack(Fragment fragment) {
        String tag = fragment.getClass().getName();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, tag)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
