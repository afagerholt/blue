package com.visma.blue.metadata.eaccounting;

import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.content.Context;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.visma.blue.BR;
import com.visma.blue.R;
import com.visma.blue.metadata.BaseMetaDataViewModel;
import com.visma.blue.metadata.CurrencyAdapter;
import com.visma.blue.metadata.MetadataFragment;
import com.visma.blue.metadata.TypePickerDialog;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.OnlinePhotoType;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.qr.UsingQr;
import com.visma.common.DecimalNumberTextWatcher;
import com.visma.common.SimpleTextWatcher;
import com.visma.common.util.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class EAcountingMetadataModelView extends BaseMetaDataViewModel {

    private DatePickerDialog mPaymentDatePickerDialog = null;
    private DatePickerDialog mInvoiceDatePickerDialog = null;
    private DatePickerDialog mDueDatePickerDialog = null;
    private TypePickerDialog mTypePickerDialog;
    private CurrencyAdapter mCurrencyAdapter;
    private android.support.v4.app.FragmentManager mSupportFragManager;
    private InputMethodManager mInputMethodManager;
    private UsingQr mUsingQr;
    private boolean mCorrectMoreViewSize = false;

    public EAcountingMetadataModelView(FragmentManager fragmentManager,
                                        android.support.v4.app.FragmentManager supportFragManager,
                                        OnlineMetaData onlineMetaData,
                                        InputMethodManager inputMethodManager,
                                        Fragment mainFragment,
                                        Context context,
                                        boolean hasBitmap) {
        super(fragmentManager, onlineMetaData, context, hasBitmap);
        mSupportFragManager = supportFragManager;
        mInputMethodManager = inputMethodManager;
        mUsingQr = getQrScanObject();
        mCurrencyAdapter = new CurrencyAdapter(context);
        mTypePickerDialog = createTypePickerDialog(mainFragment);
        mPaymentDatePickerDialog = createPaymentDateDialog();
        mInvoiceDatePickerDialog = createInvoiceDateDialog();
        mDueDatePickerDialog = createDueDateDialog();
    }

    private TypePickerDialog createTypePickerDialog(Fragment targetFragment) {
        TypePickerDialog typePickerDialog = new TypePickerDialog();
        typePickerDialog.setTargetFragment(targetFragment, MetadataFragment.REQUEST_CODE_TYPE);
        return typePickerDialog;
    }

    public void setType(int type) {
        mOnlineMetaData.type = type;
        notifyPropertyChanged(BR.type);
        notifyPropertyChanged(BR.isPaidBoxClickable);
        notifyPropertyChanged(BR.isPaidLayoutEnabled);
    }

    public View.OnClickListener getTypeChangeOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                closeKeyboard(view);
                showTypePickerDialog();
            }
        };
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

                int wrapContentHeight = Util.getHeightForWrapContent(view.getContext(), expandableView);

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
                            (ImageView) view.findViewById(R.id.fragment_metadata_layout_more_information_expander_image);
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
                            (ImageView) view.findViewById(R.id.fragment_metadata_layout_more_information_expander_image);
                    expanderImage.setImageResource(R.drawable.nc_ic_action_expand);
                }
            }
        };
    }

    private void closeKeyboard(View view) {
        mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void showTypePickerDialog() {
        final String tag = "TypePickerDialog";

        synchronized (mTypePickerDialog) {
            if (mSupportFragManager.findFragmentByTag(tag) != null) {
                return;
            }
            Bundle args = new Bundle();
            args.putInt(TypePickerDialog.EXTRA_TYPE, mOnlineMetaData.type);
            mTypePickerDialog.setArguments(args);
            mTypePickerDialog.show(mSupportFragManager, tag);
            // If we want to query the fragment manager for the fragment we need the fragment to be added immediately
            mSupportFragManager.executePendingTransactions();
        }
    }

    @Bindable
    public boolean getIsPaid() {
        if (mOnlineMetaData.isPaid == null) {
            return false;
        }
        return mOnlineMetaData.isPaid;
    }

    private void setIsPaid(boolean isPaid) {
        mOnlineMetaData.isPaid = isPaid;
        if (isPaid) {
            setIsApproved(false);
        }
        notifyPropertyChanged(BR.isPaid);
        notifyPropertyChanged(BR.isPaidDateEnabled);
        notifyPropertyChanged(BR.moreInfoFieldVisibility);
        notifyPropertyChanged(BR.paidLayoutVisibility);
        notifyPropertyChanged(BR.isApprovedBoxClickable);

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

    public CompoundButton.OnCheckedChangeListener getIsPaidCheckboxListener() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mOnlineMetaData.isPaid == false && isChecked == true) {
                    setPaymentDate(new Date());
                }

                setIsPaid(isChecked);

                if (!isChecked) {
                    setPaymentDate(null);
                }

                // If it is payed we will clear some fields and hide them
                // But we only clear them if they are not read from a qr-code
                if (isChecked && TextUtils.isEmpty(mOnlineMetaData.usingQrString)) {
                    setOrganisationNumber(null);
                    setReferenceNumber(null);
                    setInvoiceDate(null);
                    setDueDate(null);
                }

                setCorrectMoreInfoViewSize(true);


                notifyPropertyChanged(BR.isApprovedLayoutEnabled);

            }
        };
    }

    public void updateTypeChange(final int newType) {
        // Handle emailed documents that don't have isPaid set
        // Linas. Don't know if this check is needed in Visma Online integration
        if (mOnlineMetaData.type == OnlinePhotoType.UNKNOWN.getValue()
                || mOnlineMetaData.type == OnlinePhotoType.DOCUMENT.getValue()) {

            if (newType == OnlinePhotoType.INVOICE.getValue()) {
                setIsPaid(false);
            } else if (newType == OnlinePhotoType.RECEIPT.getValue()) {
                setPaymentDate(new Date());
                setIsPaid(true);
            }
        }

        if (newType == OnlinePhotoType.INVOICE.getValue()) {
            setType(OnlinePhotoType.INVOICE.getValue());
        } else if (newType == OnlinePhotoType.RECEIPT.getValue()) {
            setType(OnlinePhotoType.RECEIPT.getValue());
            if (mOnlineMetaData.isPaid != null) { // Don't mess with the ones created before we added this field
                if (!mOnlineMetaData.isPaid) {
                    setPaymentDate(new Date());
                    setIsPaid(true);
                }
            }
        }

        notifyPropertyChanged(BR.approvalLayoutVisibility);
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

        DatePickerDialog duePickerDialog = DatePickerDialog.newInstance(
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

        final GregorianCalendar minDateCalendar = new GregorianCalendar();
        minDateCalendar.set(2000, Calendar.JANUARY, 1);
        duePickerDialog.setMinDate(minDateCalendar);

        return duePickerDialog;
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
                    // If we want to query the fragment manager for the fragment we need the fragment to be added immediately
                    mFragmentManager.executePendingTransactions();
                }
            }
        };
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
                    // If we want to query the fragment manager for the fragment we need the fragment to be added immediately
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
                    // If we want to query the fragment manager for the fragment we need the fragment to be added immediately
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
    public String getName() {
        return mOnlineMetaData.name;
    }

    public TextWatcher getNameWatcher() {
        return new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                mOnlineMetaData.name = editable.toString();
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
    public String getTotalVatAmount() {
        return Util.getFormattedNumberString(mOnlineMetaData.totalVatAmount);
    }

    public TextWatcher getTotalVatWatcher() {
        return new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                mOnlineMetaData.totalVatAmount = Util.parseDouble(s.toString());
            }
        };
    }

    public View.OnFocusChangeListener getTotalVatFocusChangeListener() {
        return new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    notifyPropertyChanged(BR.totalVatAmount);
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

    public int getTotalVatVisibility() {
        String companyCountryCodeAlpha2 = VismaUtils.getCurrentCompanyCountryCodeAlpha2();
        if (companyCountryCodeAlpha2 != null && companyCountryCodeAlpha2.equals("NO")) {
            return View.GONE;
        } else {
            return View.VISIBLE;
        }
    }

    public int getOtherVatVisibility() {
        String companyCountryCodeAlpha2 = VismaUtils.getCurrentCompanyCountryCodeAlpha2();
        if (companyCountryCodeAlpha2 != null && companyCountryCodeAlpha2.equals("NO")) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    @Bindable
    public boolean getIsPaidLayoutEnabled() {
        if (mUsingQr != null) {
            if (mUsingQr.tp == 3) {
                return false;
            }
        }

        if (mOnlineMetaData.type == OnlinePhotoType.RECEIPT.getValue()) {
            return false;
        }

        if (!mOnlineMetaData.canDelete) {
            return false;
        }

        return true;
    }

    @Bindable
    public boolean getIsPaidDateEnabled() {
        //Payment date received from QR scan so it can't be changed
        if (mUsingQr != null) {
            if (mUsingQr.tp == 3) {
                if (mUsingQr.idt != null) {
                    return false;
                }
            }
        }
        //If paid box unchecked disable payment date pick
        if (mOnlineMetaData.isPaid != null) {
            if (!mOnlineMetaData.isPaid) {
                return false;
            }
        }
        //Document registered in system and there for can't be changed
        if (!mOnlineMetaData.canDelete) {
            return false;
        }

        return true;
    }

    @Bindable
    public boolean getIsMoreFieldEnabled() {
        //Data filled from QR code so can't be changed
        if (!TextUtils.isEmpty(mOnlineMetaData.usingQrString)) {
            return false;
        }

        //Document registered in system and there for can't be changed
        if (!mOnlineMetaData.canDelete) {
            return false;
        }

        return true;
    }

    @Bindable
    public int getPaidLayoutVisibility() {
        if (mOnlineMetaData.isPaid == null) {
            return View.GONE;
        } else {
            return View.VISIBLE;
        }
    }

    @Bindable
    public boolean getIsPaidBoxClickable() {
        if (mUsingQr != null) {
            if (mUsingQr.tp == 3) {
                return false;
            }
        }

        if (mOnlineMetaData.type == OnlinePhotoType.RECEIPT.getValue()) {
            return false;
        }

        if (!mOnlineMetaData.canDelete) {
            return false;
        }

        return true;
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
    public boolean getIsTypeLayoutEnabled() {
        if (!mOnlineMetaData.canDelete || !TextUtils.isEmpty(mOnlineMetaData.usingQrString)) {
            return false;
        }
        return true;
    }

    private UsingQr getQrScanObject() {
        if (TextUtils.isEmpty(mOnlineMetaData.usingQrString)) {
            return null;
        }
        UsingQr usingQr = null;
        try {
            Gson gson = new Gson();
            usingQr = gson.fromJson(mOnlineMetaData.usingQrString, UsingQr.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return usingQr;
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

    public void setCorrectMoreInfoViewSize(boolean correctMoreViewSize) {
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

        final View expandableView = root.findViewById(R.id.fragment_metadata_layout_more_information_expandable_view);
        int wrapContentHeight = Util.getHeightForWrapContent(root.getContext(), expandableView);
        int currentHeight = expandableView.getHeight();

        if (currentHeight > 0) {
            final ScrollView scrollView = (ScrollView) root.findViewById(R.id.blue_fragment_metadata_scrollview);
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

    public void updateDataAfterQrScan(String qrString) {
        mOnlineMetaData.usingQrString = qrString;
        mUsingQr = getQrScanObject();
        if (mUsingQr != null) {
            mUsingQr.updateFieldsIn(mOnlineMetaData);
        }
        notifyAllFields();
    }

    private void notifyAllFields() {
        notifyPropertyChanged(BR.type);
        notifyPropertyChanged(BR.isPaid);
        notifyPropertyChanged(BR.invoiceDate);
        notifyPropertyChanged(BR.dueDate);
        notifyPropertyChanged(BR.organisationNumber);
        notifyPropertyChanged(BR.name);
        notifyPropertyChanged(BR.referenceNumber);
        notifyPropertyChanged(BR.dueAmount);
        notifyPropertyChanged(BR.totalVatAmount);
        notifyPropertyChanged(BR.highVatAmount);
        notifyPropertyChanged(BR.middleVatAmount);
        notifyPropertyChanged(BR.lowVatAmount);
        notifyPropertyChanged(BR.isPaidLayoutEnabled);
        notifyPropertyChanged(BR.isPaidDateEnabled);
        notifyPropertyChanged(BR.isMoreFieldEnabled);
        notifyPropertyChanged(BR.moreInfoFieldVisibility);
        notifyPropertyChanged(BR.isTypeLayoutEnabled);
        notifyPropertyChanged(BR.currencySelection);
        notifyPropertyChanged(BR.type);
        notifyPropertyChanged(BR.isPaidBoxClickable);
        notifyPropertyChanged(BR.isPaidLayoutEnabled);
        notifyPropertyChanged(BR.paymentDate);
        notifyPropertyChanged(BR.isApprovedLayoutEnabled);
    }

    public CompoundButton.OnCheckedChangeListener getIsApprovedCheckboxListener() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setIsApproved(isChecked);
            }
        };
    }

    @Bindable
    public boolean getIsApproved() {
        return mOnlineMetaData.approvedForPayment;
    }

    public void setIsApproved(boolean approved) {
        mOnlineMetaData.approvedForPayment = approved;
        notifyPropertyChanged(BR.isApproved);
    }

    @Bindable
    public int getApprovalLayoutVisibility() {
        if (mOnlineMetaData.type != OnlinePhotoType.INVOICE.getValue()) {
            return View.GONE;
        }

        if (!VismaUtils.isSupplierInvoiceApprovalEnabled(mContext) && mOnlineMetaData.canDelete) {
            return View.GONE;
        }

        return View.VISIBLE;
    }

    @Bindable
    public boolean getIsApprovedBoxClickable() {
        if (mOnlineMetaData.isPaid != null) {
            if (mOnlineMetaData.isPaid) {
                return false;
            }
        }
        return mOnlineMetaData.canDelete;

    }

    @Bindable
    public boolean getIsApprovedLayoutEnabled() {
        if (mOnlineMetaData.isPaid != null) {
            if (mOnlineMetaData.isPaid) {
                return false;
            }
        }
        return mOnlineMetaData.canDelete;
    }

    public void onCompanySettingsChange() {
        notifyPropertyChanged(BR.approvalLayoutVisibility);
    }
}
