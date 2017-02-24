package com.visma.blue;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class EmailPresentationDialog extends DialogFragment {

    public static final String EMAIL = "EMAIL";

    private String mEmail;

    public EmailPresentationDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mEmail = getArguments().getString(EMAIL);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.visma_blue_email_dialog_title);
        builder.setMessage(getResources().getString(R.string.visma_blue_inbound_email_explanation) + "\n\n" + mEmail);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        builder.setPositiveButton(android.R.string.copy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                ClipboardManager manager =
                        (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData clip = ClipData.newPlainText(mEmail, mEmail);
                manager.setPrimaryClip(clip);

                dismiss();
            }
        });

        return builder.create();
    }
}