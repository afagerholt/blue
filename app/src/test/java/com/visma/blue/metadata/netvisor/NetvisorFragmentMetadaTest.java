package com.visma.blue.metadata.netvisor;

import android.app.FragmentManager;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.metadata.BaseMetaDataFragmentTest;
import com.visma.blue.misc.AppId;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.OnlinePhotoType;
import com.visma.common.util.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

public class NetvisorFragmentMetadaTest extends BaseMetaDataFragmentTest{

    @Override
    public void setup() {
        BlueConfig.setAppId(AppId.NETVISOR.getValue());
        super.setup();
    }

    @Test
    public void testIfFragmentNotNull() {
        assertNotNull("Failed creating VismaOnlineMetaDataFragment", mMetaDataFragment);
    }

    @Test
    public void testViewCreation() {
        View rootView = mMetaDataFragment.getView();
        assertNotNull("Root view in NetvisorMetadaFragment is null!",
                rootView);
        assertNotNull("Information text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.metaDataInformationTextView));
        assertNotNull("Type layout in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.layoutType));
        assertNotNull("Type text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.typeTextView));
        assertNotNull("Payment layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payment_date));
        assertNotNull("Payment text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.blue_fragment_payment_date_data));
        assertNotNull("Image layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.fragment_metadata_layout_image));
        assertNotNull("Image file name text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.fragment_metadata_image_filename));
        assertNotNull("More information layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.fragment_metadata_layout_more_information));
        assertNotNull("More information expand iamge view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id
                        .fragment_metadata_layout_more_information_expander_image));
        assertNotNull("More information text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.fragment_metadata_layout_more_information_text));
        assertNotNull("More information expand layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id
                        .fragment_metadata_layout_more_information_expandable_view));
        assertNotNull("Organisation number layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_organisation_number));
        assertNotNull("Organisation number text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_organisation_number));
        assertNotNull("Reference number layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_reference_number));
        assertNotNull("Reference number text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_reference_number));
        assertNotNull("Invoice date layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_invoice_date));
        assertNotNull("Invoice date text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_invoice_date_data));
        assertNotNull("Due date layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_due_date));
        assertNotNull("Due layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_due_date_data));
        assertNotNull("Due amount text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_due_amount));
        assertNotNull("High vat layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_high_vat));
        assertNotNull("High vat amount text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_high_vat_amount));
        assertNotNull("Middle vat layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_middle_vat));
        assertNotNull("Middle vat amount text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_middle_vat_amount));
        assertNotNull("Low vat layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_low_vat));
        assertNotNull("Low vat amount text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_low_vat_amount));
        assertNotNull("Zero vat layout view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_zero_vat));
        assertNotNull("Zero vat amount text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_zero_vat_amount));
        assertNotNull("Currency spinner view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_currency_spinner));
        assertNotNull("Custom spinner layout in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.blue_fragment_metadata_custom_backend_data_layout));
        assertNotNull("Comment text view in NetvisorMetadaFragment is null!",
                rootView.findViewById(R.id.commentTextView));
    }

    @Test
    public void testViewDisablingWhenPhotoLocked() {
        mOnlineMetaData.canDelete = false;
        recreateActivity();
        View rootView = mMetaDataFragment.getView();
        assertFalse("Type layout should not be clickable!",
                rootView.findViewById(R.id.layoutType).isClickable());
        assertFalse("Payment layout is not disabled!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payment_date).isEnabled());
        assertTrue("Image layout is disabled!",
                rootView.findViewById(R.id.fragment_metadata_layout_image).isEnabled());
        assertTrue("More information layout is disabled!",
                rootView.findViewById(R.id.fragment_metadata_layout_more_information).isEnabled());
        assertTrue("More information expand layout view is disabled!",
                rootView.findViewById(R.id
                        .fragment_metadata_layout_more_information_expandable_view).isEnabled());
        assertFalse("Due amount field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_due_amount)
                        .isEnabled());
        assertFalse("High vat amount field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_high_vat_amount)
                        .isEnabled());
        assertFalse("Middle vat amount field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_middle_vat_amount)
                        .isEnabled());
        assertFalse("Low vat amount field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_low_vat_amount)
                        .isEnabled());
        assertFalse("Zero vat amount field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_zero_vat_amount)
                        .isEnabled());
        assertFalse("Currency spinner is not disabled!",
                rootView.findViewById(R.id.activity_more_information_currency_spinner)
                        .isEnabled());
        assertFalse("Comment view is not disabled!",
                rootView.findViewById(R.id.commentTextView).isEnabled());
    }

    @Test
    public void testReceiptTypeSetup() {
        mOnlineMetaData.type = OnlinePhotoType.RECEIPT.getValue();
        recreateActivity();
        TextView typeView = (TextView) mMetaDataFragment.getView().findViewById(R.id.typeTextView);
        assertEquals("Wrong type. Should be Receipt!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.RECEIPT
                        .getValue())),
                typeView.getText().toString());
    }

    @Test
    public void testCommentSetup() {
        mOnlineMetaData.comment = "Test comment";
        recreateActivity();
        TextView commentView = (TextView) mMetaDataFragment.getView().findViewById(R.id
                .commentTextView);
        assertEquals("Wrong comment set!",
                mOnlineMetaData.comment, commentView.getText().toString());
    }

    @Test
    public void testCommentChange() {
        mOnlineMetaData.comment = "Test comment";
        recreateActivity();
        TextView commentView = (TextView) mMetaDataFragment.getView().findViewById(R.id
                .commentTextView);
        commentView.setText("Testing comment change");
        assertEquals("Wrong comment set!",
                commentView.getText().toString(), mOnlineMetaData.comment);
    }

    @Test
    public void testPaymentDateSetup() {
        mOnlineMetaData.paymentDate = new Date();
        recreateActivity();
        TextView paymentDateView = (TextView) mMetaDataFragment.getView()
                .findViewById(R.id.blue_fragment_payment_date_data);
        assertEquals("Wrong payment date set!",
                SimpleDateFormat.getDateInstance().format(mOnlineMetaData.paymentDate),
                paymentDateView.getText().toString());
    }

    @Test
    public void testEmptyPaymentDateSetup() {
        mOnlineMetaData.paymentDate = null;
        recreateActivity();
        TextView paymentDateView = (TextView) mMetaDataFragment
                .getView().findViewById(R.id.blue_fragment_payment_date_data);
        assertEquals("Payment date should be empty!",
                "", paymentDateView.getText().toString());
    }

    @Test
    public void testPaymentPickerShow() {
        checkDatePicker("PaymentDatePickerDialog",
                R.id.blue_fragment_metadata_layout_payment_date);
    }

    @Test
    public void testPaymentMaxDate() {
        mOnlineMetaData.paymentDate = new Date();
        View paymentDateLayout = mMetaDataFragment.getView()
                .findViewById(R.id.blue_fragment_metadata_layout_payment_date);
        paymentDateLayout.performClick();

        FragmentManager fm = mMetaDataActivity.getFragmentManager();
        DatePickerDialog dateDialog =
                (DatePickerDialog) fm.findFragmentByTag("PaymentDatePickerDialog");
        assertEquals("Date dialog max value can't be more then current date!",
                SimpleDateFormat.getDateInstance().format(mOnlineMetaData.paymentDate),
                SimpleDateFormat.getDateInstance().format(dateDialog.getMaxDate().getTime()));
    }

    @Test
    public void testPaymentPickerMinDate() {
        checkDatePickerMinDate("PaymentDatePickerDialog",
                R.id.blue_fragment_metadata_layout_payment_date);
    }

    @Test
    public void testPhotoClick() {
        mOnlineMetaData.canDelete = true;
        recreateActivity();
        View photoLayout = mMetaDataFragment
                .getView().findViewById(R.id.fragment_metadata_layout_image);
        assertEquals("Photo layout should be always clickable!", true, photoLayout.isClickable());

        mOnlineMetaData.canDelete = false;
        recreateActivity();
        View photoLayout2 = mMetaDataFragment
                .getView().findViewById(R.id.fragment_metadata_layout_image);
        assertEquals("Photo layout should be always clickable!", true, photoLayout2.isClickable());
    }

    @Test
    public void testDueAmount() {
        mOnlineMetaData.dueAmount = 5555.55;
        recreateActivity();

        EditText dueAmountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_due_amount);

        assertEquals("Wrong due amount field input type!",
                InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_CLASS_NUMBER,
                dueAmountView.getInputType());

        assertEquals("Set wrong due amount!",
                Util.getFormattedNumberString(mOnlineMetaData.dueAmount),
                dueAmountView.getText().toString());

        dueAmountView.requestFocus();
        dueAmountView.setText("99999");
        assertEquals("Due amount should not have been formatted before loosing focus!",
                "99999",
                dueAmountView.getText().toString());
        assertTrue("Due amount value not changed in object!",
                99999 == mOnlineMetaData.dueAmount);

        dueAmountView.clearFocus();
        assertEquals("Due Amount should have been formatted!",
                Util.getFormattedNumberString(mOnlineMetaData.dueAmount),
                dueAmountView.getText().toString());
    }

    @Test
    public void testZeroVatAmount() {
        mOnlineMetaData.zeroVatAmount = 55.55;
        recreateActivity();

        EditText zeroVatAmountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_zero_vat_amount);

        assertEquals("Wrong zero vat amount field input type!",
                InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_CLASS_NUMBER,
                zeroVatAmountView.getInputType());

        assertEquals("Set wrong zero vat amount!",
                Util.getFormattedNumberString(mOnlineMetaData.zeroVatAmount),
                zeroVatAmountView.getText().toString());

        zeroVatAmountView.requestFocus();
        zeroVatAmountView.setText("99999");
        assertEquals("Zero vat amount should not have been formatted before loosing focus!",
                "99999",
                zeroVatAmountView.getText().toString());
        assertTrue("Zero vat amount value not changed in object!",
                99999 == mOnlineMetaData.zeroVatAmount);

        zeroVatAmountView.clearFocus();
        assertEquals("Zero vat amount should have been formatted!",
                Util.getFormattedNumberString(mOnlineMetaData.zeroVatAmount),
                zeroVatAmountView.getText().toString());
    }

    @Test
    public void testHighVatAmount() {
        mOnlineMetaData.highVatAmount = 55.55;
        recreateActivity();

        EditText highVatAmountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_high_vat_amount);

        assertEquals("Wrong high vat amount field input type!",
                InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_CLASS_NUMBER,
                highVatAmountView.getInputType());

        assertEquals("Set wrong high vat amount!",
                Util.getFormattedNumberString(mOnlineMetaData.highVatAmount),
                highVatAmountView.getText().toString());

        highVatAmountView.requestFocus();
        highVatAmountView.setText("99999");
        assertEquals("High vat amount should not have been formatted before loosing focus!",
                "99999",
                highVatAmountView.getText().toString());
        assertTrue("High vat amount value not changed in object!",
                99999 == mOnlineMetaData.highVatAmount);

        highVatAmountView.clearFocus();
        assertEquals("High vat amount should have been formatted!",
                Util.getFormattedNumberString(mOnlineMetaData.highVatAmount),
                highVatAmountView.getText().toString());
    }

    @Test
    public void testMiddleVatAmount() {
        mOnlineMetaData.middleVatAmount = 55.55;
        recreateActivity();

        EditText middleVatAmountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_middle_vat_amount);

        assertEquals("Wrong middle vat amount field input type!",
                InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_CLASS_NUMBER,
                middleVatAmountView.getInputType());

        assertEquals("Set wrong middle vat amount!",
                Util.getFormattedNumberString(mOnlineMetaData.middleVatAmount),
                middleVatAmountView.getText().toString());

        middleVatAmountView.requestFocus();
        middleVatAmountView.setText("99999");
        assertEquals("Middle vat amount should not have been formatted before loosing focus!",
                "99999",
                middleVatAmountView.getText().toString());
        assertTrue("Middle vat amount value not changed in object!",
                99999 == mOnlineMetaData.middleVatAmount);

        middleVatAmountView.clearFocus();
        assertEquals("Middle vat amount should have been formatted!",
                Util.getFormattedNumberString(mOnlineMetaData.middleVatAmount),
                middleVatAmountView.getText().toString());
    }

    @Test
    public void testLowVatAmount() {
        mOnlineMetaData.lowVatAmount = 55.55;
        recreateActivity();

        EditText lowVatAmountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_low_vat_amount);

        assertEquals("Wrong low vat amount field input type!",
                InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_CLASS_NUMBER,
                lowVatAmountView.getInputType());

        assertEquals("Set wrong low vat amount!",
                Util.getFormattedNumberString(mOnlineMetaData.lowVatAmount),
                lowVatAmountView.getText().toString());

        lowVatAmountView.requestFocus();
        lowVatAmountView.setText("99999");
        assertEquals("Low vat amount should not have been formatted before loosing focus!",
                "99999",
                lowVatAmountView.getText().toString());
        assertTrue("Low vat amount value not changed in object!",
                99999 == mOnlineMetaData.lowVatAmount);

        lowVatAmountView.clearFocus();
        assertEquals("Low vat amount should have been formatted!",
                Util.getFormattedNumberString(mOnlineMetaData.lowVatAmount),
                lowVatAmountView.getText().toString());
    }

    @Test
    public void testCurrencyNotSelectedSetup() {
        Spinner currencySpinner = (Spinner) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_currency_spinner);

        assertTrue("Currency spinner can't be empty!",
                currencySpinner.getAdapter().getCount() > 0);
        assertEquals("First currency item must be empty!",
                "",
                currencySpinner.getItemAtPosition(0));
        assertFalse("Second currency item must not be empty!",
                currencySpinner.getItemAtPosition(1).equals(""));
    }

    @Test
    public void testCurrencySelectedSetup() {
        mOnlineMetaData.currency = "EUR";
        recreateActivity();

        Spinner currencySpinner = (Spinner) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_currency_spinner);

        assertEquals("Selected currency item is not selected in Spinner!",
                mOnlineMetaData.currency,
                currencySpinner.getSelectedItem().toString());
    }

    @Test
    public void testCurrencyChange() {
        Spinner currencySpinner = (Spinner) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_currency_spinner);

        String secondCurrencyItem = (String) currencySpinner.getItemAtPosition(1);
        currencySpinner.setSelection(1);
        assertEquals("Selected currency is not equal to currency in object!",
                secondCurrencyItem,
                mOnlineMetaData.currency);
    }

    @Test
    public void testTitleSetup() {
        mOnlineMetaData.type = OnlinePhotoType.RECEIPT.getValue();
        recreateActivity();
        assertEquals("Title is not equal to photo type!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.RECEIPT
                        .getValue())),
                mMetaDataActivity.getTitle().toString());

        mOnlineMetaData.type = OnlinePhotoType.INVOICE.getValue();
        recreateActivity();
        assertEquals("Title is not equal to photo type!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.INVOICE
                        .getValue())),
                mMetaDataActivity.getTitle().toString());
    }

    @Test
    public void testOptionsMenuInitialize() {
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
    public void testOptionsMenuVisibility() {
        mOnlineMetaData.canDelete = true;
        recreateActivity();
        Menu menu = shadowOf(mMetaDataActivity).getOptionsMenu();

        assertTrue("Send data menu item should be visible!",
                 menu.findItem(R.id.blue_fragment_metadata_menu_send).isVisible());
        assertTrue("Delete data menu item should be visible!",
                 menu.findItem(R.id.blue_fragment_metadata_menu_discard).isVisible());
        assertFalse("QR code menu should not be visible!",
                 menu.findItem(R.id.blue_fragment_metadata_menu_qr_code).isVisible());

        mOnlineMetaData.canDelete = false;
        recreateActivity();
        Menu menu2 = shadowOf(mMetaDataActivity).getOptionsMenu();

        assertFalse("Send data menu item should be visible!",
                 menu2.findItem(R.id.blue_fragment_metadata_menu_send).isVisible());
        assertFalse("Delete data menu item should be visible!",
                 menu2.findItem(R.id.blue_fragment_metadata_menu_discard).isVisible());
        assertFalse("QR code menu should be hidden!",
                 menu2.findItem(R.id.blue_fragment_metadata_menu_qr_code).isVisible());
    }
}
