package com.visma.blue.metadata.severa;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.visma.blue.BR;
import com.visma.blue.R;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.network.containers.SeveraCustomData;
import com.visma.blue.network.requests.customdata.Severa;
import com.visma.common.DecimalKeyListener;
import com.visma.common.DecimalNumberTextWatcher;
import com.visma.common.SimpleTextWatcher;
import com.visma.common.util.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SeveraMetadataFragmentModelView extends BaseObservable {

    public interface SeveraActionListener {
        void onCasePhaseClick();
    }

    private FragmentManager mFragmentManager;
    private InputMethodManager mInputMethodManager;

    private DatePickerDialog mStartDatePickerDialog;
    private DatePickerDialog mEndDatePickerDialog;
    private TimePickerDialog mStartTimePickerDialog;
    private TimePickerDialog mEndTimePickerDialog;

    private SeveraProductAdapter mExpenseTypeAdapter;
    private SeveraTaxAdapter mTaxAdapter;

    private OnlineMetaData mOnlineMetaData;
    private Severa mCustomSeveraData;
    private SeveraActionListener mSeveraActionListener;

    private Context mContext;
    private static boolean isCustomDataEnabled;

    public SeveraMetadataFragmentModelView(OnlineMetaData onlineMetaData,
                                           FragmentManager fragmentManager,
                                           InputMethodManager inputMethodManager,
                                           Context context,
                                           Severa customSeveraData,
                                           SeveraActionListener severaActionListener) {
        mFragmentManager = fragmentManager;
        mOnlineMetaData = onlineMetaData;
        mInputMethodManager = inputMethodManager;
        mContext = context;
        mCustomSeveraData = customSeveraData;
        mSeveraActionListener = severaActionListener;
        checkIfSeveraDataNotNull();
        mExpenseTypeAdapter = new SeveraProductAdapter(context);
        mTaxAdapter = new SeveraTaxAdapter(context);
        mStartDatePickerDialog = createStartDateDateDialog();
        mStartTimePickerDialog = createStartTimePicker();
        mEndTimePickerDialog = createEndTimePickerDialog();
        isCustomDataEnabled = false;
    }

    //Needed when taken new photo
    private void checkIfSeveraDataNotNull() {
        if (mOnlineMetaData.severaCustomData == null) {
            mOnlineMetaData.severaCustomData = new SeveraCustomData();
        }
    }

    public boolean getEnabled() {
        return mOnlineMetaData.canDelete;
    }

    @Bindable
    public String getComment() {
        return mOnlineMetaData.comment;
    }

    public TextWatcher getCommentWatcher() {
        return new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                // for this field we use instead of null
                mOnlineMetaData.comment = editable.toString();
            }
        };
    }

    private DatePickerDialog createStartDateDateDialog() {
        final GregorianCalendar calendar = new GregorianCalendar();
        if (mOnlineMetaData.severaCustomData.startDateUtc != null) {
            calendar.setTime(mOnlineMetaData.severaCustomData.startDateUtc);
        } else {
            clearCalendarTime(calendar);
            mOnlineMetaData.severaCustomData.startDateUtc = calendar.getTime();
        }


        DatePickerDialog startDatePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int
                            dayOfMonth) {
                        Date startDate = getDateChange(year, monthOfYear, dayOfMonth,
                                mOnlineMetaData.severaCustomData.startDateUtc);
                        setStartDate(startDate);
                        setStartTime(startDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        final GregorianCalendar minDateCalendar = new GregorianCalendar();
        minDateCalendar.set(2000, Calendar.JANUARY, 1);
        startDatePickerDialog.setMinDate(minDateCalendar);

        return startDatePickerDialog;
    }

    private DatePickerDialog createEndDateDialog() {
        // Set calendar to start date if end date does not exist yet to have a starting point
        // don't assign end date yet in order to keep an ability to choose future date for
        // a start date (that ability is removed after end date is not null anymore)
        final GregorianCalendar calendar = new GregorianCalendar();
        if (mOnlineMetaData.severaCustomData.endDateUtc != null) {
            calendar.setTime(mOnlineMetaData.severaCustomData.endDateUtc);
        } else {
            calendar.setTime(mOnlineMetaData.severaCustomData.startDateUtc);
        }

        DatePickerDialog endDatePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int
                            dayOfMonth) {
                        Date endDate;
                        if (mOnlineMetaData.severaCustomData.endDateUtc != null) {
                            endDate = getDateChange(year, monthOfYear, dayOfMonth, mOnlineMetaData
                                    .severaCustomData.endDateUtc);
                        } else {
                            endDate = getDateChange(year, monthOfYear, dayOfMonth);
                        }

                        setEndDate(endDate);
                        setEndTime(endDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        return endDatePickerDialog;
    }

    private TimePickerDialog createStartTimePicker() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.clear();
        calendar.setTime(mOnlineMetaData.severaCustomData.startDateUtc);

        TimePickerDialog startTimePickerDialog = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
                        setStartTime(getTimeChange(hourOfDay, minute,
                                mOnlineMetaData.severaCustomData.startDateUtc));
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);

        startTimePickerDialog.setStartTime(calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));

        return startTimePickerDialog;
    }

    private TimePickerDialog createEndTimePickerDialog() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.clear();

        if (mOnlineMetaData.severaCustomData.endDateUtc != null) {
            calendar.setTime(mOnlineMetaData.severaCustomData.endDateUtc);
        } else {
            calendar.setTime(mOnlineMetaData.severaCustomData.startDateUtc);
        }

        TimePickerDialog endTimePickerDialog = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
                        Date endTime;
                        if (mOnlineMetaData.severaCustomData.endDateUtc != null) {
                            endTime = getTimeChange(hourOfDay, minute,
                                    mOnlineMetaData.severaCustomData.endDateUtc);
                        } else {
                            endTime = getTimeChange(hourOfDay, minute,
                                    mOnlineMetaData.severaCustomData.startDateUtc);
                        }
                        setEndTime(endTime);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);

        endTimePickerDialog.setStartTime(calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
        return endTimePickerDialog;
    }

    private Date getTimeChange(int hourOfDay, int minute, Date currentSetTime) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.clear();
        calendar.setTime(currentSetTime);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getDateChange(int year, int monthOfYear, int dayOfMonth, Date oldDate) {
        GregorianCalendar newDateCalendar = new GregorianCalendar();
        newDateCalendar.clear();
        newDateCalendar.set(year, monthOfYear, dayOfMonth);

        // Prevent time resetting
        GregorianCalendar oldStartDateCalendar = new GregorianCalendar();
        oldStartDateCalendar.setTime(oldDate);

        newDateCalendar.set(Calendar.HOUR_OF_DAY, oldStartDateCalendar.get(Calendar.HOUR_OF_DAY));
        newDateCalendar.set(Calendar.MINUTE, oldStartDateCalendar.get(Calendar.MINUTE));
        return newDateCalendar.getTime();
    }

    private Date getDateChange(int year, int monthOfYear, int dayOfMonth) {
        GregorianCalendar newDateCalendar = new GregorianCalendar();
        newDateCalendar.clear();
        newDateCalendar.set(year, monthOfYear, dayOfMonth);
        return newDateCalendar.getTime();
    }

    private void setEndDate(Date endDate) {
        mOnlineMetaData.severaCustomData.endDateUtc = endDate;
        notifyPropertyChanged(BR.endDate);
    }

    @Bindable
    public String getEndDate() {
        if (mOnlineMetaData.severaCustomData.endDateUtc == null) {
            return "";
        }
        return SimpleDateFormat.getDateInstance().format(mOnlineMetaData.severaCustomData
                .endDateUtc);
    }

    private void setEndTime(Date endDate) {
        mOnlineMetaData.severaCustomData.endDateUtc = endDate;
        notifyPropertyChanged(BR.endTime);
    }

    @Bindable
    public String getEndTime() {
        if (mOnlineMetaData.severaCustomData.endDateUtc == null) {
            return "";
        }
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(mOnlineMetaData
                .severaCustomData.endDateUtc);
    }

    private void setStartDate(Date startDate) {
        mOnlineMetaData.severaCustomData.startDateUtc = startDate;
        notifyPropertyChanged(BR.startDate);
    }

    @Bindable
    public String getStartDate() {
        if (mOnlineMetaData.severaCustomData.startDateUtc == null) {
            return "";
        }
        return SimpleDateFormat.getDateInstance().format(mOnlineMetaData.severaCustomData
                .startDateUtc);
    }

    private void setStartTime(Date startDate) {
        mOnlineMetaData.severaCustomData.startDateUtc = startDate;
        notifyPropertyChanged(BR.startTime);
    }

    @Bindable
    public String getStartTime() {
        if (getRemainingFieldsVisibility() == View.GONE) {
            return "";
        }

        if (mOnlineMetaData.severaCustomData.startDateUtc == null) {
            return "";
        }
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(mOnlineMetaData
                .severaCustomData.startDateUtc);
    }

    public View.OnClickListener getStartDateOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                clearAmountViewFocus(view);
                closeKeyboard(view);
                final String tag = "StartDatePickerDialog";

                synchronized (mStartDatePickerDialog) {

                    if (mOnlineMetaData.severaCustomData.endDateUtc != null) {
                        final GregorianCalendar maxDateCalendar = new GregorianCalendar();
                        maxDateCalendar.setTime(mOnlineMetaData.severaCustomData.endDateUtc);
                        mStartDatePickerDialog.setMaxDate(maxDateCalendar);
                    }

                    if (mFragmentManager.findFragmentByTag(tag) != null) {
                        return;
                    }

                    mStartDatePickerDialog.show(mFragmentManager, tag);
                    // If we want to query the fragment manager for the fragment we need the
                    // fragment to be added immediately
                    mFragmentManager.executePendingTransactions();
                }
            }
        };
    }

    public View.OnClickListener getStartTimeOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                clearAmountViewFocus(view);
                closeKeyboard(view);
                final String tag = "StartTimePickerDialog";

                synchronized (mStartTimePickerDialog) {
                    if (mFragmentManager.findFragmentByTag(tag) != null) {
                        return;
                    }

                    mStartTimePickerDialog.show(mFragmentManager, tag);
                    // If we want to query the fragment manager for the fragment we need the
                    // fragment to be added immediately
                    mFragmentManager.executePendingTransactions();
                }
            }
        };
    }

    public View.OnClickListener getEndDateOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                clearAmountViewFocus(view);
                closeKeyboard(view);

                if (mEndDatePickerDialog == null) {
                    mEndDatePickerDialog = createEndDateDialog();
                }

                final String tag = "EndDatePickerDialog";

                synchronized (mEndDatePickerDialog) {

                    if (mOnlineMetaData.severaCustomData.startDateUtc != null) {
                        final GregorianCalendar minDateCalendar = new GregorianCalendar();
                        minDateCalendar.setTime(mOnlineMetaData.severaCustomData.startDateUtc);
                        mEndDatePickerDialog.setMinDate(minDateCalendar);
                    }

                    if (mFragmentManager.findFragmentByTag(tag) != null) {
                        return;
                    }

                    mEndDatePickerDialog.show(mFragmentManager, tag);
                    // If we want to query the fragment manager for the fragment we need the
                    // fragment to be added immediately
                    mFragmentManager.executePendingTransactions();
                }
            }
        };
    }

    public View.OnClickListener getEndTimeOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                clearAmountViewFocus(view);
                closeKeyboard(view);
                final String tag = "EndTimePickerDialog";

                synchronized (mEndTimePickerDialog) {
                    if (mFragmentManager.findFragmentByTag(tag) != null) {
                        return;
                    }

                    mEndTimePickerDialog.show(mFragmentManager, tag);
                    // If we want to query the fragment manager for the fragment we need the
                    // fragment to be added immediately
                    mFragmentManager.executePendingTransactions();
                }
            }
        };
    }

    private void setAmount(double amount) {
        mOnlineMetaData.dueAmount = amount;
        notifyPropertyChanged(BR.amount);
    }

    @Bindable
    public String getAmount() {
        if (mOnlineMetaData.dueAmount == null) {
            return Util.getFormattedNumberString(0.0);
        } else {
            return Util.getFormattedNumberString(mOnlineMetaData.dueAmount);
        }

    }

    private void setCurrency(String currency) {
        mOnlineMetaData.currency = currency;
        notifyPropertyChanged(BR.currency);
    }

    @Bindable
    public String getCurrency() {
        if (mOnlineMetaData.currency != null) {
            return mContext.getString(R.string.visma_blue_metadata_more_information_due_amount)
                    + " (" + mOnlineMetaData.currency + ")";
        } else {
            return mContext.getString(R.string.visma_blue_metadata_more_information_due_amount);
        }
    }

    public TextWatcher getAmountWatcher() {
        return new DecimalNumberTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                mOnlineMetaData.dueAmount = Util.parseDouble(s.toString());
            }
        };
    }

    public View.OnFocusChangeListener getAmountFocusChangeListener() {
        return new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    notifyPropertyChanged(BR.amount);
                }
            }
        };
    }

    public TextView.OnEditorActionListener getAmountEditorActionListener() {
        return new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    closeKeyboard(view);
                    view.clearFocus();
                    return true;
                }
                return false;
            }
        };
    }

    public DecimalKeyListener getDecimalKeyListener() {
        return new DecimalKeyListener();
    }

    private void closeKeyboard(View view) {
        mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager
                .HIDE_NOT_ALWAYS);
    }

    //Request focus so that amount view loose focus when clicked on other view.
    private void clearAmountViewFocus(View view) {
        View parent = (View) view.getParent();
        parent.setFocusable(true);
        parent.setFocusableInTouchMode(true);
        parent.requestFocus();
    }

    private Severa.Product getProduct() {
        return new Severa.Product(
                mOnlineMetaData.severaCustomData.product.guid,
                mOnlineMetaData.severaCustomData.product.name,
                mOnlineMetaData.severaCustomData.product.useStartAndEndTime,
                mOnlineMetaData.severaCustomData.vatPercentage,
                mOnlineMetaData.currency,
                mOnlineMetaData.dueAmount
        );
    }

    @Bindable
    public int getProductSelection() {
        //If nothing is selected, then select the empty element placed the top of the adapter
        if (mOnlineMetaData.severaCustomData.product == null) {
            return 0;
        }

        Severa.Product currentlySelectedProduct = getProduct();

        int position = mExpenseTypeAdapter.getPositionByProductGuid(currentlySelectedProduct.guid);
        Log.d("debug", "Count: " + mExpenseTypeAdapter.getCount());
        // No matching item in the adapter
        if (position == -1) {
            mExpenseTypeAdapter.add(currentlySelectedProduct);
            return getProductSelection();
        }
        return position;
    }

    @Bindable
    public BaseAdapter getExpenseTypeAdapter() {
        return mExpenseTypeAdapter;
    }

    public AdapterView.OnItemSelectedListener getExpenseTypeItemSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Severa.Product product = (Severa.Product) parent.getItemAtPosition(position);
                mExpenseTypeAdapter.highlightText(position);

                // Prevent the assigning of empty element on default trigger
                if (product.guid == null) {
                    mOnlineMetaData.severaCustomData.product = null;
                    mOnlineMetaData.dueAmount = null;
                    notifyExtraFieldsVisibility();
                    return;
                }

                mOnlineMetaData.severaCustomData.product = new SeveraCustomData.Product(
                        product.guid,
                        product.name,
                        product.useStartAndEndTime
                );

                notifyExtraFieldsVisibility();

                // Stop if this is the saved product update from fetched list to prevent saved vat
                // and amount overriding
                String lastSelectedGuid = mExpenseTypeAdapter.getSelectedGuid();
                if (lastSelectedGuid != null && lastSelectedGuid.equals(
                        mOnlineMetaData.severaCustomData.product.guid)) {
                    return;
                }

                setTaxVatPercentage(product.vatPercentage);
                setAmount(product.price);
                setCurrency(product.currencyCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }

    public View.OnTouchListener getTouchListener() {
        return new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                closeKeyboard(view);
                clearAmountViewFocus(view);
                return false;
            }
        };
    }

    private void setTaxVatPercentage(double percentage) {
        mOnlineMetaData.severaCustomData.vatPercentage = percentage;
        notifyPropertyChanged(BR.taxSelection);
    }

    @Bindable
    public int getTaxSelection() {
        //If nothing is selected, then select the empty element placed the top of the adapter
        if (mOnlineMetaData.severaCustomData.vatPercentage == null) {
            return 0;
        }

        Severa.Tax tax = new Severa.Tax(mOnlineMetaData.severaCustomData.vatPercentage);

        int position = mTaxAdapter.getPositionByTaxPercentage(tax.percentage);

        // No matching item in the adapter
        if (position == -1) {
            mTaxAdapter.add(tax);
            return getTaxSelection();
        }
        return position;
    }

    @Bindable
    public BaseAdapter getTaxAdapter() {
        return mTaxAdapter;
    }

    public AdapterView.OnItemSelectedListener getTaxItemSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Severa.Tax tax = (Severa.Tax) parent.getItemAtPosition(position);
                mTaxAdapter.highlightText(position);
                mOnlineMetaData.severaCustomData.vatPercentage = tax.percentage;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }

    private void createSeveraCustomDataObject() {
        if (mCustomSeveraData == null) {
            mCustomSeveraData = new Severa();
        }
    }

    public void updateCasesPhases(ArrayList<Severa.Case> cases) {
        createSeveraCustomDataObject();
        mCustomSeveraData.cases = cases;
        if (mOnlineMetaData.canDelete) {
            isCustomDataEnabled = true;
        }
        updateCasePhaseSelection();
        notifyPropertyChanged(BR.casePhaseEnabled);
    }

    @BindingAdapter({"updateCasesPhases"})
    public static void updateCasesPhases(final View rootView,
                                         final SeveraCustomData severaCustomData) {
        if (!VismaUtils.isRunningUnitTest) {
            updateSelectCaseField(rootView, severaCustomData);
        } else {
            //Robolectric ActivityController class doesn't receive callback when calling
            // visible() method. There is a bug with Robolectric data binding and dynamically
            // created and added views. It seems that if view visibility is modified by data
            // binding and and after that you add views dynamically unit test will run for ever
            // with out stopping. Adding views dynamically in other thread seems to solve the
            // problem. This workaround is only used when running unit tests for Severa.
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    updateSelectCaseField(rootView, severaCustomData);
                }
            });
        }

    }

    public void updateProducts(ArrayList<Severa.Product> products) {
        createSeveraCustomDataObject();
        mCustomSeveraData.products = products;
        updateProducts();
        notifyPropertyChanged(BR.expenseTypeEnabled);
    }

    private void updateProducts() {
        if (mCustomSeveraData != null && mCustomSeveraData.products != null) {
            mExpenseTypeAdapter.updateProducts(mContext, mCustomSeveraData.products);

            //Check if product was already selected
            if (mOnlineMetaData.severaCustomData.product == null) {
                return;
            }
            notifyPropertyChanged(BR.productSelection);
        }
    }

    public void updateTaxes(ArrayList<Severa.Tax> taxes) {
        createSeveraCustomDataObject();
        mCustomSeveraData.taxes = taxes;
        updateTaxes();
        notifyPropertyChanged(BR.taxesEnabled);
    }

    private void updateTaxes() {
        if (mCustomSeveraData != null && mCustomSeveraData.taxes != null) {
            if (mOnlineMetaData.severaCustomData.vatPercentage == null) {
                mTaxAdapter.updateTaxes(mCustomSeveraData.taxes);
                return;
            }

            mTaxAdapter.updateTaxes(mCustomSeveraData.taxes);
            notifyPropertyChanged(BR.taxSelection);
        }
    }

    private void notifyExtraFieldsVisibility() {
        notifyPropertyChanged(BR.remainingFieldsVisibility);
        notifyPropertyChanged(BR.timeFieldVisibility);
    }

    @Bindable
    public int getRemainingFieldsVisibility() {
        if (mOnlineMetaData.severaCustomData.product == null) {
            return View.GONE;
        }

        Severa.Product product = getProduct();
        if (product.guid == null) {
            return View.GONE;
        }

        return View.VISIBLE;
    }

    @Bindable
    public int getTimeFieldVisibility() {
        if (mOnlineMetaData.severaCustomData.product == null) {
            return View.GONE;
        }

        Severa.Product product = getProduct();
        if (product.guid == null) {
            return View.GONE;
        }

        if (!product.useStartAndEndTime) {
            clearTimeFieldsData();
            return View.GONE;
        }

        return View.VISIBLE;
    }

    private void clearTimeFieldsData() {
        // Clear end date and the fields related to it
        if (mOnlineMetaData.severaCustomData.endDateUtc != null) {
            setEndDate(null);
            setEndTime(null);
        }

        // Clear the start time of the start date object
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(mOnlineMetaData.severaCustomData.startDateUtc);
        clearCalendarTime(calendar);

        // Assign start date with cleared time
        setStartTime(calendar.getTime());
    }

    private void clearCalendarTime(GregorianCalendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    @Bindable
    public boolean getCasePhaseEnabled() {
        if (!mOnlineMetaData.canDelete) {
            return false;
        }

        if (mCustomSeveraData == null) {
            return false;
        }

        if (!isCustomDataEnabled) {
            return false;
        }

        return mCustomSeveraData.cases != null;
    }

    @Bindable
    public boolean getExpenseTypeEnabled() {
        if (!mOnlineMetaData.canDelete) {
            return false;
        }

        if (mCustomSeveraData == null) {
            return false;
        }

        return mCustomSeveraData.products != null;
    }

    @Bindable
    public boolean getTaxesEnabled() {
        if (!mOnlineMetaData.canDelete) {
            return false;
        }

        if (mCustomSeveraData == null) {
            return false;
        }

        return mCustomSeveraData.taxes != null;
    }

    @Bindable
    public SeveraCustomData getUpdateCasesPhases() {
        return mOnlineMetaData.severaCustomData;
    }

    private static int getCustomDataTextColor() {
        if (isCustomDataEnabled) {
            return R.color.primary_text_nc_light;
        } else {
            return R.color.bright_foreground_disabled_nc_light;
        }
    }

    private static void updateSelectCaseField(View rootView, SeveraCustomData severaCustomData) {
        LinearLayout casesAndPhases = (LinearLayout) rootView
                .findViewById(R.id.layoutCasesAndPhases);

        casesAndPhases.removeAllViews();

        Context context = rootView.getContext();

        TextView caseView = new TextView(rootView.getContext());
        caseView.setTextSize(16);
        caseView.setTextColor(ContextCompat.getColor(context, getCustomDataTextColor()));
        caseView.setSingleLine(true);
        caseView.setPadding(0, 0, 0, 12);
        caseView.setEllipsize(TextUtils.TruncateAt.END);
        casesAndPhases.addView(caseView);

        if (severaCustomData == null || severaCustomData.severaCase != null) {
            caseView.setText(severaCustomData.severaCase.name);
        } else {
            caseView.setText(context.getString(R.string.visma_blue_spinner_nothing_chosen));
            return;
        }

        if (severaCustomData.severaCase.tasks != null
                && severaCustomData.severaCase.tasks.size() > 1) {
            for (SeveraCustomData.Task task : severaCustomData.severaCase.tasks) {

                // The root case task is always at hierarchy level 0 so we need to skip it, since we
                // use the case name instead of case's task name
                if (task.hierarchyLevel > 0) {
                    TextView textView = new TextView(context);

                    textView.setText(task.name);
                    textView.setTextSize(16);
                    textView.setTextColor(ContextCompat.getColor(context,
                            getCustomDataTextColor()));
                    textView.setSingleLine(true);
                    textView.setPadding(0, 0, 0, 12);
                    textView.setEllipsize(TextUtils.TruncateAt.END);

                    casesAndPhases.addView(textView);
                }
            }
        }
    }

    public View.OnClickListener getCasePhaseOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mSeveraActionListener != null) {
                    mSeveraActionListener.onCasePhaseClick();
                }
            }
        };
    }

    public void updateCasePhaseSelection() {
        notifyPropertyChanged(BR.updateCasesPhases);
    }

    public Severa getCustomSeveraData() {
        return mCustomSeveraData;
    }
}
