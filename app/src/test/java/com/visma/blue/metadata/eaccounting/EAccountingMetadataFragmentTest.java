package com.visma.blue.metadata.eaccounting;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.custom.CustomMetaData;
import com.visma.blue.metadata.BaseMetaDataFragmentTest;
import com.visma.blue.metadata.MetadataFragment;
import com.visma.blue.misc.AppId;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.OnlinePhotoType;
import com.visma.blue.qr.QrActivity;
import com.visma.common.util.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

public class EAccountingMetadataFragmentTest extends BaseMetaDataFragmentTest {

    @Override
    public void setup() {
        BlueConfig.setAppId(AppId.EACCOUNTING.getValue());
        super.setup();
    }

    private void makeQrScan(int type) {
        Intent intent = mMetaDataActivity.getIntent();
        if (type == OnlinePhotoType.INVOICE.getValue()) {
            intent.putExtra(QrActivity.ACTIVITY_RESULT_CODE_QR_MESSAGE,
                    CustomMetaData.getInvoiceQrString());
        } else if (type == OnlinePhotoType.RECEIPT.getValue()) {
            intent.putExtra(QrActivity.ACTIVITY_RESULT_CODE_QR_MESSAGE,
                    CustomMetaData.getReceiptQrString());
        }

        mMetaDataFragment.onActivityResult(MetadataFragment.ACTIVITY_REQUEST_CODE_RETRIEVE_QR_CODE,
                Activity.RESULT_OK, intent);
    }

    private void checkDataAfterQrSCan(int type) {
        View rootView = mMetaDataFragment.getView();
        assertEquals("Type from Qr is not equal to type in object!",
                type, mOnlineMetaData.type);

        if (type == OnlinePhotoType.RECEIPT.getValue()) {
            assertEquals("Payment date from Qr is not equal to payment date in object!",
                    ((TextView) rootView.findViewById(R.id
                            .blue_fragment_payment_date_data)).getText().toString(),
                    SimpleDateFormat.getDateInstance().format(mOnlineMetaData.paymentDate));
        }

        if (type == OnlinePhotoType.INVOICE.getValue()) {
            assertEquals("Org. number from Qr is not equal to org. number in object!",
                    ((TextView) rootView.findViewById(R.id
                            .activity_more_information_organisation_number)).getText().toString(),
                    mOnlineMetaData.organisationNumber);
            assertEquals("Ref. number from Qr is not equal to org. number in object!",
                    ((TextView) rootView.findViewById(R.id
                            .activity_more_information_reference_number)).getText().toString(),
                    mOnlineMetaData.referenceNumber);
            assertEquals("Invoice date from Qr is not equal to invoice date in object!",
                    ((TextView) rootView.findViewById(R.id
                            .activity_more_information_layout_invoice_date_data)).getText()
                            .toString(),
                    SimpleDateFormat.getDateInstance().format(mOnlineMetaData.invoiceDate));
            assertEquals("Due date from Qr is not equal to due date in object!",
                    ((TextView) rootView.findViewById(R.id
                            .activity_more_information_layout_due_date_data)).getText().toString(),
                    SimpleDateFormat.getDateInstance().format(mOnlineMetaData.dueDate));
        }

        assertEquals("Org. name from Qr is not equal to org. name in object!",
                ((TextView) rootView.findViewById(R.id
                        .activity_more_information_name)).getText().toString(),
                mOnlineMetaData.name);
        assertEquals("Due amount from Qr is not equal to due amount in object!",
                Util.parseDouble(((TextView) rootView.findViewById(R.id
                        .activity_more_information_due_amount)).getText().toString()),
                mOnlineMetaData.dueAmount);
        assertEquals("Total vat amount from Qr is not equal to total vat amount in object!",
                Util.parseDouble(((TextView) rootView.findViewById(R.id
                        .activity_more_information_total_vat_amount)).getText().toString()),
                mOnlineMetaData.totalVatAmount);
        assertEquals("High vat amount from Qr is not equal to high vat amount in object!",
                Util.parseDouble(((TextView) rootView.findViewById(R.id
                        .activity_more_information_high_vat_amount)).getText().toString()),
                mOnlineMetaData.highVatAmount);
        assertEquals("Middle vat amount from Qr is not equal to middle vat amount in object!",
                Util.parseDouble(((TextView) rootView.findViewById(R.id
                        .activity_more_information_middle_vat_amount)).getText().toString()),
                mOnlineMetaData.middleVatAmount);
        assertEquals("Low vat amount from Qr is not equal to low vat amount in object!",
                Util.parseDouble(((TextView) rootView.findViewById(R.id
                        .activity_more_information_low_vat_amount)).getText().toString()),
                mOnlineMetaData.lowVatAmount);
        assertEquals("Currency from Qr is not equal to currency in object!",
                ((Spinner) rootView.findViewById(R.id.activity_more_information_currency_spinner))
                        .getSelectedItem().toString(),
                mOnlineMetaData.currency);
    }

    private void checkTypeChangeToReceipt() {
        View rootView = mMetaDataFragment.getView();
        TextView typeView = (TextView) rootView.findViewById(R.id.typeTextView);
        assertEquals("Wrong type. Should be Receipt!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.RECEIPT
                        .getValue())),
                typeView.getText().toString());
        assertFalse("Payed layout should be disabled!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payed).isEnabled());

        CheckBox payedCheck = (CheckBox) rootView.findViewById(R.id
                .blue_fragment_metadata_payed_checkbox);
        assertTrue("Payed check box should be checked!", payedCheck.isChecked());

        assertTrue("Payment date layout should be enabled!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payment_date).isEnabled());
        assertTrue("Payment date layout should be clickable!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payment_date)
                        .isClickable());
        assertNotNull("Payment date object should not be null!", mOnlineMetaData.paymentDate);

        assertTrue("Photo layout should always be clickable!", rootView
                .findViewById(R.id.fragment_metadata_layout_image).isClickable());
        assertTrue("More information layout should always be clickable!", rootView
                .findViewById(R.id.fragment_metadata_layout_more_information).isClickable());

        //More information fields
        assertEquals("Organisation number layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_organisation_number)
                        .getVisibility(),
                View.GONE);
        assertEquals("Organisation name layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_information_name)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Reference number  layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_reference_number)
                        .getVisibility(),
                View.GONE);
        assertEquals("Invoice date layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_invoice_date)
                        .getVisibility(),
                View.GONE);
        assertEquals("Due date layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_due_date)
                        .getVisibility(),
                View.GONE);
        assertEquals("Due amount layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_due_amount).getVisibility(),
                View.VISIBLE);
        assertEquals("Total vat layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_total_vat)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("High vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_high_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("middle vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_middle_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("Low vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_low_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("Currency layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_currency_spinner)
                        .getVisibility(),
                View.VISIBLE);

        assertEquals("Comment field should  always be visible!",
                rootView.findViewById(R.id.commentTextView).getVisibility(),
                View.VISIBLE);
    }

    private void checkTypeChangeToInvoiceWhenPaidChecked() {
        View rootView = mMetaDataFragment.getView();
        TextView typeView = (TextView) rootView.findViewById(R.id.typeTextView);
        assertEquals("Wrong type. Should be Invoice!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.INVOICE
                        .getValue())),
                typeView.getText().toString());

        assertTrue("Payed layout should be enabled!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payed).isEnabled());

        CheckBox payedCheck = (CheckBox) rootView.findViewById(R.id
                .blue_fragment_metadata_payed_checkbox);
        assertTrue("Payed check box should be enabled!", payedCheck.isEnabled());
        assertTrue("Payed check box should be checked!", payedCheck.isChecked());

        assertTrue("Payment date layout should  not be disabled!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payment_date).isEnabled());

        assertNotNull("Payment date object should not be null!", mOnlineMetaData.paymentDate);

        assertTrue("Photo layout should always be clickable!", rootView
                .findViewById(R.id.fragment_metadata_layout_image).isClickable());
        assertTrue("More information layout should always be clickable!", rootView
                .findViewById(R.id.fragment_metadata_layout_more_information).isClickable());
        //More information fields
        assertEquals("Organisation number layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_organisation_number)
                        .getVisibility(),
                View.GONE);
        assertEquals("Organisation name layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_information_name)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Reference number  layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_reference_number)
                        .getVisibility(),
                View.GONE);
        assertEquals("Invoice date layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_invoice_date)
                        .getVisibility(),
                View.GONE);
        assertEquals("Due date layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_due_date)
                        .getVisibility(),
                View.GONE);
        assertEquals("Due amount layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_due_amount).getVisibility(),
                View.VISIBLE);
        assertEquals("Total vat layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_total_vat)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("High vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_high_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("middle vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_middle_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("Low vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_low_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("Currency layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_currency_spinner)
                        .getVisibility(),
                View.VISIBLE);

        assertEquals("Comment field should  always be visible!",
                rootView.findViewById(R.id.commentTextView).getVisibility(),
                View.VISIBLE);
    }

    private void checkTypeChangeToInvoiceWhenPaidNotChecked() {
        View rootView = mMetaDataFragment.getView();
        TextView typeView = (TextView) rootView.findViewById(R.id.typeTextView);
        assertEquals("Wrong type. Should be Invoice!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.INVOICE
                        .getValue())),
                typeView.getText().toString());

        assertTrue("Payed layout should be enabled!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payed).isEnabled());

        CheckBox payedCheck = (CheckBox) rootView.findViewById(R.id
                .blue_fragment_metadata_payed_checkbox);
        assertTrue("Payed check box should be enabled!", payedCheck.isEnabled());
        assertFalse("Payed check box should be checked!", payedCheck.isChecked());

        assertFalse("Payment date layout should be disabled!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payment_date).isEnabled());

        assertNull("Payment date object should be null!", mOnlineMetaData.paymentDate);

        assertTrue("Photo layout should always be clickable!", rootView
                .findViewById(R.id.fragment_metadata_layout_image).isClickable());
        assertTrue("More information layout should always be clickable!", rootView
                .findViewById(R.id.fragment_metadata_layout_more_information).isClickable());

        //More information fields
        assertEquals("Organisation number layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_organisation_number)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Organisation name layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_information_name)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Reference number  layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_reference_number)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Invoice date layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_invoice_date)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Due date layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_due_date)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Due amount layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_due_amount).getVisibility(),
                View.VISIBLE);
        assertEquals("Total vat layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_total_vat)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("High vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_high_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("middle vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_middle_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("Low vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_low_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("Currency layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_currency_spinner)
                        .getVisibility(),
                View.VISIBLE);

        assertEquals("Comment field should  always be visible!",
                rootView.findViewById(R.id.commentTextView).getVisibility(),
                View.VISIBLE);
    }

    @Test
    public void testIfFragmentNotNull() {
        assertNotNull("Failed creating eAccountingMetaDataFragment", mMetaDataFragment);
    }

    @Test
    public void testViewCreation() {
        View rootView = mMetaDataFragment.getView();
        assertNotNull("Root view in eAccountingMetadaFragment is null!",
                rootView);
        assertNotNull("Information text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.metaDataInformationTextView));
        assertNotNull("Type layout in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.layoutType));
        assertNotNull("Type text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.typeTextView));
        assertNotNull("Payed layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payed));
        assertNotNull("Payed layout checkbox in eAccountingMetadaFragment  is null!",
                rootView.findViewById(R.id.blue_fragment_metadata_payed_checkbox));
        assertNotNull("Payment layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payment_date));
        assertNotNull("Payment text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.blue_fragment_payment_date_data));
        assertNotNull("Image layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.fragment_metadata_layout_image));
        assertNotNull("Image file name text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.fragment_metadata_image_filename));
        assertNotNull("More information layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.fragment_metadata_layout_more_information));
        assertNotNull("More information expand iamge view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id
                        .fragment_metadata_layout_more_information_expander_image));
        assertNotNull("More information text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.fragment_metadata_layout_more_information_text));
        assertNotNull("More information expand layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id
                        .fragment_metadata_layout_more_information_expandable_view));
        assertNotNull("Organisation number layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_organisation_number));
        assertNotNull("Organisation number text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_organisation_number));
        assertNotNull("Name layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_information_name));
        assertNotNull("Name text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_name));
        assertNotNull("Reference number layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_reference_number));
        assertNotNull("Reference number text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_reference_number));
        assertNotNull("Invoice date layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_invoice_date));
        assertNotNull("Invoice date text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_invoice_date_data));
        assertNotNull("Due date layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_due_date));
        assertNotNull("Due layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_due_date_data));
        assertNotNull("Due amount text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_due_amount));
        assertNotNull("Total vat layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_total_vat));
        assertNotNull("Total vat amount text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_total_vat_amount));
        assertNotNull("High vat layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_high_vat));
        assertNotNull("High vat amount text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_high_vat_amount));
        assertNotNull("Middle vat layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_middle_vat));
        assertNotNull("Middle vat amount text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_middle_vat_amount));
        assertNotNull("Low vat layout view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_layout_low_vat));
        assertNotNull("Low vat amount text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_low_vat_amount));
        assertNotNull("Currency spinner view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.activity_more_information_currency_spinner));
        assertNotNull("Comment text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.commentTextView));
    }

    @Test
    public void testViewDisablingWhenPhotoLocked() {
        mOnlineMetaData.canDelete = false;
        recreateActivity();
        View rootView = mMetaDataFragment.getView();
        assertFalse("Type layout is not disabled!",
                rootView.findViewById(R.id.layoutType).isEnabled());
        assertFalse("Payed layout view is not disabled!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payed).isEnabled());
        assertFalse("Payment layout is not disabled!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payment_date).isEnabled());
        assertTrue("Image layout is disabled!",
                rootView.findViewById(R.id.fragment_metadata_layout_image).isEnabled());
        assertTrue("More information layout is disabled!",
                rootView.findViewById(R.id.fragment_metadata_layout_more_information).isEnabled());
        assertTrue("More information expand layout view is disabled!",
                rootView.findViewById(R.id
                        .fragment_metadata_layout_more_information_expandable_view).isEnabled());
        assertFalse("Organisation number field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_organisation_number)
                        .isEnabled());
        assertFalse("Name field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_name).isEnabled());
        assertFalse("Reference number field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_reference_number)
                        .isEnabled());
        assertFalse("Invoice date layout is not disabled!",
                rootView.findViewById(R.id.activity_more_information_layout_invoice_date)
                        .isEnabled());
        assertFalse("Due date layout is not disabled!",
                rootView.findViewById(R.id.activity_more_information_layout_due_date)
                        .isEnabled());
        assertFalse("Due amount field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_due_amount)
                        .isEnabled());
        assertFalse("Total vat field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_total_vat_amount)
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
        assertFalse("Currency spinner is not disabled!",
                rootView.findViewById(R.id.activity_more_information_currency_spinner)
                        .isEnabled());
        assertFalse("Comment text view in eAccountingMetadaFragment is null!",
                rootView.findViewById(R.id.commentTextView).isEnabled());
        assertFalse("Comment view is not disabled!",
                rootView.findViewById(R.id.commentTextView).isEnabled());
    }

    @Test
    public void testInvoiceTypeSetupWithoutPayedCheck() {
        mOnlineMetaData.type = OnlinePhotoType.INVOICE.getValue();
        recreateActivity();
        checkTypeChangeToInvoiceWhenPaidNotChecked();
    }

    @Test
    public void testInvoiceTypeSetupWithPayedChecked() {
        mOnlineMetaData.type = OnlinePhotoType.INVOICE.getValue();
        mOnlineMetaData.isPaid = true;
        mOnlineMetaData.paymentDate = new Date();
        recreateActivity();
        checkTypeChangeToInvoiceWhenPaidChecked();
    }

    @Test
    public void testUnclassifiedTypeSetup() {
        mOnlineMetaData.type = OnlinePhotoType.UNKNOWN.getValue();
        mOnlineMetaData.isPaid = null;
        recreateActivity();
        View rootView = mMetaDataFragment.getView();
        TextView typeView = (TextView) rootView.findViewById(R.id.typeTextView);
        assertEquals("Wrong type. Should be Unknown!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.UNKNOWN
                        .getValue())),
                typeView.getText().toString());

        assertEquals("Payed layout should be gone!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payed).getVisibility(),
                View.GONE);

        assertTrue("Payment date layout should be enabled!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payment_date).isEnabled());
        assertTrue("Payment date layout should be clickable!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payment_date)
                        .isClickable());
        assertNull("Payment date object should  be null!", mOnlineMetaData.paymentDate);

        assertTrue("Photo layout should always be clickable!", rootView
                .findViewById(R.id.fragment_metadata_layout_image).isClickable());
        assertTrue("More information layout should always be clickable!", rootView
                .findViewById(R.id.fragment_metadata_layout_more_information).isClickable());

        //More information fields
        assertEquals("Organisation number layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_organisation_number)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Organisation name layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_information_name)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Reference number  layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_reference_number)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Invoice date layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_invoice_date)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Due date layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_due_date)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("Due amount layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_due_amount).getVisibility(),
                View.VISIBLE);
        assertEquals("Total vat layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_layout_total_vat)
                        .getVisibility(),
                View.VISIBLE);
        assertEquals("High vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_high_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("middle vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_middle_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("Low vat layout should be gone!",
                rootView.findViewById(R.id.activity_more_information_layout_low_vat)
                        .getVisibility(),
                View.GONE);
        assertEquals("Currency layout should be visible!",
                rootView.findViewById(R.id.activity_more_information_currency_spinner)
                        .getVisibility(),
                View.VISIBLE);

        assertEquals("Comment field should  always be visible!",
                rootView.findViewById(R.id.commentTextView).getVisibility(),
                View.VISIBLE);
    }

    @Test
    public void testReceiptTypeSetup() {
        mOnlineMetaData.type = OnlinePhotoType.RECEIPT.getValue();
        mOnlineMetaData.isPaid = true;
        mOnlineMetaData.paymentDate = new Date();
        recreateActivity();
        checkTypeChangeToReceipt();
    }

    @Test
    public void testTypeChangeToInvoice() {
        mOnlineMetaData.type = OnlinePhotoType.RECEIPT.getValue();
        mOnlineMetaData.isPaid = true;
        mOnlineMetaData.paymentDate = new Date();
        recreateActivity();
        changeType(OnlinePhotoType.INVOICE.getValue());
        checkTypeChangeToInvoiceWhenPaidChecked();
    }

    @Test
    public void testTypeChangeToReceiptWithPayedCheck() {
        mOnlineMetaData.type = OnlinePhotoType.INVOICE.getValue();
        mOnlineMetaData.isPaid = true;
        mOnlineMetaData.paymentDate = new Date();
        recreateActivity();
        changeType(OnlinePhotoType.RECEIPT.getValue());
        checkTypeChangeToReceipt();
    }

    @Test
    public void testTypeChangeToReceiptWithoutPayedCheck() {
        mOnlineMetaData.type = OnlinePhotoType.INVOICE.getValue();
        recreateActivity();
        assertNull("Payment date object should be null!", mOnlineMetaData.paymentDate);
        assertFalse("Is paid date parameter should be false!", mOnlineMetaData.isPaid);

        changeType(OnlinePhotoType.RECEIPT.getValue());
        checkTypeChangeToReceipt();
    }

    @Test
    public void testTypeChangeToReceiptFromUnclassified() {
        mOnlineMetaData.type = OnlinePhotoType.UNKNOWN.getValue();
        mOnlineMetaData.isPaid = null;
        recreateActivity();
        assertNull("Payment date object should be null!", mOnlineMetaData.paymentDate);
        assertNull("Is paid parameter should be null!", mOnlineMetaData.isPaid);

        changeType(OnlinePhotoType.RECEIPT.getValue());
        checkTypeChangeToReceipt();
    }

    @Test
    public void testTypeChangeToInvoiceFromUnclassified() {
        mOnlineMetaData.type = OnlinePhotoType.UNKNOWN.getValue();
        mOnlineMetaData.isPaid = null;
        recreateActivity();
        assertNull("Payment date object should be null!", mOnlineMetaData.paymentDate);
        assertNull("Is paid parameter should be null!", mOnlineMetaData.isPaid);

        changeType(OnlinePhotoType.INVOICE.getValue());
        checkTypeChangeToInvoiceWhenPaidNotChecked();
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
    public void testInvoiceDateSetup() {
        mOnlineMetaData.invoiceDate = new Date();
        recreateActivity();
        TextView invoiceDateView = (TextView) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_layout_invoice_date_data);
        assertEquals("Wrong invoice date set!",
                SimpleDateFormat.getDateInstance()
                        .format(mOnlineMetaData.invoiceDate), invoiceDateView.getText().toString());
    }

    @Test
    public void testEmptyInvoiceDateSetup() {
        mOnlineMetaData.paymentDate = null;
        recreateActivity();
        TextView invoiceDateView = (TextView) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_layout_invoice_date_data);
        assertEquals("Invoice date should be empty!",
                "", invoiceDateView.getText().toString());
    }

    @Test
    public void testDueDateSetup() {
        mOnlineMetaData.dueDate = new Date();
        recreateActivity();
        TextView invoiceDateView = (TextView) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_layout_due_date_data);
        assertEquals("Wrong due date set!",
                SimpleDateFormat.getDateInstance()
                        .format(mOnlineMetaData.dueDate), invoiceDateView.getText().toString());
    }

    @Test
    public void testEmptyDueDateSetup() {
        mOnlineMetaData.dueDate = null;
        recreateActivity();
        TextView dueDateView = (TextView) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_layout_due_date_data);
        assertEquals("Due date should be empty!",
                "", dueDateView.getText().toString());
    }

    @Test
    public void testPaymentPickerShow() {
        checkDatePicker("PaymentDatePickerDialog",
                R.id.blue_fragment_metadata_layout_payment_date);
    }

    @Test
    public void testInvoicetPickerShow() {
        checkDatePicker("InvoiceDatePickerDialog",
                R.id.activity_more_information_layout_invoice_date);
    }

    @Test
    public void testDuePickerShow() {
        checkDatePicker("DueDatePickerDialog",
                R.id.activity_more_information_layout_due_date);
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
    public void testInvoiceMaxDate() {
        mOnlineMetaData.invoiceDate = new Date();
        View invoiceDateLayout = mMetaDataFragment.getView()
                .findViewById(R.id.activity_more_information_layout_invoice_date);
        invoiceDateLayout.performClick();

        FragmentManager fm = mMetaDataActivity.getFragmentManager();
        DatePickerDialog dateDialog =
                (DatePickerDialog) fm.findFragmentByTag("InvoiceDatePickerDialog");
        assertEquals("Date dialog max value can't be more then current date!",
                SimpleDateFormat.getDateInstance().format(mOnlineMetaData.invoiceDate),
                SimpleDateFormat.getDateInstance().format(dateDialog.getMaxDate().getTime()));
    }

    @Test
    public void testDueMaxDate() {
        mOnlineMetaData.dueDate = new Date();
        View paymentDateLayout = mMetaDataFragment.getView()
                .findViewById(R.id.activity_more_information_layout_due_date);
        paymentDateLayout.performClick();

        FragmentManager fm = mMetaDataActivity.getFragmentManager();
        DatePickerDialog dateDialog =
                (DatePickerDialog) fm.findFragmentByTag("DueDatePickerDialog");
        assertNull("Due date dialog should not have max date value limit!",
                dateDialog.getMaxDate());
    }

    @Test
    public void testPaymentPickerMinDate() {
        checkDatePickerMinDate("PaymentDatePickerDialog",
                R.id.blue_fragment_metadata_layout_payment_date);
    }

    @Test
    public void testInvoicePickerMinDate() {
        checkDatePickerMinDate("InvoiceDatePickerDialog",
                R.id.activity_more_information_layout_invoice_date);
    }

    @Test
    public void testDuePickerMinDate() {
        checkDatePickerMinDate("DueDatePickerDialog",
                R.id.activity_more_information_layout_due_date);
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
    public void testOrganisationNumber() {
        mOnlineMetaData.organisationNumber = "1000";
        recreateActivity();

        EditText organisationNumView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_organisation_number);

        assertEquals("Wrong organisation field input type!",
                InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_NUMBER_FLAG_SIGNED
                        | InputType.TYPE_CLASS_NUMBER,
                organisationNumView.getInputType());

        assertEquals("Set wrong organisation number!",
                mOnlineMetaData.organisationNumber,
                organisationNumView.getText().toString());

        organisationNumView.setText("1001");
        assertEquals("Organisation number value not changed in object!",
                organisationNumView.getText().toString(),
                mOnlineMetaData.organisationNumber);
    }

    @Test
    public void testOrganisationName() {
        mOnlineMetaData.name = "Test name setup";
        recreateActivity();

        EditText nameView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_name);

        assertEquals("Wrong name field input type!",
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT,
                nameView.getInputType());

        assertEquals("Set wrong name!",
                mOnlineMetaData.name,
                nameView.getText().toString());

        nameView.setText("Change name field text");
        assertEquals("Name value not changed in object!",
                nameView.getText().toString(),
                mOnlineMetaData.name);
    }

    @Test
    public void testReferenceNumber() {
        mOnlineMetaData.referenceNumber = "Test reference number setup";
        recreateActivity();

        EditText referenceView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_reference_number);

        assertEquals("Wrong reference number field input type!",
                InputType.TYPE_CLASS_TEXT,
                referenceView.getInputType());

        assertEquals("Set wrong reference number!",
                mOnlineMetaData.referenceNumber,
                referenceView.getText().toString());

        referenceView.setText("Change reference field text");
        assertEquals("Reference number value not changed in object!",
                referenceView.getText().toString(),
                mOnlineMetaData.referenceNumber);
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
    public void testTotalVatAmount() {
        mOnlineMetaData.totalVatAmount = 55.55;
        recreateActivity();

        EditText totalVatAmountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_total_vat_amount);

        assertEquals("Wrong due amount field input type!",
                InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_CLASS_NUMBER,
                totalVatAmountView.getInputType());

        assertEquals("Set wrong due amount!",
                Util.getFormattedNumberString(mOnlineMetaData.totalVatAmount),
                totalVatAmountView.getText().toString());

        totalVatAmountView.requestFocus();
        totalVatAmountView.setText("99999");
        assertEquals("Due amount should not have been formatted before loosing focus!",
                "99999",
                totalVatAmountView.getText().toString());
        assertTrue("Due amount value not changed in object!",
                99999 == mOnlineMetaData.totalVatAmount);

        totalVatAmountView.clearFocus();
        assertEquals("Due Amount should have been formatted!",
                Util.getFormattedNumberString(mOnlineMetaData.totalVatAmount),
                totalVatAmountView.getText().toString());
    }

    @Test
    public void testHighVatAmount() {
        mOnlineMetaData.highVatAmount = 55.55;
        recreateActivity();

        EditText highVatAmountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_high_vat_amount);

        assertEquals("Wrong High vat amount field input type!",
                InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_CLASS_NUMBER,
                highVatAmountView.getInputType());

        assertEquals("Set wrong High vat amount!",
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

        EditText highVatAmountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_middle_vat_amount);

        assertEquals("Wrong middle vat amount field input type!",
                InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_CLASS_NUMBER,
                highVatAmountView.getInputType());

        assertEquals("Set wrong middle vat amount!",
                Util.getFormattedNumberString(mOnlineMetaData.middleVatAmount),
                highVatAmountView.getText().toString());

        highVatAmountView.requestFocus();
        highVatAmountView.setText("99999");
        assertEquals("Middle vat amount should not have been formatted before loosing focus!",
                "99999",
                highVatAmountView.getText().toString());
        assertTrue("Middle vat amount value not changed in object!",
                99999 == mOnlineMetaData.middleVatAmount);

        highVatAmountView.clearFocus();
        assertEquals("Middle vat amount should have been formatted!",
                Util.getFormattedNumberString(mOnlineMetaData.middleVatAmount),
                highVatAmountView.getText().toString());
    }

    @Test
    public void testLowVatAmount() {
        mOnlineMetaData.lowVatAmount = 55.55;
        recreateActivity();

        EditText highVatAmountView = (EditText) mMetaDataFragment
                .getView().findViewById(R.id.activity_more_information_low_vat_amount);

        assertEquals("Wrong low vat amount field input type!",
                InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_CLASS_NUMBER,
                highVatAmountView.getInputType());

        assertEquals("Set wrong low vat amount!",
                Util.getFormattedNumberString(mOnlineMetaData.lowVatAmount),
                highVatAmountView.getText().toString());

        highVatAmountView.requestFocus();
        highVatAmountView.setText("99999");
        assertEquals("Low vat amount should not have been formatted before loosing focus!",
                "99999",
                highVatAmountView.getText().toString());
        assertTrue("Low vat amount value not changed in object!",
                99999 == mOnlineMetaData.lowVatAmount);

        highVatAmountView.clearFocus();
        assertEquals("Low vat amount should have been formatted!",
                Util.getFormattedNumberString(mOnlineMetaData.lowVatAmount),
                highVatAmountView.getText().toString());
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

    private void checkQrDataSetup(int type) {
        View rootView = mMetaDataFragment.getView();
        assertFalse("Type layout is not disabled!",
                rootView.findViewById(R.id.layoutType).isEnabled());

        if (type == OnlinePhotoType.INVOICE.getValue()) {
            assertTrue("Payed layout view Should not be disabled!",
                    rootView.findViewById(R.id.blue_fragment_metadata_layout_payed).isEnabled());
        } else {
            assertFalse("Payed layout view should be disabled!",
                    rootView.findViewById(R.id.blue_fragment_metadata_layout_payed).isEnabled());
        }

        assertFalse("Payment layout should be disabled!",
                rootView.findViewById(R.id.blue_fragment_metadata_layout_payment_date).isEnabled());
        assertTrue("Image layout is disabled!",
                rootView.findViewById(R.id.fragment_metadata_layout_image).isEnabled());
        assertTrue("More information layout is disabled!",
                rootView.findViewById(R.id.fragment_metadata_layout_more_information).isEnabled());
        assertTrue("More information expand layout view is disabled!",
                rootView.findViewById(R.id
                        .fragment_metadata_layout_more_information_expandable_view).isEnabled());
        assertFalse("Organisation number field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_organisation_number)
                        .isEnabled());
        assertFalse("Name field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_name).isEnabled());
        assertFalse("Reference number field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_reference_number)
                        .isEnabled());
        assertFalse("Invoice date layout is not disabled!",
                rootView.findViewById(R.id.activity_more_information_layout_invoice_date)
                        .isEnabled());
        assertFalse("Due date layout is not disabled!",
                rootView.findViewById(R.id.activity_more_information_layout_due_date)
                        .isEnabled());
        assertFalse("Due amount field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_due_amount)
                        .isEnabled());
        assertFalse("Total vat field is not disabled!",
                rootView.findViewById(R.id.activity_more_information_total_vat_amount)
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
        assertFalse("Currency spinner is not disabled!",
                rootView.findViewById(R.id.activity_more_information_currency_spinner)
                        .isEnabled());
        assertTrue("Comment view Should not be disabled!",
                rootView.findViewById(R.id.commentTextView).isEnabled());
    }

    @Test
    public void testInvoiceQrDataSetup() {
        mOnlineMetaData.usingQrString = CustomMetaData.getInvoiceQrString();
        recreateActivity();
        checkQrDataSetup(OnlinePhotoType.INVOICE.getValue());
    }

    @Test
    public void testReceiptQrDataSetup() {
        mOnlineMetaData.usingQrString = CustomMetaData.getReceiptQrString();
        recreateActivity();
        checkQrDataSetup(OnlinePhotoType.RECEIPT.getValue());
    }

    @Test
    public void testInvoiceQrDataScan() {
        mOnlineMetaData.usingQrString = null;
        recreateActivity();
        makeQrScan(OnlinePhotoType.INVOICE.getValue());
        checkDataAfterQrSCan(OnlinePhotoType.INVOICE.getValue());
    }

    @Test
    public void testReceiptQrDataScan() {
        mOnlineMetaData.usingQrString = null;
        recreateActivity();
        makeQrScan(OnlinePhotoType.RECEIPT.getValue());
        checkDataAfterQrSCan(OnlinePhotoType.RECEIPT.getValue());
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
    public void testTitleChange() {
        mOnlineMetaData.type = OnlinePhotoType.RECEIPT.getValue();
        recreateActivity();
        changeType(OnlinePhotoType.INVOICE.getValue());
        assertEquals("Wrong title changed. Should be Invoice!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.INVOICE
                        .getValue())),
                mMetaDataActivity.getTitle().toString());
        changeType(OnlinePhotoType.RECEIPT.getValue());
        assertEquals("Wrong title changed. Should be Receipt!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.RECEIPT
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

        assertEquals("Send data menu item should be visible!",
                true, menu.findItem(R.id.blue_fragment_metadata_menu_send).isVisible());
        assertEquals("Delete data menu item should be visible!",
                true, menu.findItem(R.id.blue_fragment_metadata_menu_discard).isVisible());
        assertEquals("QR code menu should be visible!",
                true, menu.findItem(R.id.blue_fragment_metadata_menu_qr_code).isVisible());

        mOnlineMetaData.canDelete = false;
        recreateActivity();
        Menu menu2 = shadowOf(mMetaDataActivity).getOptionsMenu();

        assertEquals("Send data menu item should be visible!",
                false, menu2.findItem(R.id.blue_fragment_metadata_menu_send).isVisible());
        assertEquals("Delete data menu item should be visible!",
                false, menu2.findItem(R.id.blue_fragment_metadata_menu_discard).isVisible());
        assertEquals("QR code menu should be hidden!",
                false, menu2.findItem(R.id.blue_fragment_metadata_menu_qr_code).isVisible());
    }
}
