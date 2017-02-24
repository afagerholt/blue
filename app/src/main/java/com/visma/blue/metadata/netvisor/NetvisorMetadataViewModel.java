package com.visma.blue.metadata.netvisor;

import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.content.Context;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.visma.blue.BR;
import com.visma.blue.R;
import com.visma.blue.metadata.BaseMetaDataViewModel;
import com.visma.blue.metadata.CurrencyAdapter;
import com.visma.blue.metadata.CustomDataAdapter;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.network.requests.customdata.Netvisor;
import com.visma.common.DecimalNumberTextWatcher;
import com.visma.common.SimpleTextWatcher;
import com.visma.common.ViewIdGenerator;
import com.visma.common.util.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.WeakHashMap;

public class NetvisorMetadataViewModel extends BaseMetaDataViewModel {

    private WeakHashMap<String, CustomDataAdapter> mCustomSpinnerAdaptersHashMap = new WeakHashMap<>();
    private WeakHashMap<String, Spinner> mCustomSpinnersHashMap = new WeakHashMap<>();
    private CurrencyAdapter mCurrencyAdapter;
    private DatePickerDialog mPaymentDatePickerDialog;
    private DatePickerDialog mInvoiceDatePickerDialog = null;
    private DatePickerDialog mDueDatePickerDialog = null;
    private InputMethodManager mInputMethodManager;
    private boolean mCorrectMoreViewSize;

    public NetvisorMetadataViewModel(FragmentManager fragmentManager,
                                     OnlineMetaData onlineMetaData,
                                     Context context,
                                     boolean hasBitmap,
                                     InputMethodManager inputMethodManager) {
        super(fragmentManager, onlineMetaData, context, hasBitmap);
        mInputMethodManager = inputMethodManager;
        mCurrencyAdapter = new CurrencyAdapter(context);
        mPaymentDatePickerDialog = createPaymentDateDialog();
        mInvoiceDatePickerDialog = createInvoiceDateDialog();
        mDueDatePickerDialog = createDueDateDialog();
    }


    public View.OnClickListener getMoreInformationClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final View expandableView =
                        ((View) view.getParent()).findViewById(
                                R.id.fragment_metadata_layout_more_information_expandable_view);

                if (expandableView == null) {
                    return;
                }

                closeKeyboard(view);

                int wrapContentHeight = Util.getHeightForWrapContent(view.getContext(),
                        expandableView);

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
                            (ImageView) view.findViewById(R.id
                                    .fragment_metadata_layout_more_information_expander_image);
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
                            (ImageView) view.findViewById(R.id
                                    .fragment_metadata_layout_more_information_expander_image);
                    expanderImage.setImageResource(R.drawable.nc_ic_action_expand);
                }
            }
        };
    }

    @Bindable
    public String getPaymentDate() {
        if (mOnlineMetaData.paymentDate == null) {
            return "";
        }
        return SimpleDateFormat.getDateInstance().format(mOnlineMetaData.paymentDate);
    }

    private void setPaymentDate(Date date) {
        mOnlineMetaData.paymentDate = date;
        notifyPropertyChanged(BR.paymentDate);
    }

    private DatePickerDialog createPaymentDateDialog() {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

        if (mOnlineMetaData.paymentDate != null) {
            calendar.setTime(mOnlineMetaData.paymentDate);
        }

        DatePickerDialog paymentDatePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
                                          int dayOfMonth) {
                        setPaymentDate(getCustomDate(year, monthOfYear, dayOfMonth));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        final GregorianCalendar maxDateCalendar = new GregorianCalendar();
        paymentDatePickerDialog.setMaxDate(maxDateCalendar);
        final GregorianCalendar minDateCalendar = new GregorianCalendar();
        minDateCalendar.set(2000, Calendar.JANUARY, 1);
        paymentDatePickerDialog.setMinDate(minDateCalendar);

        return paymentDatePickerDialog;
    }

    public View.OnClickListener getPaymentDateOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String tag = "PaymentDatePickerDialog";

                synchronized (mPaymentDatePickerDialog) {
                    if (mFragmentManager.findFragmentByTag(tag) != null) {
                        return;
                    }

                    mPaymentDatePickerDialog.show(mFragmentManager, tag);
                    // If we want to query the fragment manager for the fragment we need the
                    // fragment to be added immediately
                    mFragmentManager.executePendingTransactions();
                }
            }
        };
    }

    @Bindable
    public String getDueAmount() {
        return Util.getFormattedNumberString(mOnlineMetaData.dueAmount);
    }

    public TextWatcher getDueAmountWatcher() {
        return new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                mOnlineMetaData.dueAmount = Util.parseDouble(s.toString());
            }
        };
    }

    public View.OnFocusChangeListener getDueAmountFocusChangeListener() {
        return new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    notifyPropertyChanged(BR.dueAmount);
                }
            }
        };
    }

    @Bindable
    public String getHighVatAmount() {
        return Util.getFormattedNumberString(mOnlineMetaData.highVatAmount);
    }

    public TextWatcher getHighVatWatcher() {
        return new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                mOnlineMetaData.highVatAmount = Util.parseDouble(s.toString());
            }
        };
    }

    public View.OnFocusChangeListener getHighVatFocusChangeListener() {
        return new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    notifyPropertyChanged(BR.highVatAmount);
                }
            }
        };
    }

    @Bindable
    public String getMiddleVatAmount() {
        return Util.getFormattedNumberString(mOnlineMetaData.middleVatAmount);
    }

    public TextWatcher getMiddleVatWatcher() {
        return new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                mOnlineMetaData.middleVatAmount = Util.parseDouble(s.toString());
            }
        };
    }

    public View.OnFocusChangeListener getMiddleVatFocusChangeListener() {
        return new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    notifyPropertyChanged(BR.middleVatAmount);
                }
            }
        };
    }

    @Bindable
    public String getLowVatAmount() {
        return Util.getFormattedNumberString(mOnlineMetaData.lowVatAmount);
    }

    public TextWatcher getLowVatWatcher() {
        return new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                mOnlineMetaData.lowVatAmount = Util.parseDouble(s.toString());
            }
        };
    }

    public View.OnFocusChangeListener getLowVatFocusChangeListener() {
        return new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    notifyPropertyChanged(BR.lowVatAmount);
                }
            }
        };
    }

    @Bindable
    public String getZeroVatAmount() {
        return Util.getFormattedNumberString(mOnlineMetaData.zeroVatAmount);
    }

    public TextWatcher getZeroVatWatcher() {
        return new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                mOnlineMetaData.zeroVatAmount = Util.parseDouble(s.toString());
            }
        };
    }

    public View.OnFocusChangeListener getZeroVatFocusChangeListener() {
        return new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    notifyPropertyChanged(BR.zeroVatAmount);
                }
            }
        };
    }

    @Bindable
    public int getCurrencySelection() {
        @SuppressWarnings("unchecked")
        int position = mCurrencyAdapter.getPosition(mOnlineMetaData.currency);
        if (position == -1) { // No matching item was found
            return 0;
        }

        return position;
    }

    @Bindable
    public BaseAdapter getCurrencyAdapter() {
        return mCurrencyAdapter;
    }

    public AdapterView.OnItemSelectedListener getCurrencyItemSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mOnlineMetaData.currency = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }

    private void setCorrectMoreInfoViewSize(boolean correctMoreViewSize) {
        mCorrectMoreViewSize = correctMoreViewSize;
        notifyPropertyChanged(BR.correctMoreInfoViewSize);
    }

    @Bindable
    public boolean getCorrectMoreInfoViewSize() {
        return mCorrectMoreViewSize;
    }

    @BindingAdapter("correctMoreInfoViewSize")
    public static void correctMoreInfoViewSize(View root, boolean correctSize) {
        if (!correctSize) {
            return;
        }

        final View expandableView = root.findViewById(R.id
                .fragment_metadata_layout_more_information_expandable_view);
        int wrapContentHeight = Util.getHeightForWrapContent(root.getContext(), expandableView);
        int currentHeight = expandableView.getHeight();

        if (currentHeight > 0) {
            final ScrollView scrollView = (ScrollView) root.findViewById(R.id
                    .blue_fragment_metadata_scrollview);
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

    private void closeKeyboard(View view) {
        mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager
                .HIDE_NOT_ALWAYS);
    }

    @Bindable
    public int getMoreInfoFieldVisibility() {
        if (mOnlineMetaData.isPaid != null) {
            if (mOnlineMetaData.isPaid) {
                return View.GONE;
            }
        }
        return View.VISIBLE;
    }

    @Bindable
    public String getInvoiceDate() {
        if (mOnlineMetaData.invoiceDate == null) {
            return "";
        }

        return SimpleDateFormat.getDateInstance().format(mOnlineMetaData.invoiceDate);
    }

    private void setInvoiceDate(Date date) {
        mOnlineMetaData.invoiceDate = date;
        notifyPropertyChanged(BR.invoiceDate);
    }

    private DatePickerDialog createInvoiceDateDialog() {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

        if (mOnlineMetaData.invoiceDate != null) {
            calendar.setTime(mOnlineMetaData.invoiceDate);
        }

        DatePickerDialog invoiseDatePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
                                          int dayOfMonth) {
                        setInvoiceDate(getCustomDate(year, monthOfYear, dayOfMonth));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        final GregorianCalendar maxDateCalendar = new GregorianCalendar();
        invoiseDatePickerDialog.setMaxDate(maxDateCalendar);
        final GregorianCalendar minDateCalendar = new GregorianCalendar();
        minDateCalendar.set(2000, Calendar.JANUARY, 1);
        invoiseDatePickerDialog.setMinDate(minDateCalendar);

        return invoiseDatePickerDialog;
    }

    @Bindable
    public String getDueDate() {
        if (mOnlineMetaData.dueDate == null) {
            return "";
        }

        return SimpleDateFormat.getDateInstance().format(mOnlineMetaData.dueDate);
    }

    private void setDueDate(Date date) {
        mOnlineMetaData.dueDate = date;
        notifyPropertyChanged(BR.dueDate);
    }

    private DatePickerDialog createDueDateDialog() {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

        if (mOnlineMetaData.dueDate != null) {
            calendar.setTime(mOnlineMetaData.dueDate);
        }

        DatePickerDialog invoiceDatePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
                                          int dayOfMonth) {
                        setDueDate(getCustomDate(year, monthOfYear, dayOfMonth));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        return invoiceDatePickerDialog;
    }

    public View.OnClickListener getInvoiceDateOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String tag = "InvoiceDatePickerDialog";

                synchronized (mInvoiceDatePickerDialog) {
                    if (mFragmentManager.findFragmentByTag(tag) != null) {
                        return;
                    }

                    mInvoiceDatePickerDialog.show(mFragmentManager, tag);
                    // If we want to query the fragment manager for the fragment we need the
                    // fragment to be added immediately
                    mFragmentManager.executePendingTransactions();
                }
            }
        };
    }

    public View.OnClickListener getDueDateOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String tag = "DueDatePickerDialog";

                synchronized (mDueDatePickerDialog) {
                    if (mFragmentManager.findFragmentByTag(tag) != null) {
                        return;
                    }

                    mDueDatePickerDialog.show(mFragmentManager, tag);
                    // If we want to query the fragment manager for the fragment we need the
                    // fragment to be added immediately
                    mFragmentManager.executePendingTransactions();
                }
            }
        };
    }

    @Bindable
    public String getOrganisationNumber() {
        return mOnlineMetaData.organisationNumber;
    }

    private void setOrganisationNumber(String number) {
        mOnlineMetaData.organisationNumber = number;
        notifyPropertyChanged(BR.organisationNumber);
    }

    public TextWatcher getOrganisationNumberWatcher() {
        return new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                mOnlineMetaData.organisationNumber = editable.toString();
            }
        };
    }

    @Bindable
    public String getReferenceNumber() {
        return mOnlineMetaData.referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        mOnlineMetaData.referenceNumber = referenceNumber;
        notifyPropertyChanged(BR.referenceNumber);
    }

    public TextWatcher getReferenceNumberWatcher() {
        return new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                mOnlineMetaData.referenceNumber = editable.toString();
            }
        };
    }

    /*
     * Create spinners and sets the value of them based on what the saved values are.
     */
    public void setupCustomDataDropDowns(View rootView) {
        LinearLayout customBackendDataLayout =
                (LinearLayout) rootView.findViewById(R.id.blue_fragment_metadata_custom_backend_data_layout);

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

            CustomDataAdapter adapter = new CustomDataAdapter(rootView.getContext());
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
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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

            disableCustomDataSpinners();
        }
    }

    private void disableCustomDataSpinners() {
        if (!mOnlineMetaData.canDelete) {
            for (Spinner customDataSpinner : mCustomSpinnersHashMap.values()) {
                customDataSpinner.setEnabled(false);
            }
        }
    }

    /*
   * Updates the data in the custom data spinners.
   */
    public void updateCustomDataDropDowns(View rootView, ArrayList<Netvisor.DropDown>
            customSpinnerData) {
        LinearLayout customBackendDataLayout =
                (LinearLayout) rootView.findViewById(R.id.blue_fragment_metadata_custom_backend_data_layout);

        // Two cases:
        // 1. The spinner is already created from saved data
        // 2. It is a new spinner, i.e., data on the server has ben updated
        for (Netvisor.DropDown dropdown : customSpinnerData) {
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
                adapter = new CustomDataAdapter(rootView.getContext());

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
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
        disableCustomDataSpinners();
        setCorrectMoreInfoViewSize(true);
    }

    @Override
    public String getInformationText() {
        String infoText = super.getInformationText();
        if (infoText.equals(mContext.getString(R.string.visma_blue_label_photo_is_a_copy))) {
           return "";
        }
        return infoText;
    }
}