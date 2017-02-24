package com.visma.blue.metadata.expense;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.TextView;

import com.visma.blue.BR;
import com.visma.blue.R;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.network.containers.SeveraCustomData;
import com.visma.blue.network.requests.customdata.Expense;
import com.visma.common.DecimalKeyListener;
import com.visma.common.DecimalNumberTextWatcher;
import com.visma.common.LabeledView;
import com.visma.common.SimpleTextWatcher;
import com.visma.common.util.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class MetadataViewModel extends BaseObservable {

    public interface ExpenseActionListener {
        void onOpenExpenseTypeList();

        void onOpenExpenseCurrencyList();
    }

    private OnlineMetaData mOnlineMetaData;
    private DatePickerDialog mDatePickerDialog;
    private FragmentManager mFragmentManager;
    private Context mContext;
    private ExpenseActionListener mExpenseListener;
    private ArrayList<Expense.ExpenseType> mExpenseTypes = new ArrayList<>();
    private ArrayList<Expense.Currency> mExpenseCurrencies = new ArrayList<>();

    public MetadataViewModel(Context context, FragmentManager fragmentManager, OnlineMetaData
            onlineMetaData, ExpenseActionListener expenseListener) {
        mExpenseListener = expenseListener;
        mContext = context;
        mOnlineMetaData = onlineMetaData;
        mFragmentManager = fragmentManager;
        mExpenseTypes = new ArrayList<>();
        mDatePickerDialog = createDateDialog(mOnlineMetaData.date);
    }

    public void updateExpenseTypes(ArrayList<Expense.ExpenseType> expenseTypes) {
        mExpenseTypes = expenseTypes;
        notifyPropertyChanged(BR.expenseTypeEnabled);
        notifyPropertyChanged(BR.updateExpenseTypeHeading);
    }

    public void updateCurrencies(ArrayList<Expense.Currency> currencies) {
        mExpenseCurrencies = currencies;
        notifyPropertyChanged(BR.expenseCurrencyEnabled);
        notifyPropertyChanged(BR.updateExpenseCurrencyHeading);
    }

    @Bindable
    public boolean getEnabled() {
        return mOnlineMetaData.canDelete;
    }

    @Bindable
    public String getDate() {
        return SimpleDateFormat.getDateInstance().format(mOnlineMetaData.date);
    }

    public void setDate(int year, int monthOfYear, int dayOfMonth) {
        GregorianCalendar utcCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        utcCalendar.clear();
        utcCalendar.set(year, monthOfYear, dayOfMonth);
        mOnlineMetaData.date = utcCalendar.getTime();
        notifyPropertyChanged(BR.date);

        performExpenseTypeFiltering(mOnlineMetaData.date);
    }

    public View.OnClickListener getDateOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String tag = "DatePickerDialog";

                synchronized (mDatePickerDialog) {
                    if (mFragmentManager.findFragmentByTag(tag) != null) {
                        return;
                    }

                    mDatePickerDialog.show(mFragmentManager, tag);
                    // If we want to query the fragment manager for the fragment we need the
                    // fragment to be added immediately
                    mFragmentManager.executePendingTransactions();
                }
            }
        };
    }

    public String getComment() {
        return mOnlineMetaData.comment;
    }

    public TextWatcher getCommentWatcher() {
        return new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                mOnlineMetaData.comment = editable.toString(); // for this field we use ""
                // instead of null
            }
        };
    }

    public KeyListener getDecimalKeyListener() {
        return new DecimalKeyListener();
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

    public View.OnFocusChangeListener getDueAmountOnFocusChangeListener() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    notifyPropertyChanged(BR.dueAmount);
                }
            }
        };
    }

    private DatePickerDialog createDateDialog(Date initialDate) {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(initialDate);

        DatePickerDialog paymentDatePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int
                            dayOfMonth) {
                        setDate(year, monthOfYear, dayOfMonth);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        final GregorianCalendar maxDateCalendar = new GregorianCalendar();
        paymentDatePickerDialog.setMaxDate(maxDateCalendar);
        final GregorianCalendar minDateCalendar = new GregorianCalendar();
        minDateCalendar.set(2009, Calendar.JANUARY, 1);
        paymentDatePickerDialog.setMinDate(minDateCalendar);

        return paymentDatePickerDialog;
    }

    @Bindable
    public String getExpenseTypeName() {
        if (mOnlineMetaData.expenseCustomData == null
                || mOnlineMetaData.expenseCustomData.expenseType == null) {
            return null;
        }

        return mOnlineMetaData.expenseCustomData.expenseType.name;

    }

    @Bindable
    public View.OnClickListener getExpenseTypeOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnlineMetaData.canDelete) {
                    mExpenseListener.onOpenExpenseTypeList();
                }
            }
        };
    }

    public ArrayList<Expense.ExpenseType> getExpenseTypes() {
        return mExpenseTypes;
    }

    public long getExpenseFilterTime() {
        return mOnlineMetaData.date.getTime();
    }

    public Expense.ExpenseType getSelectedExpenseType() {
        if (mOnlineMetaData.expenseCustomData == null || mOnlineMetaData.expenseCustomData
                .expenseType == null) {
            return null;
        }

        return mOnlineMetaData.expenseCustomData.expenseType;
    }

    public void updateExpenseTypeSelection(Expense.ExpenseType expenseType) {
        if (expenseType != null && expenseType.isEmptyValue) {
            expenseType = null;
        }
        mOnlineMetaData.expenseCustomData.expenseType = expenseType;
        notifyPropertyChanged(BR.expenseTypeName);
    }

    private void performExpenseTypeFiltering(Date filterDate) {
        if (mOnlineMetaData.expenseCustomData == null
                || mOnlineMetaData.expenseCustomData.expenseType == null) {
            return;
        }

        if (!mOnlineMetaData.expenseCustomData.expenseType.isValid(filterDate)) {
            mOnlineMetaData.expenseCustomData.expenseType = null;
            notifyPropertyChanged(BR.expenseTypeName);
        }

    }

    @Bindable
    public String getExpenseCurrencyName() {
        if (mOnlineMetaData.expenseCustomData == null
                || mOnlineMetaData.expenseCustomData.currency == null || mOnlineMetaData
                .expenseCustomData.currency.isEmptyValue) {
            return null;
        }

        return mOnlineMetaData.expenseCustomData.currency.toString();
    }

    @Bindable
    public View.OnClickListener getExpenseCurrencyOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnlineMetaData.canDelete) {
                    mExpenseListener.onOpenExpenseCurrencyList();
                }
            }
        };
    }

    public ArrayList<Expense.Currency> getExpenseCurrencies() {
        return mExpenseCurrencies;
    }


    public Expense.Currency getSelectedExpenseCurrency() {
        if (mOnlineMetaData.expenseCustomData == null || mOnlineMetaData.expenseCustomData
                .currency == null) {
            String defaultCurrency = VismaUtils.getDefaultCurrency(mContext);
            if (shouldSetDefaultCurrency()
                    && (defaultCurrency == null || !defaultCurrency.isEmpty())) {
                return null;
            } else {
                return ExpenseCurrencyAdapter.emptyValue(mContext);
            }

        }

        return mOnlineMetaData.expenseCustomData.currency;
    }

    public void updateExpenseCurrencySelection(Expense.Currency expenseCurrency) {
        mOnlineMetaData.expenseCustomData.currency = expenseCurrency;
        notifyPropertyChanged(BR.expenseCurrencyName);
    }

    public void setDefaultCurrency(String currencyId) {
        if (mOnlineMetaData.expenseCustomData != null
                && mOnlineMetaData.expenseCustomData.currency != null) {
            return;
        }

        if (currencyId == null) {
            if (mExpenseCurrencies.size() > 0 && mExpenseCurrencies.get(0).timesUsed > 0) {
                //The most often used currency
                updateExpenseCurrencySelection(mExpenseCurrencies.get(0));
                return;
            }
        } else {
            for (Expense.Currency currency : mExpenseCurrencies) {
                if (currencyId.equals(currency.guid)) {
                    updateExpenseCurrencySelection(currency);
                    return;
                }
            }

            if (currencyId.equals(ExpenseCurrencyAdapter.emptyValue(mContext).guid)) {
                updateExpenseCurrencySelection(ExpenseCurrencyAdapter.emptyValue(mContext));
            } else {
                if (mExpenseCurrencies.size() > 0 && mExpenseCurrencies.get(0).timesUsed > 0) {
                    //The most often used currency
                    updateExpenseCurrencySelection(mExpenseCurrencies.get(0));
                }
            }
            return;
        }

        updateExpenseCurrencySelection(null);

    }

    public boolean shouldSetDefaultCurrency() {
        return !mOnlineMetaData.isSynchronized && !mOnlineMetaData.isVerified
                && !mOnlineMetaData.isNotSyncedDueToError;
    }

    @Bindable
    public boolean getExpenseTypeEnabled() {
        return mOnlineMetaData.canDelete && !(mExpenseTypes == null || mExpenseTypes.isEmpty());
    }

    @Bindable
    public boolean getExpenseCurrencyEnabled() {
        return mOnlineMetaData.canDelete
                && !(mExpenseCurrencies == null || mExpenseCurrencies.isEmpty());
    }

    @BindingAdapter({"updateExpenseTypeHeading"})
    public static void updateExpenseTypeHeading(final LabeledView holderView, boolean isEnabled) {
        updateLabelStyle(holderView, isEnabled);
    }

    @Bindable
    public boolean getUpdateExpenseTypeHeading() {
        return !((mOnlineMetaData.expenseCustomData == null || mOnlineMetaData.expenseCustomData
                .expenseType == null) && (mExpenseTypes == null || mExpenseTypes.isEmpty()));

    }

    @BindingAdapter({"updateExpenseCurrencyHeading"})
    public static void updateExpenseCurrencyHeading(final LabeledView holderView, boolean isEnabled) {
        updateLabelStyle(holderView, isEnabled);
    }

    @Bindable
    public boolean getUpdateExpenseCurrencyHeading() {
        return !((mOnlineMetaData.expenseCustomData == null || mOnlineMetaData.expenseCustomData
                .currency == null) && (mExpenseCurrencies == null || mExpenseCurrencies.isEmpty()));

    }

    private static void updateLabelStyle(LabeledView holderView, boolean isEnabled) {
        if (holderView == null || holderView.getChildCount() == 0 ) {
            return;
        }
        View labelView =  holderView.getChildAt(0);

        if (labelView instanceof TextView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (isEnabled) {
                    setLabelStyle(holderView, (TextView) labelView, R.style
                            .TextAppearance_FloatLabel);
                } else {
                    setLabelStyle(holderView, (TextView) labelView, R.style
                            .TextAppearance_FloatLabel_Disabled);
                }
            } else {
                if (isEnabled) {
                    setLabelStyle(holderView.getContext(), holderView, (TextView) labelView, R.style
                            .TextAppearance_FloatLabel);
                } else {
                    setLabelStyle(holderView.getContext(), holderView, (TextView) labelView, R.style
                            .TextAppearance_FloatLabel_Disabled);
                }
            }

        }
    }

    private static void setLabelStyle(LabeledView holder, TextView label, @StyleRes int styleId) {
        holder.setTextAppearance(label, styleId);
    }

    private static void setLabelStyle(Context context, LabeledView holder, TextView label,
                                      @StyleRes int styleId) {
        holder.setTextAppearance(context, label, styleId);
    }

}
