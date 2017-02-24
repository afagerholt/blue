package com.visma.blue.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.misc.AppId;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.OnlinePhotoType;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        boolean hideEmailSetting = VismaUtils.isSyncMode(getActivity());
        if (hideEmailSetting) {
            PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().findPreference("DefaultEmailStuff");
            getPreferenceScreen().removePreference(category);
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Hide the complete inbound email settings if in demo mode
        if (shouldHideInboundSettings()) {
            PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().findPreference("InboundEmailStuff");
            getPreferenceScreen().removePreference(category);
            return;
        }

        int selectedType = Integer.parseInt(settings.getString(getString(R.string.preference_type_for_automatically_verified_documents), "-1"));

        // Hide the type selection field if it is an invalid type
        if (!isValidType(selectedType)) {
            PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().findPreference("InboundEmailStuff");
            Preference typePreference = category.findPreference(getString(R.string.preference_type_for_automatically_verified_documents));
            category.removePreference(typePreference);
        }

        // Update the type text manually if it is an unknown type
        if (selectedType == OnlinePhotoType.UNKNOWN.getValue()) {
            Preference typePreference = getPreferenceScreen().findPreference(
                    getString(R.string.preference_type_for_automatically_verified_documents));
            typePreference.setSummary(R.string.visma_blue_document_type_unknown);
        }

        String inboundEmailAddress = settings.getString(getString(R.string.preference_inbound_email_address), "");
        if (!TextUtils.isEmpty(inboundEmailAddress)) {
            Preference inboundEmailPreference = getPreferenceScreen().findPreference(
                    getString(R.string.preference_inbound_email_address));
            inboundEmailPreference.setSummary(inboundEmailAddress);
        }
    }

    private static boolean isValidType(int value) {
        return value == 0 || value == 1 || value == -1;
    }

    private boolean shouldHideInboundSettings() {
        return VismaUtils.isDemoMode(getActivity()) || (BlueConfig.getAppType()
                != AppId.VISMA_ONLINE && BlueConfig.getAppType()
                != AppId.EACCOUNTING);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final String scanDirectlyKey = getString(R.string.preference_automatically_verify_emailed_documents);
        final String typeKey = getString(R.string.preference_type_for_automatically_verified_documents);
        final String inboundEmailKey = getString(R.string.preference_inbound_email_address);

        boolean scanDirectlyToSystem = sharedPreferences.getBoolean(getString(R.string.preference_automatically_verify_emailed_documents), false);
        int defaultDocumentType =
                Integer.parseInt(sharedPreferences.getString(
                        getString(R.string.preference_type_for_automatically_verified_documents),
                        "-1"));

        if (key.equals(scanDirectlyKey) && scanDirectlyToSystem && defaultDocumentType == OnlinePhotoType.UNKNOWN.getValue()) {
            ListPreference typePreference = (ListPreference) getPreferenceScreen().findPreference(
                    getString(R.string.preference_type_for_automatically_verified_documents));
            typePreference.setSummary(R.string.visma_blue_preference_default_set_value_summary);
            typePreference.setValueIndex(OnlinePhotoType.INVOICE.getValue());
        }

        if (key.equals(scanDirectlyKey) || key.equals(typeKey)) {
            Activity activity = getActivity();
            Intent intent = new Intent(activity, SettingsUploadService.class);
            activity.startService(intent);
        }

        if (key.equals(inboundEmailKey)) {
            Preference preference = getPreferenceScreen().findPreference(
                    getString(R.string.preference_inbound_email_address));            ;
            preference.setSummary(sharedPreferences.getString(inboundEmailKey, null));
        }
    }
}
