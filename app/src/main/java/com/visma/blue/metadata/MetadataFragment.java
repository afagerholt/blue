package com.visma.blue.metadata;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.misc.AppId;
import com.visma.blue.misc.ErrorMessage;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.BlueNetworkError;
import com.visma.blue.network.OnlinePhotoType;
import com.visma.blue.network.OnlineResponseCodes;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.network.requests.customdata.GetCustomDataAnswer;
import com.visma.blue.network.requests.customdata.GetCustomDataRequest;
import com.visma.blue.network.requests.customdata.Netvisor;
import com.visma.blue.qr.QrActivity;
import com.visma.blue.qr.UsingQr;
import com.visma.common.DecimalKeyListener;
import com.visma.common.DecimalNumberTextWatcher;
import com.visma.common.FloatLabelLayout;
import com.visma.common.ViewIdGenerator;
import com.visma.common.VismaAlertDialog;
import com.visma.common.util.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.WeakHashMap;

public class MetadataFragment extends BaseMetadataFragment {
    public static final int ACTIVITY_REQUEST_CODE_RETRIEVE_QR_CODE = 1;
    public static final int REQUEST_CODE_TYPE = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 10;
    private static final String EXTRA_CUSTOM_DATA = "CUSTOM_DATA";

    // Data that is saved and restored
    private ArrayList<Netvisor.DropDown> mCustomSpinnersData;

    private View view;
    private WeakHashMap<String, CustomDataAdapter> mCustomSpinnerAdaptersHashMap =
            new WeakHashMap<String, CustomDataAdapter>();
    private WeakHashMap<String, Spinner> mCustomSpinnersHashMap = new WeakHashMap<String, Spinner>();

    private DatePickerDialog mDatePickerDialog = null;
    private DatePickerDialog mInvoiceDatePickerDialog = null;
    private DatePickerDialog mDueDatePickerDialog = null;
    private DatePickerDialog mPaymentDatePickerDialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle;
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            bundle = savedInstanceState;
        } else {
            bundle = getArguments();
        }

        if (bundle != null) {
            this.mCustomSpinnersData = bundle.getParcelableArrayList(EXTRA_CUSTOM_DATA);
        }

        // Needed for onOptionsItemSelected to be called later on
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.blue_fragment_metadata, container, false);

        // Setup controls
        setupComment(view);
        setupChangeType();
        setupIsPaidCheckBox();
        setupPaidDateControl();
        setupImageLayout(view, R.id.fragment_metadata_layout_image, R.id.fragment_metadata_image_filename);
        setupDatePickerLayout(view);
        setupMoreInformationView(view);

        // Hide, enable or disable controls depending on app version
        hideEnableAndDisableControls(view);

        // Display some data in the controls
        updateData();

        setAppSpecificTexts(view);

        downloadCustomData(inflater.getContext());

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(EXTRA_CUSTOM_DATA, mCustomSpinnersData);
    }

    private void setupDatePickerLayout(View view) {
        View layoutDate = view.findViewById(R.id.layoutDate);
        layoutDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager inputManager =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if (mDatePickerDialog == null) {
                    GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                    calendar.setTime(mOnlineMetaData.date);

                    mDatePickerDialog = DatePickerDialog.newInstance(
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
                                        int dayOfMonth) {
                                    setDate(year, monthOfYear, dayOfMonth, R.id.textViewDate);

                                    GregorianCalendar utcCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                                    utcCalendar.clear();
                                    utcCalendar.set(year, monthOfYear, dayOfMonth);
                                    mOnlineMetaData.date = utcCalendar.getTime();
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));

                    final GregorianCalendar maxDateCalendar = new GregorianCalendar();
                    mDatePickerDialog.setMaxDate(maxDateCalendar);
                    final GregorianCalendar minDateCalendar = new GregorianCalendar();
                    minDateCalendar.set(2000, Calendar.JANUARY, 1);
                    mDatePickerDialog.setMinDate(minDateCalendar);
                }

                // This library does not use the support dialog fragment
                mDatePickerDialog.show(getActivity().getFragmentManager(), "");
            }
        });
    }

    private void setupComment(View view) {
        EditText comment = (EditText) view.findViewById(R.id.commentTextView);
        comment.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                mOnlineMetaData.comment = s.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void correctMoreInformationViewSize() {
        final View expandableView = view.findViewById(R.id.fragment_metadata_layout_more_information_expandable_view);
        int wrapContentHeight = Util.getHeightForWrapContent(getActivity(), expandableView);
        int currentHeight = expandableView.getHeight();

        if (currentHeight > 0) {
            final ScrollView scrollView = (ScrollView) view.findViewById(R.id.blue_fragment_metadata_scrollview);
            final int initialScrollY = scrollView.getScrollY();

            ValueAnimator anim = ValueAnimator.ofInt(currentHeight, wrapContentHeight);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ViewGroup.LayoutParams params = expandableView.getLayoutParams();
                    params.height = (Integer) animation.getAnimatedValue();
                    expandableView.setLayoutParams(params);
                    scrollView.smoothScrollTo(0, initialScrollY);
                }
            });
            anim.setDuration(1000);
            anim.start();
        }
    }

    private void setupMoreInformationView(View view) {
        final View expandableView = view.findViewById(R.id.fragment_metadata_layout_more_information_expandable_view);
        final View clickToExpandView = view.findViewById(R.id.fragment_metadata_layout_more_information);
        clickToExpandView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int wrapContentHeight = Util.getHeightForWrapContent(getActivity(), expandableView);

                if (expandableView.getHeight() == 0) {
                    ValueAnimator anim = ValueAnimator.ofInt(0, wrapContentHeight);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            ViewGroup.LayoutParams params = expandableView.getLayoutParams();
                            params.height = (Integer) animation.getAnimatedValue();
                            expandableView.setLayoutParams(params);
                        }
                    });
                    anim.setDuration(1000);
                    anim.start();

                    ImageView expanderImage =
                            (ImageView) v.findViewById(R.id.fragment_metadata_layout_more_information_expander_image);
                    expanderImage.setImageResource(R.drawable.nc_ic_action_collapse);
                } else {
                    ValueAnimator anim = ValueAnimator.ofInt(expandableView.getHeight(), 0);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            ViewGroup.LayoutParams params = expandableView.getLayoutParams();
                            params.height = (Integer) animation.getAnimatedValue();
                            expandableView.setLayoutParams(params);
                        }
                    });
                    anim.setDuration(1000);
                    anim.start();
                    ImageView expanderImage =
                            (ImageView) v.findViewById(R.id.fragment_metadata_layout_more_information_expander_image);
                    expanderImage.setImageResource(R.drawable.nc_ic_action_expand);
                }
            }
        });

        setupOrganisationNumberControl();
        setupReferenceNumberControl();
        setupNameControl();
        setupInvoiceDateControl();
        setupDueDateControl();
        setupDueAmountControl();
        setupHighVatAmountControl();
        setupMiddleVatAmountControl();
        setupLowVatAmountControl();
        setupZeroVatAmountControl();
        setupTotalVatAmountControl();
        setupCurrencySpinner();
        setupCustomDataDropDowns();
    }

    private void setupCurrencySpinner() {
        Spinner spinner = (Spinner) view.findViewById(R.id.activity_more_information_currency_spinner);
        CurrencyAdapter adapter = new CurrencyAdapter(getActivity());
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mOnlineMetaData.currency = (String) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupInvoiceDateControl() {
        View dateLayout = view.findViewById(R.id.activity_more_information_layout_invoice_date);
        dateLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager inputManager =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                if (mOnlineMetaData.invoiceDate != null) {
                    calendar.setTime(mOnlineMetaData.invoiceDate);
                }

                if (mInvoiceDatePickerDialog == null) {
                    mInvoiceDatePickerDialog = DatePickerDialog.newInstance(
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
                                        int dayOfMonth) {
                                    setDate(year, monthOfYear, dayOfMonth,
                                            R.id.activity_more_information_layout_invoice_date_data);

                                    GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                                    calendar.clear();
                                    calendar.set(year, monthOfYear, dayOfMonth);
                                    mOnlineMetaData.invoiceDate = calendar.getTime();
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));

                    final GregorianCalendar maxDateCalendar = new GregorianCalendar();
                    mInvoiceDatePickerDialog.setMaxDate(maxDateCalendar);
                    final GregorianCalendar minDateCalendar = new GregorianCalendar();
                    minDateCalendar.set(2000, Calendar.JANUARY, 1);
                    mInvoiceDatePickerDialog.setMinDate(minDateCalendar);
                }

                // This library does not use the support dialog fragment
                mInvoiceDatePickerDialog.show(getActivity().getFragmentManager(), "");
            }
        });
    }

    private void setupDueDateControl() {
        View dateLayout = view.findViewById(R.id.activity_more_information_layout_due_date);
        dateLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager inputManager =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                if (mOnlineMetaData.dueDate != null) {
                    calendar.setTime(mOnlineMetaData.dueDate);
                }

                if (mDueDatePickerDialog == null) {
                    mDueDatePickerDialog = DatePickerDialog.newInstance(
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
                                        int dayOfMonth) {
                                    setDate(year, monthOfYear, dayOfMonth,
                                            R.id.activity_more_information_layout_due_date_data);

                                    GregorianCalendar utcCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                                    utcCalendar.clear();
                                    utcCalendar.set(year, monthOfYear, dayOfMonth);
                                    mOnlineMetaData.dueDate = utcCalendar.getTime();
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                }

                // This library does not use the support dialog fragment
                mDueDatePickerDialog.show(getActivity().getFragmentManager(), "");
            }
        });
    }

    private void setupOrganisationNumberControl() {
        EditText editText = (EditText) view.findViewById(R.id.activity_more_information_organisation_number);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                mOnlineMetaData.organisationNumber = s.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void setupReferenceNumberControl() {
        EditText editText = (EditText) view.findViewById(R.id.activity_more_information_reference_number);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                mOnlineMetaData.referenceNumber = s.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void setupNameControl() {
        EditText editText = (EditText) view.findViewById(R.id.activity_more_information_name);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                mOnlineMetaData.name = s.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void setupDueAmountControl() {
        EditText editText = (EditText) view.findViewById(R.id.activity_more_information_due_amount);
        editText.setKeyListener(new DecimalKeyListener()); // https://code.google.com/p/android/issues/detail?id=2626
        editText.addTextChangedListener(new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);

                mOnlineMetaData.dueAmount = Util.parseDouble(s.toString());
            }
        });

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText editText = (EditText) v;
                    editText.setText(Util.getFormattedNumberString(mOnlineMetaData.dueAmount));
                }
            }
        });
    }

    private void setupHighVatAmountControl() {
        EditText editText = (EditText) view.findViewById(R.id.activity_more_information_high_vat_amount);
        editText.setKeyListener(new DecimalKeyListener()); // https://code.google.com/p/android/issues/detail?id=2626
        editText.addTextChangedListener(new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);

                mOnlineMetaData.highVatAmount = Util.parseDouble(s.toString());
            }
        });

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText editText = (EditText) v;
                    editText.setText(Util.getFormattedNumberString(mOnlineMetaData.highVatAmount));
                }
            }
        });
    }

    private void setupMiddleVatAmountControl() {
        EditText editText = (EditText) view.findViewById(R.id.activity_more_information_middle_vat_amount);
        editText.setKeyListener(new DecimalKeyListener()); // https://code.google.com/p/android/issues/detail?id=2626
        editText.addTextChangedListener(new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);

                mOnlineMetaData.middleVatAmount = Util.parseDouble(s.toString());
            }
        });

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText editText = (EditText) v;
                    editText.setText(Util.getFormattedNumberString(mOnlineMetaData.middleVatAmount));
                }
            }
        });
    }

    private void setupLowVatAmountControl() {
        EditText editText = (EditText) view.findViewById(R.id.activity_more_information_low_vat_amount);
        editText.setKeyListener(new DecimalKeyListener()); // https://code.google.com/p/android/issues/detail?id=2626
        editText.addTextChangedListener(new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);

                mOnlineMetaData.lowVatAmount = Util.parseDouble(s.toString());
            }
        });

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText editText = (EditText) v;
                    editText.setText(Util.getFormattedNumberString(mOnlineMetaData.lowVatAmount));
                }
            }
        });
    }

    private void setupZeroVatAmountControl() {
        EditText editText = (EditText) view.findViewById(R.id.activity_more_information_zero_vat_amount);
        editText.setKeyListener(new DecimalKeyListener()); // https://code.google.com/p/android/issues/detail?id=2626
        editText.addTextChangedListener(new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);

                mOnlineMetaData.zeroVatAmount = Util.parseDouble(s.toString());
            }
        });

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText editText = (EditText) v;
                    editText.setText(Util.getFormattedNumberString(mOnlineMetaData.zeroVatAmount));
                }
            }
        });
    }

    private void setupTotalVatAmountControl() {
        EditText editText = (EditText) view.findViewById(R.id.activity_more_information_total_vat_amount);
        editText.setKeyListener(new DecimalKeyListener()); // https://code.google.com/p/android/issues/detail?id=2626
        editText.addTextChangedListener(new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);

                mOnlineMetaData.totalVatAmount = Util.parseDouble(s.toString());
            }
        });

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText editText = (EditText) v;
                    editText.setText(Util.getFormattedNumberString(mOnlineMetaData.totalVatAmount));
                }
            }
        });
    }

    private void setupFakeData() {
        mCustomSpinnersData = new ArrayList<Netvisor.DropDown>();

        for (int i = 0; i < 3; i++) {
            Netvisor.DropDown dropDown = new Netvisor.DropDown();
            dropDown.id = "Fake id - " + Integer.toString(i);
            dropDown.title = "Fake label - " + Integer.toString(i);
            dropDown.type = 0;
            dropDown.values = new ArrayList<Netvisor.Value>();
            mCustomSpinnersData.add(dropDown);

            for (int j = 0; j < 5; j++) {
                String valueId = "Fake value id - " + Integer.toString(j);
                String valueTitle = "Fake value name - " + Integer.toString(j);
                Netvisor.Value fakeValue = new Netvisor.Value(valueId, valueTitle, null);
                dropDown.values.add(fakeValue);
            }
        }
    }

    /*
     * Updates the data in the custom data spinners.
     */
    private void updateCustomDataDropDowns() {
        LinearLayout customBackendDataLayout =
                (LinearLayout) view.findViewById(R.id.blue_fragment_metadata_custom_backend_data_layout);

        // Two cases:
        // 1. The spinner is already created from saved data
        // 2. It is a new spinner, i.e., data on the server has ben updated
        for (Netvisor.DropDown dropdown : mCustomSpinnersData) {
            if (dropdown.type != OnlineMetaData.CustomDataValue.TYPE_SPINNER) {
                continue; // Only spinners are supported in this version
            }

            CustomDataAdapter adapter = null;
            String savedCustomDataValueId = null;

            for (OnlineMetaData.CustomDataValue savedCustomData : mOnlineMetaData.customData) {
                if (savedCustomData.id.equalsIgnoreCase(dropdown.id)) {
                    adapter = mCustomSpinnerAdaptersHashMap.get(savedCustomData.id);
                    savedCustomDataValueId = savedCustomData.valueId;
                    break;
                }
            }

            if (adapter == null) {
                adapter = new CustomDataAdapter(getActivity());

                View customBackendDataItem =
                        View.inflate(customBackendDataLayout.getContext(), R.layout.blue_custom_data_item,
                                customBackendDataLayout);
                TextView label = (TextView) customBackendDataItem.findViewById(R.id.blue_custom_data_item_label);
                label.setId(ViewIdGenerator.generateViewId());
                Spinner spinner = (Spinner) customBackendDataItem.findViewById(R.id.blue_custom_data_item_spinner);
                spinner.setId(ViewIdGenerator.generateViewId());

                /*
                // It opens, but it is not placed in a good way
                final int spinnerId = spinner.getId();
                customBackendDataItem.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            v.findViewById(spinnerId).performClick();
                        } else {
                            v.findViewById(spinnerId).performClick();
                        }
                    }
                });
                */

                // Set the title
                label.setText(dropdown.toString());

                adapter.addAll(dropdown.values);
                spinner.setAdapter(adapter);
                mCustomSpinnerAdaptersHashMap.put(dropdown.id, adapter);
                mCustomSpinnersHashMap.put(dropdown.id, spinner);

                // Set the saved item to the empty item
                OnlineMetaData.CustomDataValue customDataValue = new OnlineMetaData.CustomDataValue();
                customDataValue.id = dropdown.id;
                customDataValue.name = dropdown.toString();
                customDataValue.valueId = adapter.getItem(0).id;
                customDataValue.valueTitle = adapter.getItem(0).toString();
                customDataValue.isEmptyValue = adapter.getItem(0).isEmptyValue;
                mOnlineMetaData.customData.add(customDataValue);

                final String finalSpinnerId = dropdown.id;
                spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        for (OnlineMetaData.CustomDataValue savedCustomDataValue : mOnlineMetaData.customData) {
                            if (savedCustomDataValue.id.equalsIgnoreCase(finalSpinnerId)) {
                                Netvisor.Value adapterValue = (Netvisor.Value) parent.getItemAtPosition(position);
                                savedCustomDataValue.valueId = adapterValue.id;
                                savedCustomDataValue.valueTitle = adapterValue.title;
                                savedCustomDataValue.valueSubTitle = adapterValue.subTitle;
                                savedCustomDataValue.isEmptyValue = adapterValue.isEmptyValue;
                                break;
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            } else {
                // Find the id of the selected element in the spinner.
                // Delete the old element if it exists in the new downloaded data.
                // Set the selected position of the spinner to
                // Find the position of the selected element in
                //

                int position = 0;
                boolean valueFound = false;
                for (Netvisor.Value value : dropdown.values) {
                    position++;
                    if (value.id.equalsIgnoreCase(savedCustomDataValueId)) {
                        // The value exists in the new data set sent from the server.
                        // We also have a temporary copy added to the adapter that we must now remove.
                        valueFound = true;
                        break;
                    }
                }

                if (valueFound) {
                    adapter.remove(adapter.getItem(1)); // The old value is always on position 1
                    Spinner spinner = mCustomSpinnersHashMap.get(dropdown.id);
                    adapter.addAll(dropdown.values);
                    spinner.setSelection(position);
                } else {
                    adapter.addAll(dropdown.values);
                }
            }
        }

        correctMoreInformationViewSize();
        hideEnableAndDisableControls(view);
    }

    /*
     * Create spinners and sets the value of them based on what the saved values are.
     */
    private void setupCustomDataDropDowns() {
        LinearLayout customBackendDataLayout =
                (LinearLayout) view.findViewById(R.id.blue_fragment_metadata_custom_backend_data_layout);

        for (OnlineMetaData.CustomDataValue savedCustomDataValue : mOnlineMetaData.customData) {
            if (savedCustomDataValue.type != OnlineMetaData.CustomDataValue.TYPE_SPINNER) {
                continue; // Only spinners are supported in this version
            }

            View customBackendDataItem =
                    View.inflate(customBackendDataLayout.getContext(), R.layout.blue_custom_data_item,
                            customBackendDataLayout);
            TextView label = (TextView) customBackendDataItem.findViewById(R.id.blue_custom_data_item_label);
            label.setId(ViewIdGenerator.generateViewId());
            Spinner spinner = (Spinner) customBackendDataItem.findViewById(R.id.blue_custom_data_item_spinner);
            spinner.setId(ViewIdGenerator.generateViewId());

            /*
            // It opens, but it is not placed in a good way
            final int spinnerId = spinner.getId();
            customBackendDataItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        v.findViewById(spinnerId).performClick();
                    } else {
                        v.findViewById(spinnerId).performClick();
                    }
                }
            });
            */

            // Set the title
            label.setText(savedCustomDataValue.name);

            CustomDataAdapter adapter = new CustomDataAdapter(getActivity());
            spinner.setAdapter(adapter);
            mCustomSpinnerAdaptersHashMap.put(savedCustomDataValue.id, adapter);
            mCustomSpinnersHashMap.put(savedCustomDataValue.id, spinner);

            // Create and add the currently selected item to the adapter
            Netvisor.Value selectedValue =
                    new Netvisor.Value(savedCustomDataValue.valueId, savedCustomDataValue.valueTitle,
                            savedCustomDataValue.valueSubTitle);
            adapter.add(selectedValue);
            spinner.setSelection(1);

            final String finalSpinnerId = savedCustomDataValue.id;
            spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    for (OnlineMetaData.CustomDataValue customDataValue : mOnlineMetaData.customData) {
                        if (customDataValue.id.equalsIgnoreCase(finalSpinnerId)) {
                            Netvisor.Value adapterValue = (Netvisor.Value) parent.getItemAtPosition(position);
                            customDataValue.valueId = adapterValue.id;
                            customDataValue.valueTitle = adapterValue.title;
                            customDataValue.valueSubTitle = adapterValue.subTitle;
                            customDataValue.isEmptyValue = adapterValue.isEmptyValue;
                            break;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    /**
     * The order is more or less like this
     * App specific parts:
     * Visa * More information
     * Visa * isPaid
     * Dölj * momsfältet
     * Visa * Fyra momsfält
     * <p/>
     * Generic rules:
     * Enable isPaid om kvitto
     * Enable payment date om isPaid
     * Om qr så enable eller disable isPaid baserat på typ av qr-dokument
     * Dölj isPaid om null
     * Disable payment date om inte isPaid
     * Disable MoreInformation om qr-kod
     * Dölj fält baserat på isPaid
     * <p/>
     * App specific parts:
     * Dölj, och disable.
     */
    private void hideEnableAndDisableControls(View view) {
        View layoutType = view.findViewById(R.id.layoutType);
        View layoutDate = view.findViewById(R.id.layoutDate);
        View isPaidLayout = view.findViewById(R.id.blue_fragment_metadata_layout_payed);
        View isPaid = view.findViewById(R.id.blue_fragment_metadata_payed_checkbox);
        View paymentDateLayout = view.findViewById(R.id.blue_fragment_metadata_layout_payment_date);

        // The blocks in this function must be in the order display/enable and then disable/hide

        // Enable and display based on app version
        String companyCountryCodeAlpha2 = VismaUtils.getCurrentCompanyCountryCodeAlpha2();
        switch (BlueConfig.getAppType()) {
            case UNKNOWN:
            case VISMA_ONLINE:
            case EACCOUNTING: {
                View moreInformationView = view.findViewById(R.id.fragment_metadata_layout_more_information);
                moreInformationView.setVisibility(View.VISIBLE);
                isPaidLayout.setVisibility(View.VISIBLE);
                paymentDateLayout.setVisibility(View.VISIBLE);

                // 3 fields for vat in Norway
                if (companyCountryCodeAlpha2 != null && companyCountryCodeAlpha2.equals("NO")) {
                    View temp = view.findViewById(R.id.activity_more_information_layout_total_vat);
                    temp.setVisibility(View.GONE);
                    temp = view.findViewById(R.id.activity_more_information_layout_high_vat);
                    temp.setVisibility(View.VISIBLE);
                    temp = view.findViewById(R.id.activity_more_information_layout_middle_vat);
                    temp.setVisibility(View.VISIBLE);
                    temp = view.findViewById(R.id.activity_more_information_layout_low_vat);
                    temp.setVisibility(View.VISIBLE);
                }
                break;
            }
            case MAMUT:
                break;
            case EXPENSE_MANAGER:
                break;
            case ACCOUNTVIEW:
                break;
            case NETVISOR: {
                View moreInformationView = view.findViewById(R.id.fragment_metadata_layout_more_information);
                moreInformationView.setVisibility(View.VISIBLE);
                //isPaidLayout.setVisibility(View.VISIBLE);
                paymentDateLayout.setVisibility(View.VISIBLE);

                View temp = view.findViewById(R.id.activity_more_information_layout_total_vat);
                temp.setVisibility(View.GONE);
                temp = view.findViewById(R.id.activity_more_information_layout_high_vat);
                temp.setVisibility(View.VISIBLE);
                temp = view.findViewById(R.id.activity_more_information_layout_middle_vat);
                temp.setVisibility(View.VISIBLE);
                temp = view.findViewById(R.id.activity_more_information_layout_low_vat);
                temp.setVisibility(View.VISIBLE);
                temp = view.findViewById(R.id.activity_more_information_layout_zero_vat);
                temp.setVisibility(View.VISIBLE);
            }
            break;
            default:
                throw new UnsupportedOperationException("Not implemented.");
        }

        // Enable fields
        if (mOnlineMetaData.type != OnlinePhotoType.RECEIPT.getValue()) {
            isPaidLayout.setEnabled(true);
            isPaid.setClickable(true);
        }

        if (mOnlineMetaData.isPaid != null) {
            if (mOnlineMetaData.isPaid == true) {
                paymentDateLayout.setEnabled(true);
            }
        }

        if (!TextUtils.isEmpty(mOnlineMetaData.usingQrString)) {
            UsingQr usingQr;
            try {
                layoutType.setEnabled(false);

                Gson gson = new Gson();
                usingQr = gson.fromJson(mOnlineMetaData.usingQrString, UsingQr.class);

                if (usingQr.tp != 3) {
                    isPaidLayout.setEnabled(true);
                    isPaid.setClickable(true);
                }

                // Disable fields

                if (usingQr.tp == 3) {
                    isPaidLayout.setEnabled(false);
                    isPaid.setClickable(false);

                    if (usingQr.idt != null) {
                        paymentDateLayout.setEnabled(false);
                    }
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        if (mOnlineMetaData.type == OnlinePhotoType.RECEIPT.getValue()) {
            isPaidLayout.setEnabled(false);
            isPaid.setClickable(false);
        }

        if (mOnlineMetaData.isPaid == null) {
            isPaidLayout.setVisibility(View.GONE);
            paymentDateLayout.setVisibility(View.GONE);
        } else {
            if (mOnlineMetaData.isPaid == false) {
                paymentDateLayout.setEnabled(false);
            }
        }

        if (!TextUtils.isEmpty(mOnlineMetaData.usingQrString)) {
            disableMoreInformationDataFields(true);
        }

        TextView informationTextView = (TextView) view.findViewById(R.id.metaDataInformationTextView);
        if (!mOnlineMetaData.canDelete) { // The image has been "connected", lock down changes.
            layoutType.setEnabled(false);
            isPaidLayout.setEnabled(false);
            isPaid.setClickable(false);
            paymentDateLayout.setEnabled(false);
            layoutDate.setEnabled(false);
            View comment = view.findViewById(R.id.commentTextView);
            comment.setEnabled(false);
            disableMoreInformationDataFields(false);

            informationTextView.setText(R.string.visma_blue_label_photo_is_connected);
        } else if (mHasBitmap) { // A new document not yet uploaded. Or a locally stored document.
            informationTextView.setText(R.string.visma_blue_label_photo_is_a_copy);
            if (shouldHidePhotoIsACopyText(BlueConfig.getAppType())) {
                informationTextView.setVisibility(View.GONE);
            }
        } else if (mOnlineMetaData.isVerified == false) { // A document that has been emailed
            informationTextView.setText(R.string.visma_blue_not_verified_document);
            informationTextView.setVisibility(View.VISIBLE);
        } else { // This is where we end up when looking at a saved document that is on the server
            informationTextView.setVisibility(View.GONE);
        }

        // Hide or display fields based on if it is marked as paid or not
        if (mOnlineMetaData.isPaid != null) {
            int paidVisibility = mOnlineMetaData.isPaid ? View.GONE : View.VISIBLE;
            View temp = view.findViewById(R.id.activity_more_information_layout_organisation_number);
            temp.setVisibility(paidVisibility);
            temp = view.findViewById(R.id.activity_more_information_layout_reference_number);
            temp.setVisibility(paidVisibility);
            temp = view.findViewById(R.id.activity_more_information_layout_due_date);
            temp.setVisibility(paidVisibility);
            temp = view.findViewById(R.id.activity_more_information_layout_invoice_date);
            temp.setVisibility(paidVisibility);
            correctMoreInformationViewSize();
        }

        // Disable and hide based on app version
        switch (BlueConfig.getAppType()) {
            case UNKNOWN:
            case VISMA_ONLINE:
            case EACCOUNTING:
                layoutDate.setVisibility(View.GONE);
                break;
            case MAMUT:
                break;
            case EXPENSE_MANAGER:
                layoutType.setClickable(false);
                break;
            case ACCOUNTVIEW:
                layoutType.setClickable(false);
                break;
            case NETVISOR:
                layoutDate.setVisibility(View.GONE);
                layoutType.setClickable(false);
                view.findViewById(R.id.activity_more_information_layout_information_name).setVisibility(View.GONE);
                break;
            default:
                throw new UnsupportedOperationException("Not implemented.");
        }
    }

    protected boolean shouldHidePhotoIsACopyText(AppId appId) {
        switch (appId) {
            case UNKNOWN:
            case VISMA_ONLINE:
            case EACCOUNTING:
            case MAMUT:
                return false;
            case EXPENSE_MANAGER:
                return true;
            case ACCOUNTVIEW:
            case NETVISOR:
                return false;
            default:
                throw new UnsupportedOperationException("Not implemented.");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        //TODO: Break this out into sub classes.
        switch (BlueConfig.getAppType()) {
            case VISMA_ONLINE:
            case EACCOUNTING:
                break;
            case UNKNOWN:
            case MAMUT:
            case EXPENSE_MANAGER:
            case ACCOUNTVIEW:
            case NETVISOR:
                menu.findItem(R.id.blue_fragment_metadata_menu_qr_code).setVisible(false);
                break;
            default:
                throw new UnsupportedOperationException("Not implemented.");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.blue_fragment_metadata_menu_qr_code) {
            startQr();

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQr();
            } else {
                Snackbar.make(getView(), R.string.visma_blue_error_missing_permission, Snackbar.LENGTH_LONG)
                        .show(); // Don’t forget to show!
            }
        }
    }

    private void startQr() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.

            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);

            return;
        }

        Intent intent = new Intent(getActivity(), QrActivity.class);
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE_RETRIEVE_QR_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_REQUEST_CODE_RETRIEVE_QR_CODE && resultCode == FragmentActivity.RESULT_OK) {
            mOnlineMetaData.usingQrString = data.getExtras().getString(QrActivity.ACTIVITY_RESULT_CODE_QR_MESSAGE);

            Gson gson = new Gson();
            UsingQr temp = gson.fromJson(mOnlineMetaData.usingQrString, UsingQr.class);
            temp.updateFieldsIn(mOnlineMetaData);
            updateData();
            hideEnableAndDisableControls(view);
        }

        if (requestCode == REQUEST_CODE_TYPE && resultCode == Activity.RESULT_OK) {
            final int newType = data.getIntExtra(TypePickerDialog.EXTRA_TYPE, -1);

            // Handle emailed documents that don't have isPaid set
            if (mOnlineMetaData.type == OnlinePhotoType.UNKNOWN.getValue() || mOnlineMetaData.type == OnlinePhotoType
                    .DOCUMENT.getValue()) {
                if (newType == OnlinePhotoType.INVOICE.getValue()) {
                    mOnlineMetaData.isPaid = false;
                } else if (newType == OnlinePhotoType.RECEIPT.getValue()) {
                    mOnlineMetaData.paymentDate = new Date();
                    mOnlineMetaData.isPaid = true;
                }
            }

            if (newType == OnlinePhotoType.INVOICE.getValue()) {
                mOnlineMetaData.type = OnlinePhotoType.INVOICE.getValue();
            } else if (newType == OnlinePhotoType.RECEIPT.getValue()) {
                mOnlineMetaData.type = OnlinePhotoType.RECEIPT.getValue();
                if (mOnlineMetaData.isPaid != null) { // Don't mess with the ones created before we added this field
                    if (mOnlineMetaData.isPaid == false) {
                        mOnlineMetaData.paymentDate = new Date();
                        mOnlineMetaData.isPaid = true;
                    }
                }
            }

            updateData();
            hideEnableAndDisableControls(view);
        }
    }

    private void disableMoreInformationDataFields(boolean disableOnlyQrBasedFields) {
        final int[] ids = {R.id.fragment_metadata_layout_more_information_expandable_view,
                // The EditTexts are here because we can not put android:duplicateParentState="true" in them
                // because it somehow messes up the focused state so that they get no color on focus
                R.id.activity_more_information_name,
                R.id.activity_more_information_organisation_number,
                R.id.activity_more_information_reference_number,
                R.id.activity_more_information_due_amount,
                R.id.activity_more_information_high_vat_amount,
                R.id.activity_more_information_middle_vat_amount,
                R.id.activity_more_information_low_vat_amount,
                R.id.activity_more_information_zero_vat_amount,
                R.id.activity_more_information_total_vat_amount,
                R.id.activity_more_information_layout_invoice_date,
                R.id.activity_more_information_layout_due_date};

        for (int id : ids) {
            View temp = this.view.findViewById(id);
            temp.setEnabled(false);
        }

        Spinner spinner = (Spinner) view.findViewById(R.id.activity_more_information_currency_spinner);
        spinner.setEnabled(false);

        if (disableOnlyQrBasedFields) {
            return;
        }

        // The fields below are never based on information from the QR-code so no need to disable them
        // when a QR-code has been added.
        for (Spinner customDataSpinner : mCustomSpinnersHashMap.values()) {
            customDataSpinner.setEnabled(false);
        }
    }

    private void updateData() {
        TextView tv; // HoloEverywhere does not inherit in the same way as stock android

        {
            tv = (TextView) view.findViewById(R.id.typeTextView);
            int resid = VismaUtils.getTypeTextId(mOnlineMetaData.type);
            tv.setText(resid);
            getActivity().setTitle(resid);

            if (mOnlineMetaData.isPaid != null) {
                CheckBox isPayed = (CheckBox) view.findViewById(R.id.blue_fragment_metadata_payed_checkbox);
                isPayed.setChecked(mOnlineMetaData.isPaid);
            }

            tv = (TextView) view.findViewById(R.id.textViewDate);
            tv.setText(mOnlineMetaData.getLocalDateString());

            if (mOnlineMetaData.paymentDate != null) {
                GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                calendar.setTime(mOnlineMetaData.paymentDate);
                setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                        R.id.blue_fragment_payment_date_data);
            } else {
                tv = (TextView) view.findViewById(R.id.blue_fragment_payment_date_data);
                tv.setText("");
            }

            EditText comment = (EditText) view.findViewById(R.id.commentTextView);
            comment.setText(mOnlineMetaData.comment);
        }

        tv = (TextView) view.findViewById(R.id.activity_more_information_name);

        tv.setText(mOnlineMetaData.name);

        tv = (TextView) view.findViewById(R.id.activity_more_information_organisation_number);
        tv.setText(mOnlineMetaData.organisationNumber);

        tv = (TextView) view.findViewById(R.id.activity_more_information_reference_number);
        tv.setText(mOnlineMetaData.referenceNumber);

        tv = (TextView) view.findViewById(R.id.activity_more_information_due_amount);
        tv.setText(Util.getFormattedNumberString(mOnlineMetaData.dueAmount));

        tv = (TextView) view.findViewById(R.id.activity_more_information_high_vat_amount);
        tv.setText(Util.getFormattedNumberString(mOnlineMetaData.highVatAmount));

        tv = (TextView) view.findViewById(R.id.activity_more_information_middle_vat_amount);
        tv.setText(Util.getFormattedNumberString(mOnlineMetaData.middleVatAmount));

        tv = (TextView) view.findViewById(R.id.activity_more_information_low_vat_amount);
        tv.setText(Util.getFormattedNumberString(mOnlineMetaData.lowVatAmount));

        tv = (TextView) view.findViewById(R.id.activity_more_information_zero_vat_amount);
        tv.setText(Util.getFormattedNumberString(mOnlineMetaData.zeroVatAmount));

        tv = (TextView) view.findViewById(R.id.activity_more_information_total_vat_amount);
        tv.setText(Util.getFormattedNumberString(mOnlineMetaData.totalVatAmount));

        // Currency
        Spinner spinner = (Spinner) view.findViewById(R.id.activity_more_information_currency_spinner);
        @SuppressWarnings("unchecked")
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        int position = adapter.getPosition(mOnlineMetaData.currency != null ? mOnlineMetaData.currency : "");
        spinner.setSelection(position);

        // The dates are stored in the "UTC" time zone, so we must read them as that as well
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        if (mOnlineMetaData.invoiceDate != null) {
            calendar.setTime(mOnlineMetaData.invoiceDate);
            setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                    R.id.activity_more_information_layout_invoice_date_data);
        } else {
            tv = (TextView) view.findViewById(R.id.activity_more_information_layout_invoice_date_data);
            tv.setText("");
        }

        if (mOnlineMetaData.dueDate != null) {
            calendar.setTime(mOnlineMetaData.dueDate);
            setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                    R.id.activity_more_information_layout_due_date_data);
        } else {
            tv = (TextView) view.findViewById(R.id.activity_more_information_layout_due_date_data);
            tv.setText("");
        }
    }

    private void setDate(int year, int monthOfYear, int dayOfMonth, int resourceId) {
        GregorianCalendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        Date date = calendar.getTime();
        String dateString = SimpleDateFormat.getDateInstance().format(date);

        TextView dateTextView = (TextView) view.findViewById(resourceId);
        dateTextView.setText(dateString);
    }

    private void setupChangeType() {
        View layoutType = view.findViewById(R.id.layoutType);
        layoutType.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager inputManager =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                showTypePickerDialog();
            }
        });
    }

    private void setupIsPaidCheckBox() {
        CheckBox isPayed = (CheckBox) view.findViewById(R.id.blue_fragment_metadata_payed_checkbox);
        isPayed.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mOnlineMetaData.isPaid == false && isChecked == true) {
                    mOnlineMetaData.paymentDate = new Date();
                }

                mOnlineMetaData.isPaid = isChecked;

                if (isChecked == false) {
                    mOnlineMetaData.paymentDate = null;
                }

                // If it is payed we will clear some fields and hide them
                // But we only clear them if they are not read from a qr-code
                if (isChecked == true && TextUtils.isEmpty(mOnlineMetaData.usingQrString)) {
                    mOnlineMetaData.organisationNumber = null;
                    mOnlineMetaData.referenceNumber = null;
                    mOnlineMetaData.invoiceDate = null;
                    mOnlineMetaData.dueDate = null;
                }

                hideEnableAndDisableControls(view);
                updateData();
            }
        });
    }

    private void setupPaidDateControl() {
        View paidDateLayout = view.findViewById(R.id.blue_fragment_metadata_layout_payment_date);
        paidDateLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager inputManager =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                final GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

                if (mPaymentDatePickerDialog == null) {
                    mPaymentDatePickerDialog = DatePickerDialog.newInstance(
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
                                        int dayOfMonth) {
                                    setDate(year, monthOfYear, dayOfMonth, R.id.blue_fragment_payment_date_data);

                                    GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                                    calendar.clear();
                                    calendar.set(year, monthOfYear, dayOfMonth);
                                    mOnlineMetaData.paymentDate = calendar.getTime();
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));

                    final GregorianCalendar maxDateCalendar = new GregorianCalendar();
                    mPaymentDatePickerDialog.setMaxDate(maxDateCalendar);
                    final GregorianCalendar minDateCalendar = new GregorianCalendar();
                    minDateCalendar.set(2000, Calendar.JANUARY, 1);
                    mPaymentDatePickerDialog.setMinDate(minDateCalendar);
                }

                // This library does not use the support dialog fragment
                mPaymentDatePickerDialog.show(getActivity().getFragmentManager(), "");
            }
        });
    }

    private void finishActivityWithOk() {
        getActivity().setResult(FragmentActivity.RESULT_OK);
        getActivity().finish();
    }

    private void showPhotoActivity() {
        Intent intent = new Intent(getActivity(), PhotoActivity.class);
        startActivity(intent);
    }

    @Override
    protected synchronized void showTypePickerDialog() {
        final String tag = "TypePickerDialog";

        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentByTag(tag) != null) {
            return;
        }

        TypePickerDialog typePickerDialog = new TypePickerDialog();
        typePickerDialog.setTargetFragment(this, REQUEST_CODE_TYPE);

        Bundle args = new Bundle();
        args.putInt(TypePickerDialog.EXTRA_TYPE, mOnlineMetaData.type);
        typePickerDialog.setArguments(args);
        typePickerDialog.show(fm, tag);
        // If we want to query the fragment manager for the fragment we need the fragment to be added immediately
        fm.executePendingTransactions();
    }

    private void downloadCustomData(Context context) {
        if (BlueConfig.getAppType() != AppId.NETVISOR) {
            return;
        }

        final String token = VismaUtils.getToken();
        if (TextUtils.isEmpty(token)) {
            return;
        }

        // Formulate the request and handle the response.
        GetCustomDataRequest<GetCustomDataAnswer> request =
                new GetCustomDataRequest<GetCustomDataAnswer>(context,
                        token,
                        GetCustomDataAnswer.class,
                        new Response.Listener<GetCustomDataAnswer>() {
                            @Override
                            public void onResponse(final GetCustomDataAnswer response) {
                                final Activity activity = getActivity();
                                if (activity == null || activity.isFinishing()) {
                                    return;
                                }

                                mCustomSpinnersData = response.netvisor.dropDowns;
                                updateCustomDataDropDowns();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
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

                                VismaAlertDialog alert = new VismaAlertDialog(activity);
                                alert.showError(errorMessageId);
                            }
                });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);
    }

    private void setAppSpecificTexts(View view) {
        switch (BlueConfig.getAppType()) {
            case UNKNOWN:
            case VISMA_ONLINE:
            case EACCOUNTING:
                break;
            case MAMUT:
                break;
            case EXPENSE_MANAGER:
                break;
            case ACCOUNTVIEW:
                break;
            case NETVISOR:
                // TODO: This solution is so bad, we we use it as a temporary solution only.
                // Yeah right. This comment will probably be here in a year.
                final EditText highVatField = (EditText) view
                        .findViewById(R.id.activity_more_information_high_vat_amount);
                final EditText middleVatField = (EditText) view
                        .findViewById(R.id.activity_more_information_middle_vat_amount);
                final EditText lowVatField = (EditText) view
                        .findViewById(R.id.activity_more_information_low_vat_amount);
                final EditText zeroVatField = (EditText) view
                        .findViewById(R.id.activity_more_information_zero_vat_amount);

                highVatField.setHint(
                        R.string.visma_blue_metadata_more_information_high_vat_amount_netvisor);
                middleVatField.setHint(
                        R.string.visma_blue_metadata_more_information_middle_vat_amount_netvisor);
                lowVatField.setHint(
                        R.string.visma_blue_metadata_more_information_low_vat_amount_netvisor);
                zeroVatField.setHint(
                        R.string.visma_blue_metadata_more_information_zero_vat_amount_netvisor);

                final FloatLabelLayout highVatLayout = (FloatLabelLayout) view
                        .findViewById(R.id.activity_more_information_layout_high_vat);
                final FloatLabelLayout middleVatLayout = (FloatLabelLayout) view
                        .findViewById(R.id.activity_more_information_layout_middle_vat);
                final FloatLabelLayout lowVatLayout = (FloatLabelLayout) view
                        .findViewById(R.id.activity_more_information_layout_low_vat);
                final FloatLabelLayout zeroVatLayout = (FloatLabelLayout) view
                        .findViewById(R.id.activity_more_information_layout_zero_vat);

                Resources resources = getResources();
                highVatLayout.setFloatLabelHint(
                        resources.getString(R.string.visma_blue_metadata_more_information_high_vat_amount_netvisor));
                middleVatLayout.setFloatLabelHint(
                        resources.getString(R.string.visma_blue_metadata_more_information_middle_vat_amount_netvisor));
                lowVatLayout.setFloatLabelHint(
                        resources.getString(R.string.visma_blue_metadata_more_information_low_vat_amount_netvisor));
                zeroVatLayout.setFloatLabelHint(
                        resources.getString(R.string.visma_blue_metadata_more_information_zero_vat_amount_netvisor));
                break;
            default:
                throw new UnsupportedOperationException("Not implemented.");
        }
    }
}
