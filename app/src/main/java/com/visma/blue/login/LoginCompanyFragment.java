package com.visma.blue.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.misc.ErrorMessage;
import com.visma.blue.misc.Logger;
import com.visma.blue.network.BlueNetworkError;
import com.visma.blue.network.OnlineResponseCodes;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.GetCompaniesAnswer;
import com.visma.blue.network.containers.GetTokenAnswer;
import com.visma.blue.network.requests.GetTokenRequest;
import com.visma.common.VismaAlertDialog;
import com.visma.common.VismaAlertDialog.AnimationEndingListener;

import java.util.ArrayList;

public class LoginCompanyFragment extends Fragment {
    public static final String ONLINE_CUSTOMERS = "ONLINE_CUSTOMERS";
    public static final String USER = "USER";
    public static final String PASSWORD = "PASSWORD";

    private ChangeFragment mChangeFragmentCallback;

    private ArrayList<GetCompaniesAnswer.OnlineCustomer> onlineCustomers;
    private String mUserName;
    private String mPassword;
    private String mToken;
    private String mUserId;
    private int mAppId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.blue_fragment_company, container, false);

        Logger.logPageView(Logger.VIEW_LOGIN_COMPANY_LIST);

        Bundle bundle = null;
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            bundle = savedInstanceState;
        } else if (getArguments() != null) {
            bundle = getArguments();
        }

        if (bundle != null) {
            this.onlineCustomers = bundle.getParcelableArrayList(ONLINE_CUSTOMERS);
            this.mUserName = bundle.getString(USER);
            this.mPassword = bundle.getString(PASSWORD);
            this.mAppId = bundle.getInt(LoginFragment2.APP_ID);
            this.mToken = bundle.getString(LoginFragment2.TOKEN);
            this.mUserId = bundle.getString(LoginFragment2.USER_ID);

        }

        setTitle(getString(R.string.visma_blue_login_choose_company));

        ListView listViewCompanies = (ListView) view.findViewById(R.id.listViewCompanies);
        listViewCompanies.setAdapter(new CompanyAdapter(getActivity(), onlineCustomers));
        listViewCompanies.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GetCompaniesAnswer.OnlineCustomer company =
                        (GetCompaniesAnswer.OnlineCustomer) parent.getAdapter().getItem(position);
                if (!TextUtils.isEmpty(mToken)) {
                    finishAndSendResult(company);
                } else {
                    VismaAlertDialog alert = new VismaAlertDialog(getActivity());
                    alert.showProgressBar();
                    login(company, alert);
                }
            }
        });

        return view;
    }

    private void setTitle(String title) {
        getActivity().setTitle(title);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ONLINE_CUSTOMERS, onlineCustomers);
        outState.putString(USER, mUserName);
        outState.putString(PASSWORD, mPassword);
        outState.putString(LoginFragment2.TOKEN, mToken);
        outState.putString(LoginFragment2.USER_ID, mUserId);
        outState.putInt(LoginFragment2.APP_ID, mAppId);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mChangeFragmentCallback = (ChangeFragment) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ChangeFragment interface.");
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mChangeFragmentCallback = (ChangeFragment) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ChangeFragment interface.");
        }
    }

    private void login(final GetCompaniesAnswer.OnlineCustomer company,
                       final VismaAlertDialog alert) {
        // Formulate the request and handle the response.
        GetTokenRequest<GetTokenAnswer> request = new GetTokenRequest<GetTokenAnswer>(getActivity(),
                mUserName,
                mPassword,
                company.companyId,
                GetTokenAnswer.class,
                new Response.Listener<GetTokenAnswer>() {
                    @Override
                    public void onResponse(final GetTokenAnswer response) {
                        alert.closeDialog(new AnimationEndingListener() {
                            public void onAnimationEnding() {
                                if (getActivity() == null || getActivity().isFinishing()) {
                                    return;
                                }

                                mToken =  response.token;
                                finishAndSendResult(company);
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // If we end up here we have one error that can be shown for all services
                        // so lets display it.
                        final Activity activity = getActivity();
                        if (activity == null || activity.isFinishing()) {
                            return;
                        }

                        int errorMessageId;
                        if (error instanceof BlueNetworkError) {
                            BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                            errorMessageId = ErrorMessage.getErrorMessage(blueNetworkError.blueError, false);
                        } else {
                            errorMessageId = ErrorMessage.getErrorMessage(OnlineResponseCodes.NotSet, false);
                        }

                        alert.showError(errorMessageId);
                    }
                });

        // Add the request to the RequestQueue.
        BlueConfig.setAndSaveAppIdToSettings(getActivity(), mAppId);
        VolleySingleton
                .getInstance()
                .addToRequestQueue(request);
    }

    private void finishAndSendResult(GetCompaniesAnswer.OnlineCustomer company) {
        Intent intent = new Intent();
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_USERNAME, mUserName);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_USER_ID, mUserId);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_COMPANY_NAME,  company.companyDisplayName);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_COMPANY_ID, company.companyId);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_COMPANY_COUNTRY_ALPHA2, company.countryCodeAlpha2);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_TOKEN, mToken);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_SYNC, true);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_APP_ID, mAppId);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_DEMO, false);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }
}
