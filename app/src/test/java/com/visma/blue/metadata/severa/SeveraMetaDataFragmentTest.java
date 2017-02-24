package com.visma.blue.metadata.severa;

import android.app.FragmentManager;
import android.content.Intent;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.custom.CustomMetaData;
import com.visma.blue.metadata.BaseMetaDataFragmentTest;
import com.visma.blue.misc.AppId;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.containers.ExpenseCustomData;
import com.visma.blue.network.containers.SeveraCustomData;
import com.visma.blue.network.requests.customdata.Expense;
import com.visma.common.util.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

public class SeveraMetaDataFragmentTest extends BaseMetaDataFragmentTest {
    @Override
    public void setup() {
        BlueConfig.setAppId(AppId.SEVERA.getValue());
        VismaUtils.isRunningUnitTest = true;
        super.setup();
    }

    private void setCasePhase(SeveraCustomData.Case customPhaseCase) {
        Intent intent = mMetaDataActivity.getIntent();
        intent.putExtra(SeveraCaseActivity.SAVED_DATA, customPhaseCase);
        mMetaDataFragment.onActivityResult(SeveraMetadataFragment
                .SEVERA_CASE_ACTIVITY_COMMUNICATION,
                SeveraMetadataFragment.SEVERA_CASE_ACTIVITY_COMMUNICATION,
                intent);
    }

    @Test
    public void shouldCreateSeveraMetadataFragment() {
        assertNotNull("Failed creating SeveraMetaDataFragment", mMetaDataFragment);
    }

    @Test
    public void shouldCreateViews() {
        View rootView = mMetaDataFragment.getView();
        assertNotNull("Root view in SeveraMetaDataFragment is null!",
                rootView);
        assertNotNull("Comment view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.descriptionTextView));
        assertNotNull("Image layout view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.fragment_metadata_layout_image));
        assertNotNull("Image file name text view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.fragment_metadata_image_filename));
        assertNotNull("Cases/Phases layout view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.layoutCasesAndPhases));
        assertNotNull("Expense type spinner in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.expenseTypeSpinner));
        assertNotNull("Remaining info layout view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.remainingInfo));
        assertNotNull("Currency text view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.currencyTextView));
        assertNotNull("Amount view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.amountTextView));
        assertNotNull("Vat layout view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.layoutVat));
        assertNotNull("Vat spinner view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.vatSpinner));
        assertNotNull("Date and time layout view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.layoutDatesAndTimes));
        assertNotNull("Start date layout view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.layoutStartDate));
        assertNotNull("Start date text view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.textViewStartDate));
        assertNotNull("End date layout view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.layoutEndDate));
        assertNotNull("End date text view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.textViewEndDate));
        assertNotNull("Time layout view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.layoutBothTimes));
        assertNotNull("Start time layout view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.layoutStartTime));
        assertNotNull("Start time text view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.textViewStartTime));
        assertNotNull("End time layout view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.layoutEndTime));
        assertNotNull("End time text view in SeveraMetaDataFragment is null!",
                rootView.findViewById(R.id.textViewEndTime));
    }

    @Test
    public void shouldDisableViewWhenPhotoLocked() {
        //given
        mOnlineMetaData.canDelete = false;

        //when
        recreateActivity();

        //then
        View rootView = mMetaDataFragment.getView();
        assertFalse("Comment view should be disabled!",
                rootView.findViewById(R.id.descriptionTextView).isEnabled());
        assertTrue("Image layout should not be disabled!",
                rootView.findViewById(R.id.fragment_metadata_layout_image).isEnabled());
        assertFalse("Cases/Phases layout should be disabled!",
                rootView.findViewById(R.id.layoutCasesAndPhases).isEnabled());
        assertFalse("Expense type spinner should be disabled disabled!",
                rootView.findViewById(R.id.expenseTypeSpinner).isEnabled());
        assertFalse("Amount view should be disabled!",
                rootView.findViewById(R.id.amountTextView).isEnabled());
        assertFalse("Vat spinner should be disabled!",
                rootView.findViewById(R.id.vatSpinner).isEnabled());
        assertFalse("Start date layout should be disabled!",
                rootView.findViewById(R.id.layoutStartDate).isEnabled());
        assertFalse("End date layout should be disabled!",
                rootView.findViewById(R.id.layoutEndDate).isEnabled());
        assertFalse("End time layout should be disabled!",
                rootView.findViewById(R.id.layoutEndTime).isEnabled());
        assertFalse("Start time layout should be disabled!",
                rootView.findViewById(R.id.layoutStartTime).isEnabled());
    }

    @Test
    public void shouldSetComment() {
        //given
        mOnlineMetaData.comment = "Test comment";

        //when
        recreateActivity();

        //then
        TextView commentView = (TextView) mMetaDataFragment.getView().findViewById(R.id
                .descriptionTextView);
        assertEquals("Wrong comment set!",
                mOnlineMetaData.comment, commentView.getText().toString());
    }

    @Test
    public void shouldUpdateCommentValueInData() {
        //given
        mOnlineMetaData.comment = "Test comment";

        //when
        recreateActivity();

        //then
        TextView commentView = (TextView) mMetaDataFragment.getView().findViewById(R.id
                .descriptionTextView);
        commentView.setText("Testing comment change");
        assertEquals("Wrong comment set!",
                commentView.getText().toString(), mOnlineMetaData.comment);
    }

    @Test
    public void shouldSetAmountWhenExpenseTypeSelected() {
        //given
        mOnlineMetaData = CustomMetaData.getMetaDataWithExpenseType(RuntimeEnvironment.application);
        mOnlineMetaData.dueAmount = 5555.55;

        //when
        recreateActivity();
        EditText amountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.amountTextView);

        //then
        assertEquals("Set wrong due amount!",
                Util.getFormattedNumberString(mOnlineMetaData.dueAmount),
                amountView.getText().toString());
    }

    @Test
    public void shouldNotSetAmountWithoutExpenseTypeSelected() {
        //given
        mOnlineMetaData.dueAmount = 5555.55;

        //when
        recreateActivity();

        //then
        assertNull("Due amount should be set to null!",
                Util.getFormattedNumberString(mOnlineMetaData.dueAmount));
    }

    @Test
    public void shouldUpdateAmountValueInObject() {
        //given
        mOnlineMetaData.dueAmount = null;

        //when
        recreateActivity();
        EditText amountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.amountTextView);
        amountView.setText("99999.99");

        //then
        assertTrue("Amount value not changed in object!",
                99999.99 == mOnlineMetaData.dueAmount);
    }

    @Test
    public void testDueAmount() {
        //given
        mOnlineMetaData.dueAmount = null;

        //when
        recreateActivity();
        EditText amountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.amountTextView);

        //then
        amountView.requestFocus();
        amountView.setText("99999");
        amountView.clearFocus();
        assertEquals("Due Amount should have been formatted!",
                Util.getFormattedNumberString(mOnlineMetaData.dueAmount),
                amountView.getText().toString());
    }

    @Test
    public void amountInputTypeShouldBeNumber() {
        //given
        EditText amountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.amountTextView);

        //then
        assertEquals("Wrong due amount field input type!",
                InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_CLASS_NUMBER,
                amountView.getInputType());
    }

    @Test
    public void shouldFindStartDatePickerByTag() {
        //given
        checkDatePicker("StartDatePickerDialog", R.id.layoutStartDate);
    }

    @Test
    public void shouldFindEndDatePickerByTag() {
        //given
        checkDatePicker("EndDatePickerDialog", R.id.layoutEndDate);
    }

    @Test
    public void shouldFindStartTimePickerByTag() {
        //given
        checkTimePicker("StartTimePickerDialog", R.id.layoutStartTime);
    }

    @Test
    public void shouldFindEndTimePickerByTag() {
        //given
        checkTimePicker("EndTimePickerDialog", R.id.layoutEndTime);
    }

    @Test
    public void photoViewShouldBeClicableWhenPhotoNotConnectedToSevera() {
        //given
        mOnlineMetaData.canDelete = true;

        //when
        recreateActivity();
        View photoLayout = mMetaDataFragment
                .getView().findViewById(R.id.fragment_metadata_layout_image);

        //then
        assertEquals("Photo layout should be always clickable!", true, photoLayout.isClickable());
    }

    @Test
    public void photoViewShouldBeClicableWhenPhotoConnectedToSevera() {
        //given
        mOnlineMetaData.canDelete = false;

        //when
        recreateActivity();
        View photoLayout = mMetaDataFragment
                .getView().findViewById(R.id.fragment_metadata_layout_image);

        //then
        assertEquals("Photo layout should be always clickable!", true, photoLayout.isClickable());
    }

    @Test
    public void checkDefaultStartDateMinDate() {
        //given
        checkDatePickerMinDate("StartDatePickerDialog", R.id.layoutStartDate);
    }

    @Test
    public void remainingInfoViewShouldBeGone() {
        //given
        mOnlineMetaData.severaCustomData = null;

        //when
        recreateActivity();

        //then
        assertTrue("Remaining info view should be gone!",
                mMetaDataFragment.getView()
                        .findViewById(R.id.remainingInfo).getVisibility() == View.GONE);
    }

    @Test
    public void remainingInfoViewShouldBeVisible() {
        //given
        mOnlineMetaData = CustomMetaData.getMetaDataWithExpenseType(RuntimeEnvironment.application);

        //when
        recreateActivity();

        //then
        assertTrue("Remaining info view should be visible!",
                mMetaDataFragment.getView()
                        .findViewById(R.id.remainingInfo).getVisibility() == View.VISIBLE);
    }

    @Test
    public void shouldShowOnlyStartDateView() {
        //given
        mOnlineMetaData = CustomMetaData.getMetaDataWithExpenseType(RuntimeEnvironment.application);
        mOnlineMetaData.severaCustomData.product.useStartAndEndTime = false;
        //when
        recreateActivity();

        //then
        assertTrue("Start date layout should be visible!",
                mMetaDataFragment.getView()
                        .findViewById(R.id.layoutStartDate).getVisibility() == View.VISIBLE);
        assertTrue("End date layout should be gone!",
                mMetaDataFragment.getView()
                        .findViewById(R.id.layoutEndDate).getVisibility() == View.GONE);
        assertTrue("Start  and end time layout should be gone!",
                mMetaDataFragment.getView()
                        .findViewById(R.id.layoutBothTimes).getVisibility() == View.GONE);
    }

    @Test
    public void shouldShowStartEndDateAndTimeViews() {
        //given
        mOnlineMetaData = CustomMetaData.getMetaDataWithExpenseType(RuntimeEnvironment.application);
        mOnlineMetaData.severaCustomData.product.useStartAndEndTime = true;
        //when
        recreateActivity();

        //then
        assertTrue("Start date layout should be visible!",
                mMetaDataFragment.getView()
                        .findViewById(R.id.layoutStartDate).getVisibility() == View.VISIBLE);
        assertTrue("End date layout should be visible!",
                mMetaDataFragment.getView()
                        .findViewById(R.id.layoutEndDate).getVisibility() == View.VISIBLE);
        assertTrue("Start and end time layout should be visible!",
                mMetaDataFragment.getView()
                        .findViewById(R.id.layoutBothTimes).getVisibility() == View.VISIBLE);
    }

    @Test
    public void shouldSetEndDateMinDate() {
        //given
        mOnlineMetaData = CustomMetaData.getMetaDataWithExpenseType(RuntimeEnvironment.application);
        mOnlineMetaData.severaCustomData.product.useStartAndEndTime = true;
        mOnlineMetaData.severaCustomData.startDateUtc = new Date();
        //when
        recreateActivity();
        View endDatePickerLayout = mMetaDataFragment.getView().findViewById(R.id.layoutEndDate);
        endDatePickerLayout.performClick();
        FragmentManager fm = mMetaDataActivity.getFragmentManager();
        DatePickerDialog endDateDialog = (DatePickerDialog)
                fm.findFragmentByTag("EndDatePickerDialog");

        //then
        assertEquals("Set wrong end date min date limit!",
                SimpleDateFormat.getDateInstance().format(mOnlineMetaData.severaCustomData
                        .startDateUtc),
                SimpleDateFormat.getDateInstance().format(endDateDialog.getMinDate().getTime()));
    }


    @Test
    public void shouldSetStartDateMaxDate() {
        //given
        mOnlineMetaData = CustomMetaData.getMetaDataWithExpenseType(RuntimeEnvironment.application);
        mOnlineMetaData.severaCustomData.product.useStartAndEndTime = true;
        mOnlineMetaData.severaCustomData.endDateUtc = new Date();

        //when
        recreateActivity();
        View endDatePickerLayout = mMetaDataFragment.getView().findViewById(R.id.layoutStartDate);
        endDatePickerLayout.performClick();
        FragmentManager fm = mMetaDataActivity.getFragmentManager();
        DatePickerDialog endDateDialog = (DatePickerDialog)
                fm.findFragmentByTag("StartDatePickerDialog");

        //then
        assertEquals("Set wrong start date max date limit!",
                SimpleDateFormat.getDateInstance().format(mOnlineMetaData.severaCustomData
                        .endDateUtc),
                SimpleDateFormat.getDateInstance().format(endDateDialog.getMaxDate().getTime()));
    }


    @Test
    public void shouldCreateOptionsMenu() {
        Menu menu = shadowOf(mMetaDataActivity).getOptionsMenu();
        assertNotNull("Menu is not created !", menu);
        assertNotNull("Send data menu item not found!",
                menu.findItem(R.id.blue_fragment_metadata_menu_send));
        assertNotNull("Delete data menu item not found!",
                menu.findItem(R.id.blue_fragment_metadata_menu_discard));
        assertNotNull("QR code menu item not found!",
                menu.findItem(R.id.blue_fragment_metadata_menu_qr_code));
    }

    @Test
    public void shouldHideQRCodeMenu() {
        //given
        mOnlineMetaData.canDelete = true;

        //when
        recreateActivity();
        Menu menu = shadowOf(mMetaDataActivity).getOptionsMenu();

        //then
        assertTrue("Send data menu item should be visible!",
                menu.findItem(R.id.blue_fragment_metadata_menu_send).isVisible());
        assertTrue("Delete data menu item should be visible!",
                menu.findItem(R.id.blue_fragment_metadata_menu_discard).isVisible());
        assertFalse("QR code menu should not be visible!",
                menu.findItem(R.id.blue_fragment_metadata_menu_qr_code).isVisible());


    }

    @Test
    public void shouldHideAllMenuItems() {
        //given
        mOnlineMetaData.canDelete = false;

        //when
        recreateActivity();
        Menu menu2 = shadowOf(mMetaDataActivity).getOptionsMenu();

        //then
        assertFalse("Send data menu item should be visible!",
                menu2.findItem(R.id.blue_fragment_metadata_menu_send).isVisible());
        assertFalse("Delete data menu item should be visible!",
                menu2.findItem(R.id.blue_fragment_metadata_menu_discard).isVisible());
        assertFalse("QR code menu should be hidden!",
                menu2.findItem(R.id.blue_fragment_metadata_menu_qr_code).isVisible());
    }

    @Test
    public void shouldIncreaseCasesPhasesViewCount() {
        // given
        mOnlineMetaData.severaCustomData = new SeveraCustomData();

        // when
        setCasePhase(CustomMetaData.getCustomCasePhase());

        // then
        LinearLayout casePhaseLayout = (LinearLayout) mMetaDataFragment.getView()
                .findViewById(R.id.layoutCasesAndPhases);
        assertEquals("Cases and phases count should have increased!",
                CustomMetaData.CASES_PHASES_COUNT,
                casePhaseLayout.getChildCount());
    }

    @Test
    public void shouldReturnTrueWhenVerifyingDataWithNullSeveraCustomData() {
        // given
        mOnlineMetaData.severaCustomData = null;

        // when
        recreateActivity();

        // then
        assertTrue("Data verification should have succeeded!",
                ((SeveraMetadataFragment)mMetaDataFragment).verifyMetaData());
    }

    @Test
    public void shouldNotSetValueToDueAmountWhenVerifyingData() {
        // given
        mOnlineMetaData.dueAmount = null;

        // when
        recreateActivity();
        ((SeveraMetadataFragment)mMetaDataFragment).verifyMetaData();

        // then
        assertEquals("Due amount should be null!", null,  mOnlineMetaData.dueAmount);
    }

    @Test
    public void shouldReturnFalseWhenVerifyingCustomDataWithWrongSetTime() {
        // given
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2100);
        mOnlineMetaData = CustomMetaData.getMetaDataWithExpenseType(RuntimeEnvironment.application);

        // when
        recreateActivity();
        mOnlineMetaData.severaCustomData.endDateUtc = new Date();
        mOnlineMetaData.severaCustomData.startDateUtc = new Date(calendar.getTimeInMillis());

        // then
        assertFalse("Should return false when verifying data",
                ((SeveraMetadataFragment) mMetaDataFragment).verifyMetaData());
    }

    @Test
    public void shouldSetEndTimeWhenVerifyingData() {
        // given
        mOnlineMetaData = CustomMetaData.getMetaDataWithExpenseType(RuntimeEnvironment.application);
        mOnlineMetaData.severaCustomData.product.useStartAndEndTime = true;
        // when
        recreateActivity();
        ((SeveraMetadataFragment) mMetaDataFragment).verifyMetaData();
        // then
        assertEquals("Should return false when verifying data",
                mOnlineMetaData.severaCustomData.startDateUtc, mOnlineMetaData.severaCustomData
                        .endDateUtc);
    }
}
