package com.visma.blue.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.visma.blue.metadata.BaseMetadataFragment;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.OnlinePhotoType;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.network.containers.OnlinePhoto;
import com.visma.blue.network.containers.SeveraCustomData;
import com.visma.blue.network.requests.customdata.Severa;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class CustomMetaData {

    public static final int SEVERA_TAX_COUNT = 5;
    public static final int CASES_PHASES_COUNT = 3;
    public static final int SEVERA_PRODUCT_COUNT = 4;
    public static final int SEVERA_CASES_COUNT = 5;
    public static final int SEVERA_TASKS_COUNT = 5;
    public static final int METADATA_LIST_COUNT = 20;

    public static OnlineMetaData getDefaultMetaData(Context context) {
        int defaultPhotoType = VismaUtils.getLastSelectedTypeOrDefault(context);
        return new OnlineMetaData(true, "", new Date(), "", "", defaultPhotoType);
    }

    public static OnlineMetaData getMetaDataWithExpenseType(Context context) {
        OnlineMetaData onlineMetaData = getDefaultMetaData(context);
        onlineMetaData.severaCustomData = new SeveraCustomData();
        onlineMetaData.severaCustomData.product = new SeveraCustomData.Product(
                "Test product guid", "Test product", false);
        onlineMetaData.severaCustomData.vatPercentage = 0.0;
        onlineMetaData.currency = "Eur";
        onlineMetaData.dueAmount = 55.55;
        return onlineMetaData;
    }

    public static Intent getDefaultIntent(OnlineMetaData onlineMetaData) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(BaseMetadataFragment.EXTRA_DATA_METADATA, onlineMetaData);
        intent.putExtra(BaseMetadataFragment.EXTRA_DATA_IMAGE_IS_SENT, false);
        intent.putExtra(BaseMetadataFragment.EXTRA_DATA_USE_TEMP_BITMAP, false);
        return intent;
    }

    public static String getInvoiceQrString() {
        return "{\"vat\":8610.0000,\"uqr\":1,\"tp\":1,\"nme\":\"Maria Mobil Scanner\","
                + "\"cc\":\"SE\",\"cid\":\"555555-5555\",\"iref\":\"1205574\",\"idt\":\"20160224\","
                + "\"ddt\":\"20160325\",\"due\":43050.0000,\"cur\":\"SEK\",\"pt\":\"BBAN\","
                + "\"acc\":\"256356323\",\"bc\":\"NDEASESS\",\"adr\":\"12312 staden\"}";
    }

    public static String getReceiptQrString() {
        return "{\"vat\":8610.0000,\"uqr\":1,\"tp\":3,\"nme\":\"Maria Mobil Scanner\","
                + "\"cc\":\"SE\",\"due\":43050.0000,\"cur\":\"SEK\",\"idt\":\"20160224\","
                + "\"pt\":\"BBAN\",\"acc\":\"256356323\",\"bc\":\"NDEASESS\",\"adr\":\"12312 "
                + "staden\"}";
    }

    public static SeveraCustomData.Case getCustomCasePhase() {
        SeveraCustomData.Case customCasePhase = new SeveraCustomData.Case();
        customCasePhase.guid = "Case_test_guid_0";
        customCasePhase.name = "Test:Test";
        ArrayList<SeveraCustomData.Task> tasks = new ArrayList<>();
        for (int i = 0; i < CASES_PHASES_COUNT; i++) {
            SeveraCustomData.Task task = new SeveraCustomData.Task();
            task.guid = "Task_guid_test_" + i;
            task.name = "Task_name_test_" + i;
            task.hierarchyLevel = i;
            tasks.add(task);
        }

        customCasePhase.tasks = tasks;
        return customCasePhase;
    }

    public static ArrayList<Severa.Product> getCustomProductList() {
        ArrayList<Severa.Product> products = new ArrayList<>();
        for (int i = 0; i < SEVERA_PRODUCT_COUNT; i++) {
            Severa.Product product = new Severa.Product();
            product.guid = "Product_guid_test_" + i;
            product.name = "Product_test_name_" + i;
            product.useStartAndEndTime = false;
            product.price = 10.0 + i;
            product.currencyCode = "Eur";
            product.vatPercentage = 15.15;
            products.add(product);
        }
        return products;
    }

    public static ArrayList<Severa.Tax> getCustomTaxList() {
        ArrayList<Severa.Tax> taxes = new ArrayList<>();
        for (int i = 0; i < SEVERA_TAX_COUNT; i++) {
            Severa.Tax tax = new Severa.Tax();
            tax.guid = "Taxes_guid_test_" + i;
            tax.percentage = 15 + i;
            tax.isDefault = false;
            taxes.add(tax);
        }
        return taxes;
    }

    public static ArrayList<Severa.Case> getCustomSeveraCases() {
        ArrayList<Severa.Case> severaCases = new ArrayList<>();
        for (int j = 0; j < SEVERA_CASES_COUNT; j++) {
            Severa.Case severaCase = new Severa.Case();
            severaCase.guid = "Case_test_guid_" + j;
            severaCase.name = "Test:Test_" + j;
            severaCase.task = new Severa.Task();
            severaCase.task.name = "Severa_task_name_" + j;
            severaCase.task.guid = "Severa_task_guid_" + j;
            severaCase.task.isLocked = false;

            ArrayList<Severa.Task> tasks = new ArrayList<>();
            for (int i = 0; i < SEVERA_CASES_COUNT; i++) {
                Severa.Task task = new Severa.Task();
                task.guid = "Task_guid_test_" + i;
                task.name = "Task_name_test_" + i;
                task.isLocked = false;

                tasks.add(task);
            }
            severaCase.task.tasks = tasks;
            severaCases.add(severaCase);
        }

        return severaCases;
    }

    public static ArrayList<Severa.Task> getMockedSeveraTasks() {
        ArrayList<Severa.Task> severaTasks = new ArrayList<>();
        for (int j = 0; j < SEVERA_TASKS_COUNT; j++) {
            Severa.Task severaTask = new Severa.Task();
            severaTask.name = "Severa_task_name_" + j;
            severaTask.guid = "Severa_task_guid_" + j;
            severaTask.isLocked = false;

            ArrayList<Severa.Task> innerTasks = new ArrayList<>();
            for (int i = 0; i < SEVERA_TASKS_COUNT; i++) {
                Severa.Task task = new Severa.Task();
                task.guid = "Task_inner_guid_test_" + i;
                task.name = "Task__inner_name_test_" + i;
                task.isLocked = false;
                innerTasks.add(task);
            }
            severaTask.tasks = innerTasks;
            severaTasks.add(severaTask);
        }

        return severaTasks;
    }

    public static ArrayList<SeveraCustomData.Task> getCustomDataSeveraTasks() {
        ArrayList<SeveraCustomData.Task> tasks = new ArrayList<>();
        for (int i = 0; i < SEVERA_TASKS_COUNT; i++) {
            SeveraCustomData.Task task = new SeveraCustomData.Task();
            task.guid = "Task_guid_test_" + i;
            task.name = "Task_name_test_" + i;
            task.hierarchyLevel = i;
            tasks.add(task);
        }
        return tasks;
    }

    private static byte[] getCustomPhoto(){
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.GREEN);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        return baos.toByteArray();
    }

    public static OnlineMetaData getExtendedMetaData() {
        OnlineMetaData metaData = new OnlineMetaData();

        metaData.canDelete = true;
        metaData.comment = "Extended data created from an automatic test.";
        metaData.type = OnlinePhotoType.INVOICE.getValue();
        metaData.photoId = "";

        metaData.isPaid = true;

        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(2015, 02, 12);

        metaData.date = calendar.getTime();
        calendar.add(Calendar.DATE, -5);
        metaData.invoiceDate = calendar.getTime();
        calendar.add(Calendar.DATE, 25);
        metaData.paymentDate = calendar.getTime();
        calendar.add(Calendar.DATE, 5);
        metaData.dueDate = calendar.getTime();

        metaData.usingQrString = null;
        metaData.name = "Lasses Bradgard AB";
        metaData.organisationNumber = "555555-5555";
        metaData.referenceNumber = "123";
        metaData.dueAmount = 5500.0;
        metaData.highVatAmount = null;
        metaData.middleVatAmount = null;
        metaData.lowVatAmount = null;
        metaData.totalVatAmount = metaData.dueAmount;
        metaData.currency = "SEK";

        return metaData;
    }

    public static OnlineMetaData getExtendedMetaData(String comment) {
        OnlineMetaData metaData = getExtendedMetaData();
        metaData.comment = comment;
        return metaData;
    }

    public static OnlinePhoto getCustomOnlinePhoto(){
        return new OnlinePhoto(getExtendedMetaData(), getCustomPhoto());
    }

    public static ArrayList<OnlineMetaData> getMetadataArray(){
        ArrayList<OnlineMetaData> metadataArray = new ArrayList<>();
        for (int i = 0; i < METADATA_LIST_COUNT; i++){
            metadataArray.add(getExtendedMetaData());
        }

        return metadataArray;
    }

    public static ArrayList<OnlineMetaData> getMetadataArrayWithDifferentComment(){
        ArrayList<OnlineMetaData> metadataArray = new ArrayList<>();
        for (int i = 0; i < METADATA_LIST_COUNT; i++){
            metadataArray.add(getExtendedMetaData("Test"+i));
        }

        return metadataArray;
    }
}
