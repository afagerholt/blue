package com.visma.blue.about;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.visma.blue.R;
import com.visma.blue.databinding.BlueFragmentAboutBinding;
import com.visma.blue.misc.ChangeFragment;
import com.visma.blue.misc.ErrorMessage;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.BlueNetworkError;
import com.visma.blue.network.OnlineResponseCodes;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.GetEmailAnswer;
import com.visma.blue.network.requests.GetEmailRequest;
import com.visma.common.VismaAlertDialog;
import com.visma.common.VismaAlertDialog.AnimationEndingListener;

public class AboutFragment extends Fragment {

    private ChangeFragment mChangeFragmentCallback;
    private AboutViewModel mAboutViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.logPageView(Logger.VIEW_ABOUT);

        BlueFragmentAboutBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.blue_fragment_about, container, false);
        mAboutViewModel =
                new AboutViewModel(inflater.getContext(), mChangeFragmentCallback, getFragmentManager());
        binding.setAboutViewModel(mAboutViewModel);

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mChangeFragmentCallback = (ChangeFragment) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement ChangeFragment interface.");
        }
    }
}