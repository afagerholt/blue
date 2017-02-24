package com.visma.blue.accountview;

import android.os.Bundle;

import com.visma.blue.InformationActivity;
import com.visma.blue.R;

public class AccountViewInformationActivity extends InformationActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showInformationMessage(getString(R.string.visma_blue_information_dialog_title),
                getString(R.string.visma_blue_information_dialog_message_accountview));
    }
}
