package com.visma.blue.login.integrations;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.visma.blue.R;
import com.visma.blue.login.BaseLoginActivity;
import com.visma.blue.misc.VismaUtils;

public class IntegrationsActivity extends BaseLoginActivity {

    public static final String INTEGRATION_EXTRA = "selected_integration";

    private IntegrationData mIntegrationData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_activity_integrations);
        setActionBar();
        setIntegrationsList();
    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setIntegrationsList() {
        ListView integrationsList = (ListView) findViewById(R.id.blue_integrations_list);
        if (integrationsList != null) {
            integrationsList.setAdapter(new IntegrationsAdapter(this, VismaUtils
                    .getIntegrations(this)));
            integrationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    IntegrationData integrationData = (IntegrationData) parent
                            .getItemAtPosition(position);
                    if (integrationData != null) {
                        mIntegrationData = integrationData;
                        onIntegrationSelected();
                    }
                }
            });
        }

    }

    private void onIntegrationSelected() {
        Intent intent = new Intent();
        intent.putExtra(IntegrationsActivity.INTEGRATION_EXTRA, mIntegrationData);
        setResult(FragmentActivity.RESULT_OK, intent);
        finish();
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
