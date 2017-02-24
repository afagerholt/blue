package com.visma.blue.settings;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.visma.blue.R;
import com.visma.blue.misc.ErrorMessage;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.BlueNetworkError;
import com.visma.blue.network.OnlineResponseCodes;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.GetEmailAnswer;
import com.visma.blue.network.requests.GetEmailRequest;
import com.visma.common.VismaAlertDialog;

public class InboundEmailSettingDialog extends DialogPreference {

    private String mEmail;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InboundEmailSettingDialog(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialize();
    }

    public InboundEmailSettingDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();
    }

    public InboundEmailSettingDialog(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InboundEmailSettingDialog(Context context) {
        super(context);

        initialize();
    }

    private void initialize() {
        setTitle(R.string.visma_blue_email_dialog_title);
        setDialogMessage(getContext().getResources().getString(R.string.visma_blue_inbound_email_explanation));
        setNegativeButtonText(android.R.string.cancel);
        setPositiveButtonText(android.R.string.copy);
    }

    private void updateDialogMessage() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                final String message = String.format("%s\n\n%s",
                        getContext().getResources().getString(R.string.visma_blue_inbound_email_explanation), mEmail);
                messageView.setText(message);
            }
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        if (TextUtils.isEmpty(mEmail)) {
            downloadInboundEmailAddress();
        } else {
            updateDialogMessage();
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mEmail = this.getPersistedString(null);
        } else {
            // Set default state from the XML attribute
            mEmail = (String) defaultValue;
            persistString(mEmail);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            ClipboardManager manager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(mEmail, mEmail);
            manager.setPrimaryClip(clip);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent,
            // use superclass state
            return superState;
        }

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current
        // setting value
        myState.mEmail = mEmail;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        mEmail = myState.mEmail;
        //mNumberPicker.setValue(myState.value);
    }

    private void downloadInboundEmailAddress() {
        final String finalToken = VismaUtils.getToken();

        GetEmailRequest<GetEmailAnswer> request = new GetEmailRequest<GetEmailAnswer>(getContext(),
                finalToken,
                GetEmailAnswer.class,
                new Response.Listener<GetEmailAnswer>() {
                    @Override
                    public void onResponse(final GetEmailAnswer response) {
                        mEmail = response.inboundEmail;
                        persistString(mEmail);

                        updateDialogMessage();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int errorMessageId;
                        if (error instanceof BlueNetworkError) {
                            BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                            errorMessageId = ErrorMessage.getErrorMessage(blueNetworkError.blueError, false);
                        } else {
                            errorMessageId = ErrorMessage.getErrorMessage(OnlineResponseCodes.NotSet, false);
                        }

                        Context context = getContext();
                        if (context != null) {
                            VismaAlertDialog alertDialog = new VismaAlertDialog(context);
                            alertDialog.showError(errorMessageId);
                            final Dialog dialog = getDialog();
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    }
                });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);
        Logger.logAction(Logger.ACTION_GET_INBOUND_EMAIL);
    }

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        String mEmail;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            mEmail = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeString(mEmail);
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
