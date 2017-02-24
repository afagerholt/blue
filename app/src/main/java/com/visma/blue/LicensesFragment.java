package com.visma.blue;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.visma.blue.misc.Logger;

public class LicensesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.blue_fragment_licenses, container, false);

        Logger.logPageView(Logger.VIEW_THIRD_PARTY_LICENSES);

        return view;
    }
}
