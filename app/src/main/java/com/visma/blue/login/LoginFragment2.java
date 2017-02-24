package com.visma.blue.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.login.chooser.ServerListActivity;
import com.visma.blue.login.integrations.IntegrationData;
import com.visma.blue.login.integrations.IntegrationsActivity;
import com.visma.blue.misc.ErrorMessage;
import com.visma.blue.misc.Logger;
import com.visma.blue.network.BlueNetworkError;
import com.visma.blue.network.OnlineResponseCodes;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.GetCompaniesAnswer;
import com.visma.blue.network.containers.GetTokenAnswer;
import com.visma.blue.network.requests.GetCompaniesRequest;
import com.visma.blue.network.requests.GetTokenRequest;
import com.visma.common.FiveClickDetector;
import com.visma.common.VismaAlertDialog;
import com.visma.common.VismaAlertDialog.AnimationEndingListener;
import com.visma.common.VismaLinearLayout;

public class LoginFragment2 extends Fragment {

    private static final String PASSWORD = "PASSWORD";
    protected static final String USERNAME = "USERNAME";
    protected static final String USER_ID = "USER_ID";
    protected static final String TOKEN = "TOKEN";
    protected static final String APP_ID = "APP_ID";

    private FiveClickDetector mFiveClickDetector;

    private ChangeFragment mChangeFragmentCallback;
    private String mUserName;
    private String mPassword;
    private IntegrationData mSelectedIntegrationData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        final View view = inflater.inflate(R.layout.blue_fragment_login2, container, false);

        Logger.logPageView(Logger.VIEW_LOGIN);

        Bundle bundle = null;
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            bundle = savedInstanceState;
        } else if (getArguments() != null) {
            bundle = getArguments();
        }

        if (bundle != null) {
            this.mUserName = bundle.getString(USERNAME);
            this.mPassword = bundle.getString(PASSWORD);
            mSelectedIntegrationData = bundle.getParcelable(IntegrationsActivity.INTEGRATION_EXTRA);
        }

        setUsername(view);
        setPassword(view);
        setSelectedIntegration();
        setLoginButton(view);

        /*
        // Demo button
        Button buttonDemo = (Button) view.findViewById(R.id.blue_fragment_login_button_demo);
        SpannableString spannableString = new SpannableString(buttonDemo.getText());
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
        buttonDemo.setText(spannableString);
        buttonDemo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Logger.logAction(Logger.ACTION_TRY_THE_APP);

                Intent intent = new Intent();
                intent.putExtra(LoginActivity.ACTIVITY_RESULT_DEMO, true);
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
        });
        */

        // We hide the demo button temporarily until we have a good system for handling custom data
        Button buttonDemo = (Button) view.findViewById(R.id.blue_fragment_login_button_demo);
        buttonDemo.setVisibility(View.GONE);

        setupHideShowBasedOnKeyboard(view);
        setupChangeServerView(view);

        return view;
    }

    private void setUsername(View rootView) {
        EditText editTextUser = (EditText) rootView.findViewById(R.id.blue_fragment_login_user);
        editTextUser.setText(mUserName);
    }

    private void setPassword(View rootView) {
        EditText editTextPassword = (EditText) rootView.findViewById(R.id
                .blue_fragment_login_password);
        editTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Button buttonLogin = (Button) v.getRootView().findViewById(R.id
                            .blue_fragment_login_button_login);
                    buttonLogin.performClick();
                }
                return false;
            }
        });
    }

    private void setSelectedIntegration() {
        if (mSelectedIntegrationData != null) {
            getActivity().setTitle(mSelectedIntegrationData.getName());
        }
    }

    private void setLoginButton(View rootView) {
        Button buttonLogin = (Button) rootView.findViewById(R.id.blue_fragment_login_button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                View rootView = v.getRootView();
                EditText editTextUser = (EditText) rootView.findViewById(R.id
                        .blue_fragment_login_user);
                EditText editTextPassword = (EditText) rootView.findViewById(R.id
                        .blue_fragment_login_password);

                if (editTextUser != null) {
                    mUserName = editTextUser.getText().toString();
                }

                if (editTextPassword != null) {
                    mPassword = editTextPassword.getText().toString();
                }

                if (editTextUser.length() == 0 || editTextPassword.length() == 0) {
                    VismaAlertDialog alert = new VismaAlertDialog(getActivity());
                    if (editTextUser.length() == 0) {
                        alert.showError(R.string.visma_blue_error_empty_username);
                    } else {
                        alert.showError(R.string.visma_blue_error_empty_password);
                    }

                    return;
                }

                InputMethodManager inputManager = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager
                        .HIDE_NOT_ALWAYS);

                final VismaAlertDialog alert = new VismaAlertDialog(getActivity());
                alert.showProgressBar();

                // Formulate the request and handle the response.
                GetCompaniesRequest<GetCompaniesAnswer> request = new
                        GetCompaniesRequest<GetCompaniesAnswer>(getActivity(),
                        mUserName,
                        mPassword,
                        mSelectedIntegrationData.getId(),
                        GetCompaniesAnswer.class,
                        new Response.Listener<GetCompaniesAnswer>() {
                            @Override
                            public void onResponse(final GetCompaniesAnswer response) {
                                int numberOfCompanies;
                                if (response.onlineCustomers == null) {
                                    numberOfCompanies = 0;
                                } else {
                                    numberOfCompanies = response.onlineCustomers.size();
                                }

                                if (numberOfCompanies == 0) {
                                    alert.showError(R.string.visma_blue_error_no_companies);
                                } else if (numberOfCompanies == 1) {
                                    login(mUserName, mPassword, response.onlineCustomers.get(0),
                                            response.token,
                                            response.userId,
                                            mSelectedIntegrationData.getId(), alert);
                                } else {
                                    alert.closeDialog(new AnimationEndingListener() {
                                        public void onAnimationEnding() {
                                            if (getActivity() == null || getActivity()
                                                    .isFinishing()) {
                                                return;
                                            }

                                            Bundle args = new Bundle();
                                            args.putParcelableArrayList(LoginCompanyFragment
                                                    .ONLINE_CUSTOMERS, response.onlineCustomers);
                                            args.putString(LoginCompanyFragment.USER, mUserName);
                                            args.putString(LoginCompanyFragment.PASSWORD,
                                                    mPassword);
                                            args.putInt(APP_ID, mSelectedIntegrationData.getId());
                                            args.putString(USER_ID, response.userId);
                                            args.putString(TOKEN, response.token);
                                            LoginCompanyFragment loginCompanyFragment = new
                                                    LoginCompanyFragment();
                                            loginCompanyFragment.setArguments(args);

                                            // Allows the user to go back to this fragment using
                                            // the back button.
                                            mChangeFragmentCallback
                                                    .changeFragmentWithBackStack(
                                                            loginCompanyFragment);
                                        }
                                    });
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // If we end up here we have one error that can be shown for all
                                // services
                                // so lets display it.
                                final Activity activity = getActivity();
                                if (activity == null || activity.isFinishing()) {
                                    return;
                                }

                                int errorMessageId;
                                if (error instanceof BlueNetworkError) {
                                    BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                                    errorMessageId = ErrorMessage
                                            .getErrorMessage(blueNetworkError.blueError, false);
                                } else {
                                    errorMessageId = ErrorMessage
                                            .getErrorMessage(OnlineResponseCodes.NotSet, false);
                                }

                                alert.showError(errorMessageId);
                            }
                        });

                // Add the request to the RequestQueue.
                VolleySingleton
                        .getInstance()
                        .addToRequestQueue(request);
            }
        });
    }

    private void setupHideShowBasedOnKeyboard(View view) {
        ((VismaLinearLayout) view.findViewById(R.id.blue_fragment_login_top_level_layout))
                .setLayoutChangeListener(
                        new VismaLinearLayout.OnLayoutChangeListener() {
                            @Override
                            public void onLayoutPushUp(View view) {
                                view.findViewById(R.id.blue_fragment_login_change_server)
                                        .setVisibility(View.GONE);
                            }

                            @Override
                            public void onLayoutPushDown(View view) {
                                view.findViewById(R.id.blue_fragment_login_change_server)
                                        .setVisibility(View.VISIBLE);
                            }
                        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(USERNAME, this.mUserName);
        outState.putString(PASSWORD, this.mPassword);
        outState.putParcelable(IntegrationsActivity.INTEGRATION_EXTRA,
                this.mSelectedIntegrationData);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mChangeFragmentCallback = (ChangeFragment) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
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
            throw new ClassCastException(getActivity().toString()
                    + " must implement ChangeFragment interface.");
        }
    }

    private void setupChangeServerView(View view) {
        mFiveClickDetector = new FiveClickDetector(new FiveClickDetector.OnClickListener() {
            @Override
            public void onFiveClick() {
                openServerListActivity();
            }
        });

        view.findViewById(R.id.blue_fragment_login_change_server).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                mFiveClickDetector.registerClick();
            }
        });
    }

    private void login(final String user,
                       final String password,
                       final GetCompaniesAnswer.OnlineCustomer onlinecustomer,
                       final String token,
                       final String userId,
                       final int appId,
                       final VismaAlertDialog alert) {

        BlueConfig.setAndSaveAppIdToSettings(getActivity(), appId);

        if (!TextUtils.isEmpty(token)) {
            alert.closeDialog(new AnimationEndingListener() {
                public void onAnimationEnding() {
                    if (getActivity() == null || getActivity().isFinishing()) {
                        return;
                    }

                    finishAndSendResult(onlinecustomer, userId,
                            token, appId);
                }
            });
        } else {
            // Formulate the request and handle the response.
            GetTokenRequest<GetTokenAnswer> request = new GetTokenRequest<GetTokenAnswer>(getActivity(),
                    user,
                    password,
                    onlinecustomer.companyId,
                    GetTokenAnswer.class,
                    new Response.Listener<GetTokenAnswer>() {
                        @Override
                        public void onResponse(final GetTokenAnswer response) {
                            alert.closeDialog(new AnimationEndingListener() {
                                public void onAnimationEnding() {
                                    if (getActivity() == null || getActivity().isFinishing()) {
                                        return;
                                    }

                                    finishAndSendResult(onlinecustomer, userId, response.token,
                                            appId);
                                }
                            });
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // If we end up here we have one error that can be shown for all
                            // services
                            // so lets display it.
                            final Activity activity = getActivity();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }

                            int errorMessageId;
                            if (error instanceof BlueNetworkError) {
                                BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                                errorMessageId = ErrorMessage.getErrorMessage(blueNetworkError
                                        .blueError, false);
                            } else {
                                errorMessageId = ErrorMessage.getErrorMessage(OnlineResponseCodes
                                        .NotSet, false);
                            }

                            alert.showError(errorMessageId);
                        }
                    });

            // Add the request to the RequestQueue.
            VolleySingleton.getInstance().addToRequestQueue(request);
        }
    }

    private void finishAndSendResult(GetCompaniesAnswer.OnlineCustomer company, String userId,
                                     String token, int appId) {
        Intent intent = new Intent();
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_USERNAME, mUserName);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_USER_ID, userId);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_COMPANY_NAME, company.companyDisplayName);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_COMPANY_ID, company.companyId);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_COMPANY_COUNTRY_ALPHA2, company
                .countryCodeAlpha2);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_TOKEN, token);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_SYNC, true);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_APP_ID, appId);
        intent.putExtra(LoginActivity.ACTIVITY_RESULT_DEMO, false);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    private void openServerListActivity() {
        Intent serverListIntent = new Intent(getActivity(), ServerListActivity.class);
        startActivity(serverListIntent);
    }
}
