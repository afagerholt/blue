package com.visma.blue.metadata.mamut;

import android.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.metadata.BaseMetaDataFragmentTest;
import com.visma.blue.misc.AppId;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.OnlinePhotoType;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

public class MamutMetaDataFragmentTest extends BaseMetaDataFragmentTest {

    @Override
    public void setup() {
        BlueConfig.setAppId(AppId.MAMUT.getValue());
        super.setup();
    }

    @Test
    public void testIfFragmentNotNull() {
        assertNotNull("Failed creating MamutMetaDataFragment", mMetaDataFragment);
    }

    @Test
    public void testViewCreation() {
        View rootView = mMetaDataFragment.getView();
        assertNotNull("Root view in MamutMetadaFragment  is null!",
                rootView);
        assertNotNull("Information text view in MamutMetadaFragment is null!",
                rootView.findViewById(R.id.metaDataInformationTextView));
        assertNotNull("Type layout in MamutMetadaFragment  is null!",
                rootView.findViewById(R.id.layoutType));
        assertNotNull("Type text view in MamutMetadaFragment  is null!",
                rootView.findViewById(R.id.typeTextView));
        assertNotNull("Image layout view in MamutMetadaFragment  is null!",
                rootView.findViewById(R.id.fragment_metadata_layout_image));
        assertNotNull("Date layout view in MamutMetadaFragment  is null!",
                rootView.findViewById(R.id.layoutDate));
        assertNotNull("Date text view in MamutMetadaFragment  is null!",
                rootView.findViewById(R.id.textViewDate));
        assertNotNull("Comment text view in MamutMetadaFragment  is null!",
                rootView.findViewById(R.id.commentTextView));
    }

    @Test
    public void testViewDisabling() {
        mOnlineMetaData.canDelete = false;
        recreateActivity();
        View rootView = mMetaDataFragment.getView();
        assertEquals("Type layout is not disabled!",
                false, rootView.findViewById(R.id.layoutType).isEnabled());
        assertEquals("Image layout should not be disabled!",
                true, rootView.findViewById(R.id.fragment_metadata_layout_image).isEnabled());
        assertEquals("Date layout is not disabled!",
                false, rootView.findViewById(R.id.layoutDate).isEnabled());
        assertEquals("Comment view is not disabled!",
                false, rootView.findViewById(R.id.commentTextView).isEnabled());
    }

    @Test
    public void testInvoiceTypeSetup() {
        mOnlineMetaData.type = OnlinePhotoType.INVOICE.getValue();
        recreateActivity();
        TextView typeView = (TextView) mMetaDataFragment.getView().findViewById(R.id.typeTextView);
        assertEquals("Wrong type. Should be Invoice!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.INVOICE
                        .getValue())),
                typeView.getText().toString());
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
    public void testTypeChange() {
        mOnlineMetaData.type = OnlinePhotoType.RECEIPT.getValue();
        recreateActivity();
        changeType(OnlinePhotoType.INVOICE.getValue());
        TextView typeView = (TextView) mMetaDataFragment.getView().findViewById(R.id.typeTextView);
        assertEquals("Wrong type changed. Should be Invoice!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.INVOICE
                        .getValue())),
                typeView.getText().toString());
        assertEquals("Type did not change in object! Should be Invoice!",
                OnlinePhotoType.INVOICE.getValue(),
                mOnlineMetaData.type);

        changeType(OnlinePhotoType.RECEIPT.getValue());
        assertEquals("Wrong type changed. Should be Receipt!",
                mMetaDataFragment.getString(VismaUtils.getTypeTextId(OnlinePhotoType.RECEIPT
                        .getValue())),
                typeView.getText().toString());
        assertEquals("Type did not change in object! Should be Receipt!",
                OnlinePhotoType.RECEIPT.getValue(),
                mOnlineMetaData.type);
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
    public void testDateSetup() {
        mOnlineMetaData.date = new Date();
        recreateActivity();
        TextView dateView = (TextView) mMetaDataFragment.getView().findViewById(R.id.textViewDate);
        assertEquals("Wrong date set!",
                SimpleDateFormat.getDateInstance().format(mOnlineMetaData.date), dateView.getText
                        ().toString());
    }

    @Test
    public void testDatePickerShow() {
        View dateLayout = mMetaDataFragment.getView().findViewById(R.id.layoutDate);
        dateLayout.performClick();

        FragmentManager fm = mMetaDataActivity.getFragmentManager();
        DatePickerDialog dateDialog = (DatePickerDialog) fm.findFragmentByTag("DatePickerDialog");
        assertNotNull("Date dialog not found by tag DatePickerDialog! ", dateDialog);
    }

    @Test
    public void testMaxDate() {
        View dateLayout = mMetaDataFragment.getView().findViewById(R.id.layoutDate);
        dateLayout.performClick();

        FragmentManager fm = mMetaDataActivity.getFragmentManager();
        DatePickerDialog dateDialog = (DatePickerDialog) fm.findFragmentByTag("DatePickerDialog");
        assertEquals("Date dialog max value can't be more then current date!",
                SimpleDateFormat.getDateInstance().format(mOnlineMetaData.date),
                SimpleDateFormat.getDateInstance().format(dateDialog.getMaxDate().getTime()));
    }

    @Test
    public void testMinDate() {
        View dateLayout = mMetaDataFragment.getView().findViewById(R.id.layoutDate);
        dateLayout.performClick();

        final GregorianCalendar minDateCalendar = new GregorianCalendar();
        minDateCalendar.set(2000, Calendar.JANUARY, 1);

        FragmentManager fm = mMetaDataActivity.getFragmentManager();
        DatePickerDialog dateDialog = (DatePickerDialog) fm.findFragmentByTag("DatePickerDialog");
        assertEquals("Date dialog min value should be January 1, 2000!",
                SimpleDateFormat.getDateInstance().format(minDateCalendar.getTime()),
                SimpleDateFormat.getDateInstance().format(dateDialog.getMinDate().getTime()));
    }

    @Test
    public void testPhotoClick() {
        mOnlineMetaData.canDelete = true;
        recreateActivity();
        View photoLayout = mMetaDataFragment.getView().findViewById(R.id
                .fragment_metadata_layout_image);
        assertEquals("Photo layout should be always clickable!", true, photoLayout.isClickable());

        mOnlineMetaData.canDelete = false;
        recreateActivity();
        View photoLayout2 = mMetaDataFragment.getView().findViewById(R.id
                .fragment_metadata_layout_image);
        assertEquals("Photo layout should be always clickable!", true, photoLayout2.isClickable());
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
        assertNotNull("Send data menu item not found!", menu.findItem(R.id
                .blue_fragment_metadata_menu_send));
        assertNotNull("Delete data menu item not found!", menu.findItem(R.id
                .blue_fragment_metadata_menu_discard));
        assertNotNull("QR code menu item not found!", menu.findItem(R.id
                .blue_fragment_metadata_menu_qr_code));
    }

    @Test
    public void testOptionsMenuVisibility() {
        mOnlineMetaData.canDelete = true;
        recreateActivity();
        Menu menu = shadowOf(mMetaDataActivity).getOptionsMenu();

        assertEquals("Send data menu item should be visible!", true, menu.findItem(R.id
                .blue_fragment_metadata_menu_send).isVisible());
        assertEquals("Delete data menu item should be visible!", true, menu.findItem(R.id
                .blue_fragment_metadata_menu_discard).isVisible());
        assertEquals("QR code menu should be hidden!", false, menu.findItem(R.id
                .blue_fragment_metadata_menu_qr_code).isVisible());

        mOnlineMetaData.canDelete = false;
        recreateActivity();
        Menu menu2 = shadowOf(mMetaDataActivity).getOptionsMenu();

        assertEquals("Send data menu item should be visible!", false, menu2.findItem(R.id
                .blue_fragment_metadata_menu_send).isVisible());
        assertEquals("Delete data menu item should be visible!", false, menu2.findItem(R.id
                .blue_fragment_metadata_menu_discard).isVisible());
        assertEquals("QR code menu should be hidden!", false, menu2.findItem(R.id
                .blue_fragment_metadata_menu_qr_code).isVisible());
    }
}
