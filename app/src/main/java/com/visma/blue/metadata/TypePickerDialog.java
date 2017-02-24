package com.visma.blue.metadata;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.visma.blue.R;
import com.visma.blue.network.OnlinePhotoType;

public class TypePickerDialog extends DialogFragment {
    public static final String EXTRA_TYPE = "EXTRA_TYPE";

    public TypePickerDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = savedInstanceState;
        if (bundle == null || bundle.isEmpty()) {
            bundle = getArguments();
        }
        if (bundle == null || bundle.isEmpty()) {
            bundle = null;
        }

        final int currentType = bundle.getInt(EXTRA_TYPE, -1);
        int checkedItem = -1; // -1 means no item is checked
        if (currentType == OnlinePhotoType.INVOICE.getValue()) {
            checkedItem = 0;
        } else if (currentType == OnlinePhotoType.RECEIPT.getValue()) {
            checkedItem = 1;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.visma_blue_typepicker_title);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setSingleChoiceItems(R.array.types, checkedItem,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position of the selected item
                        // The index is also the same as the defined value of the type
                        Intent intent = getActivity().getIntent();
                        intent.putExtra(EXTRA_TYPE, which);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }
}