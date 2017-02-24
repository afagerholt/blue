package com.visma.blue;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public abstract class InformationActivity  extends AppCompatActivity {

    private static final String SCANNER_APP_PACKAGE_NAME = "com.visma.blue";
    private AlertDialog mInformationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_activity_information);
    }

    protected void showInformationMessage(String title, String message) {
        mInformationDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.visma_blue_information_dialog_go_to_google_play_button_title, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        openGooglePlay(SCANNER_APP_PACKAGE_NAME);
                        finish();
                    }
                })
                .show();
    }

    private void openGooglePlay(String packageName) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (android.content.ActivityNotFoundException exeption) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    private void dismissInformationDialog() {
        if (mInformationDialog != null) {
            mInformationDialog.dismiss();
        }
    }

    @Override
    public void finish() {
        dismissInformationDialog();
        super.finish();
    }
}
