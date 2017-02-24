package com.visma.blue.mamut;

import android.os.Bundle;

import com.visma.blue.InformationActivity;
import com.visma.blue.R;

public class MamutInformationActivity extends InformationActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showInformationMessage(getString(R.string.visma_blue_information_dialog_title),
                getString(R.string.visma_blue_information_dialog_message_mamut));
    }
}
