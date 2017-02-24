package com.visma.blue.metadata;

import android.app.FragmentManager;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.visma.blue.BR;
import com.visma.blue.R;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.common.DecimalKeyListener;
import com.visma.common.SimpleTextWatcher;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class BaseMetaDataViewModel extends BaseObservable {

    private boolean mHasBitmap;
    protected Context mContext;
    protected OnlineMetaData mOnlineMetaData;
    protected DatePickerDialog mDatePickerDialog;
    protected FragmentManager mFragmentManager;

    public BaseMetaDataViewModel(FragmentManager fragmentManager,
                                 OnlineMetaData onlineMetaData,
                                 Context context,
                                 boolean hasBitmap) {
        mOnlineMetaData = onlineMetaData;
        mFragmentManager = fragmentManager;
        mContext = context;
        mHasBitmap = hasBitmap;
        mDatePickerDialog = createDateDialog(mOnlineMetaData.date);

    }

    public String getInformationText() {
        if (!mOnlineMetaData.canDelete) { // The image has been "connected", lock down changes.
            return mContext.getString(R.string.visma_blue_label_photo_is_connected);
        } else if (mHasBitmap) { // A new document not yet uploaded. Or a locally stored document.
            return mContext.getString(R.string.visma_blue_label_photo_is_a_copy);
        } else if (!mOnlineMetaData.isVerified ) { // A document that has been emailed
            return mContext.getString(R.string.visma_blue_not_verified_document);
        } else { // This is where we end up when looking at a saved document that is on the server
            return "";
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
                mOnlineMetaData.comment = editable.toString(); // for this field we use "" instead of null
            }
        };
    }

    @Bindable
    public String getDate() {
        return SimpleDateFormat.getDateInstance().format(mOnlineMetaData.date);
    }

    @Bindable
    public int getType() {
        return VismaUtils.getTypeTextId(mOnlineMetaData.type);
    }

    protected Date getCustomDate(int year, int monthOfYear, int dayOfMonth) {
        GregorianCalendar utcCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        utcCalendar.clear();
        utcCalendar.set(year, monthOfYear, dayOfMonth);
        return utcCalendar.getTime();
    }

    private DatePickerDialog createDateDialog(Date initialDate) {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(initialDate);

        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
                                          int dayOfMonth) {
                        mOnlineMetaData.date = getCustomDate(year, monthOfYear, dayOfMonth);
                        notifyPropertyChanged(BR.date);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        final GregorianCalendar maxDateCalendar = new GregorianCalendar();
        datePickerDialog.setMaxDate(maxDateCalendar);
        final GregorianCalendar minDateCalendar = new GregorianCalendar();
        minDateCalendar.set(2000, Calendar.JANUARY, 1);
        datePickerDialog.setMinDate(minDateCalendar);

        return datePickerDialog;
    }

    public View.OnClickListener getDateOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String tag = "DatePickerDialog";

                synchronized (mDatePickerDialog) {
                    if (mFragmentManager.findFragmentByTag(tag) != null) {
                        return;
                    }

                    mDatePickerDialog.show(mFragmentManager, tag);
                    // If we want to query the fragment manager for the fragment we need the fragment to be added immediately
                    mFragmentManager.executePendingTransactions();
                }
            }
        };
    }


    public DecimalKeyListener getDecimalKeyListener() {
        return new DecimalKeyListener();
    }
}