package com.visma.blue.metadata;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.util.ArraySet;
import android.view.View;

import com.visma.blue.BaseActivityTest;
import com.visma.blue.BuildConfig;
import com.visma.blue.custom.CustomApplication;
import com.visma.blue.custom.CustomMetaData;
import com.visma.blue.network.containers.OnlineMetaData;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;
import org.robolectric.util.ReflectionHelpers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Locale;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public abstract class BaseMetaDataFragmentTest extends BaseActivityTest{

    protected Fragment mMetaDataFragment;
    protected OnlineMetaData mOnlineMetaData;
    protected ActivityController<MetadataActivity> mActivityController;
    protected MetadataActivity mMetaDataActivity;

    @Before
    public void setup() {
        Locale.setDefault(Locale.US);
        mOnlineMetaData = CustomMetaData.getDefaultMetaData(RuntimeEnvironment.application);
        recreateActivity();
    }

    @After
    public void tearDown(){
        cleanUpCurrentActivityIfNeeded();

        mOnlineMetaData = null;
        resetWindowManager();
    }

    private void cleanUpCurrentActivityIfNeeded() {
        if (mActivityController != null) {
            mActivityController.pause().stop().destroy();

            mActivityController = null;
            mMetaDataActivity = null;
            mMetaDataFragment = null;
        }
    }

    protected void recreateActivity() {
        cleanUpCurrentActivityIfNeeded();

        mActivityController = Robolectric.buildActivity(MetadataActivity.class)
                .withIntent(CustomMetaData.getDefaultIntent(mOnlineMetaData));

        mMetaDataActivity = mActivityController
                .create()
                .start()
                .resume()
                .visible()
                .get();
        mMetaDataFragment =  mMetaDataActivity.getSupportFragmentManager()
                .findFragmentById(android.R.id.content);
    }

    protected void checkDatePicker(String pickerTag, @IdRes int layoutId) {
        //when
        View datePickerLayout = mMetaDataFragment.getView().findViewById(layoutId);
        datePickerLayout.performClick();

        FragmentManager fm = mMetaDataActivity.getFragmentManager();
        DatePickerDialog dateDialog = (DatePickerDialog) fm.findFragmentByTag(pickerTag);

        //then
        assertNotNull("Date dialog not found by tag " + pickerTag + "! ", dateDialog);
    }

    protected void checkTimePicker(String pickerTag, @IdRes int layoutId) {
        //when
        View timePickerLayout = mMetaDataFragment.getView().findViewById(layoutId);
        timePickerLayout.performClick();

        FragmentManager fm = mMetaDataActivity.getFragmentManager();
        TimePickerDialog dateDialog = (TimePickerDialog) fm.findFragmentByTag(pickerTag);

        //then
        assertNotNull("Time dialog not found by tag " + pickerTag + "! ", dateDialog);
    }

    protected void checkDatePickerMinDate(String pickerTag, @IdRes int layoutId) {
        //when
        View datePickerLayout = mMetaDataFragment.getView().findViewById(layoutId);
        datePickerLayout.performClick();

        final GregorianCalendar minDateCalendar = new GregorianCalendar();
        minDateCalendar.set(2000, Calendar.JANUARY, 1);

        FragmentManager fm = mMetaDataActivity.getFragmentManager();
        DatePickerDialog dateDialog = (DatePickerDialog) fm.findFragmentByTag(pickerTag);

        //then
        assertEquals("Date dialog min value should be January 1, 2000!",
                SimpleDateFormat.getDateInstance().format(minDateCalendar.getTime()),
                SimpleDateFormat.getDateInstance().format(dateDialog.getMinDate().getTime()));
    }

    protected void changeType(int type) {
        Intent intent = mMetaDataActivity.getIntent();
        intent.putExtra(TypePickerDialog.EXTRA_TYPE, type);
        mMetaDataFragment.onActivityResult(MetadataFragment.REQUEST_CODE_TYPE, Activity.RESULT_OK,
                intent);
    }
}
