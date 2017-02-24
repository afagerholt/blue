package com.visma.blue.expense;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.visma.blue.BlueMainActivity;
import com.visma.blue.R;
import com.visma.blue.login.integrations.IntegrationData;
import com.visma.blue.login.integrations.IntegrationsActivity;
import com.visma.blue.misc.AppId;

public class BlueExpenseMainActivity extends BlueMainActivity {

    private IntegrationData getExpenseIntegrationData() {
        return new IntegrationData(getString(R.string.visma_blue_integration_expense_manager_title),
                getString(R.string.visma_blue_integration_expense_manager_description),
                R.drawable.blue_expense_icon, AppId.EXPENSE_MANAGER.getValue());
    }

    private Intent getExpenseDataIntent() {
        Intent data = new Intent();
        data.putExtra(IntegrationsActivity.INTEGRATION_EXTRA, getExpenseIntegrationData());
        return data;
    }

    @Override
    protected void onReturnFromLoginTutorial(int resultCode, Intent data) {
        if (resultCode == FragmentActivity.RESULT_OK) {
            //the user pressed login button
            startLoginActivity(getSelectedIntegration(getExpenseDataIntent()));
        } else {
            //the user quit the login procedure
            super.onReturnFromLoginTutorial(resultCode, data);
        }
    }

    @Override
    protected void onReturnFromLogin(int resultCode, Intent data) {
        if (resultCode == FragmentActivity.RESULT_OK) {
            //the user successfully logged in
            super.onReturnFromLogin(resultCode, data);
        } else {
            //the user quit the login procedure
            startLoginTutorialActivity();
        }
    }
}

