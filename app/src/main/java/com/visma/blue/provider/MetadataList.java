package com.visma.blue.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.visma.blue.network.DateTypeDeserializer;
import com.visma.blue.network.DateTypeSerializer;
import com.visma.blue.network.OnlinePhotoType;
import com.visma.blue.network.containers.ExpenseCustomData;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.network.containers.SeveraCustomData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public final class MetadataList implements BaseColumns {

    public static final long UPDATE_DELAY_TIME = 1000;

    // Column names
    public static final String METADATA_CAN_DELETE = "canDelete";
    public static final String METADATA_COMMENT = "comment";
    public static final String METADATA_DATE = "date";
    public static final String METADATA_PHOTO_ID = "photoId";
    public static final String METADATA_TYPE = "type";

    //Remote data
    public static final String METADATA_VERIFIED = "Verified";
    public static final String METADATA_ORIGINAL_FILE_NAME = "OriginalFilename";
    public static final String METADATA_CONTENT_TYPE = "ContentType";
    public static final String METADATA_SYNCHRONIZED = "Synchronized";
    public static final String METADATA_NOT_SYNCED_DUE_TO_ERROR = "isNotSyncedDueToError";
    public static final String METADATA_LOCAL_FILE_NAME = "LocalFileName";

    // More information
    public static final String METADATA_IS_PAYED = "isPayed";
    public static final String METADATA_PAYMENT_DATE = "paymentDate";
    public static final String METADATA_USING_QR_STRING = "usingQrString";
    public static final String METADATA_NAME = "name";
    public static final String METADATA_ORGANISATION_NUMBER = "organisationNumber";
    public static final String METADATA_REFERENCE_NUMBER = "referenceNumber";
    public static final String METADATA_DUE_AMOUNT = "dueAmount";
    public static final String METADATA_HIGH_VAT_AMOUNT = "highVatAmount";
    public static final String METADATA_MIDDLE_VAT_AMOUNT = "middleVatAmount";
    public static final String METADATA_LOW_VAT_AMOUNT = "lowVatAmount";
    public static final String METADATA_ZERO_VAT_AMOUNT = "zeroVatAmount";
    public static final String METADATA_TOTAL_VAT_AMOUNT = "totalVatAmount";
    public static final String METADATA_CURRENCY = "currency";
    public static final String METADATA_INVOICE_DATE = "invoiceDate";
    public static final String METADATA_DUE_DATE = "dueDate";
    public static final String METADATA_CUSTOM_DATA = "customData";
    public static final String METADATA_SEVERA_CUSTOM_DATA = "severaCustomData";
    public static final String METADATA_EXPENSE_CUSTOM_DATA = "expenseCustomData";

    public static final String METADATA_IMAGE = "image";
    public static final String METADATA_USER = "user";

    public static final String METADATA_IS_READY_FOR_PAYMENT = "isReadyForPayment";

    public static final String METADATA_FROM_BACKEND = "METADATA_FROM_BACKEND";


    private ArrayList<OnlineMetaData> mMetadataFromBackEnd;
    private long mLastUpdateTime = 0;

    public static ContentValues getMetadataValues(OnlineMetaData metaData, byte[] bitmapData,
                                                  String user) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(METADATA_CAN_DELETE, metaData.canDelete ? 1 : 0);
        initialValues.put(METADATA_COMMENT, metaData.comment);
        initialValues.put(METADATA_DATE, metaData.date.getTime());
        initialValues.put(METADATA_PHOTO_ID, metaData.photoId);
        initialValues.put(METADATA_TYPE, metaData.type);
        initialValues.put(METADATA_IS_PAYED, metaData.isPaid);
        initialValues.put(METADATA_PAYMENT_DATE,
                metaData.paymentDate == null ? null : metaData.paymentDate.getTime());
        initialValues.put(METADATA_USING_QR_STRING, metaData.usingQrString);
        initialValues.put(METADATA_NAME, metaData.name);
        initialValues.put(METADATA_ORGANISATION_NUMBER, metaData.organisationNumber);
        initialValues.put(METADATA_REFERENCE_NUMBER, metaData.referenceNumber);
        initialValues.put(METADATA_DUE_AMOUNT, metaData.dueAmount);
        initialValues.put(METADATA_HIGH_VAT_AMOUNT, metaData.highVatAmount);
        initialValues.put(METADATA_MIDDLE_VAT_AMOUNT, metaData.middleVatAmount);
        initialValues.put(METADATA_LOW_VAT_AMOUNT, metaData.lowVatAmount);
        initialValues.put(METADATA_ZERO_VAT_AMOUNT, metaData.zeroVatAmount);
        initialValues.put(METADATA_TOTAL_VAT_AMOUNT, metaData.totalVatAmount);
        initialValues.put(METADATA_CURRENCY, metaData.currency);
        initialValues.put(METADATA_INVOICE_DATE,
                metaData.invoiceDate == null ? null : metaData.invoiceDate.getTime());
        initialValues.put(METADATA_DUE_DATE, metaData.dueDate == null ? null : metaData.dueDate
                .getTime());
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeSerializer())
                .create();
        String customDataJsonData = gson.toJson(metaData.customData);
        initialValues.put(METADATA_CUSTOM_DATA, customDataJsonData);
        String severaCustomDataJsonData = gson.toJson(metaData.severaCustomData);
        initialValues.put(METADATA_SEVERA_CUSTOM_DATA, severaCustomDataJsonData);
        String expenseCustomDataJsonData = gson.toJson(metaData.expenseCustomData);
        initialValues.put(METADATA_EXPENSE_CUSTOM_DATA, expenseCustomDataJsonData);
        initialValues.put(METADATA_USER, user);
        initialValues.put(METADATA_IMAGE, bitmapData);
        initialValues.put(METADATA_NOT_SYNCED_DUE_TO_ERROR, metaData.isNotSyncedDueToError ? 1 : 0);
        initialValues.put(METADATA_LOCAL_FILE_NAME, metaData.localFileName);
        initialValues.put(METADATA_CONTENT_TYPE, metaData.contentType);
        initialValues.put(METADATA_ORIGINAL_FILE_NAME, metaData.originalFilename);
        initialValues.put(METADATA_IS_READY_FOR_PAYMENT, metaData.approvedForPayment ? 1 : 0);
        return initialValues;
    }

    public static ContentValues getMetadataValues(OnlineMetaData metaData, Gson gson) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(METADATA_CAN_DELETE, metaData.canDelete);
        initialValues.put(METADATA_COMMENT, metaData.comment);
        initialValues.put(METADATA_DATE, metaData.date.getTime());
        initialValues.put(METADATA_PHOTO_ID, metaData.photoId);
        initialValues.put(METADATA_TYPE, metaData.type);
        initialValues.put(METADATA_IS_PAYED, metaData.isPaid);
        if (metaData.paymentDate != null) {
            initialValues.put(METADATA_PAYMENT_DATE, metaData.paymentDate.getTime());
        }
        initialValues.put(METADATA_USING_QR_STRING, metaData.usingQrString);
        initialValues.put(METADATA_NAME, metaData.name);
        initialValues.put(METADATA_ORGANISATION_NUMBER, metaData.organisationNumber);
        initialValues.put(METADATA_REFERENCE_NUMBER, metaData.referenceNumber);
        initialValues.put(METADATA_DUE_AMOUNT, metaData.dueAmount);
        initialValues.put(METADATA_HIGH_VAT_AMOUNT, metaData.highVatAmount);
        initialValues.put(METADATA_MIDDLE_VAT_AMOUNT, metaData.middleVatAmount);
        initialValues.put(METADATA_LOW_VAT_AMOUNT, metaData.lowVatAmount);
        initialValues.put(METADATA_ZERO_VAT_AMOUNT, metaData.zeroVatAmount);
        initialValues.put(METADATA_TOTAL_VAT_AMOUNT, metaData.totalVatAmount);
        initialValues.put(METADATA_CURRENCY, metaData.currency);
        if (metaData.invoiceDate != null) {
            initialValues.put(METADATA_INVOICE_DATE, metaData.invoiceDate.getTime());
        }
        if (metaData.dueDate != null) {
            initialValues.put(METADATA_DUE_DATE, metaData.dueDate.getTime());
        }
        String customDataJsonData = gson.toJson(metaData.customData);
        initialValues.put(METADATA_CUSTOM_DATA, customDataJsonData);
        String severaCustomDataJsonData = gson.toJson(metaData.severaCustomData);
        initialValues.put(METADATA_SEVERA_CUSTOM_DATA, severaCustomDataJsonData);
        String expenseCustomDataJsonData = gson.toJson(metaData.expenseCustomData);
        initialValues.put(METADATA_EXPENSE_CUSTOM_DATA, expenseCustomDataJsonData);
        initialValues.put(METADATA_VERIFIED, metaData.isVerified);
        initialValues.put(METADATA_ORIGINAL_FILE_NAME, metaData.originalFilename);
        initialValues.put(METADATA_CONTENT_TYPE, metaData.contentType);
        initialValues.put(METADATA_SYNCHRONIZED, true);
        initialValues.put(METADATA_FROM_BACKEND, true);
        initialValues.put(METADATA_NOT_SYNCED_DUE_TO_ERROR, false);
        initialValues.put(METADATA_IS_READY_FOR_PAYMENT, metaData.approvedForPayment);
        return initialValues;
    }

    public static String[] getDatabaseColumnNames() {
        return new String[]{
                _ID,
                METADATA_CAN_DELETE,
                METADATA_COMMENT,
                METADATA_DATE,
                METADATA_PHOTO_ID,
                METADATA_TYPE,
                METADATA_IS_PAYED,
                METADATA_PAYMENT_DATE,
                METADATA_USING_QR_STRING,
                METADATA_NAME,
                METADATA_ORGANISATION_NUMBER,
                METADATA_REFERENCE_NUMBER,
                METADATA_DUE_AMOUNT,
                METADATA_HIGH_VAT_AMOUNT,
                METADATA_MIDDLE_VAT_AMOUNT,
                METADATA_LOW_VAT_AMOUNT,
                METADATA_ZERO_VAT_AMOUNT,
                METADATA_TOTAL_VAT_AMOUNT,
                METADATA_CURRENCY,
                METADATA_INVOICE_DATE,
                METADATA_DUE_DATE,
                METADATA_CUSTOM_DATA,
                METADATA_SEVERA_CUSTOM_DATA,
                METADATA_EXPENSE_CUSTOM_DATA,
                METADATA_IMAGE,
                METADATA_NOT_SYNCED_DUE_TO_ERROR,
                METADATA_LOCAL_FILE_NAME,
                METADATA_CONTENT_TYPE,
                METADATA_ORIGINAL_FILE_NAME,
                METADATA_IS_READY_FOR_PAYMENT};
    }

    public static String[] getRemoteColumnNames() {
        return new String[]{
                METADATA_CAN_DELETE,
                METADATA_COMMENT,
                METADATA_DATE,
                METADATA_PHOTO_ID,
                METADATA_TYPE,
                METADATA_IS_PAYED,
                METADATA_PAYMENT_DATE,
                METADATA_USING_QR_STRING,
                METADATA_NAME,
                METADATA_ORGANISATION_NUMBER,
                METADATA_REFERENCE_NUMBER,
                METADATA_DUE_AMOUNT,
                METADATA_HIGH_VAT_AMOUNT,
                METADATA_MIDDLE_VAT_AMOUNT,
                METADATA_LOW_VAT_AMOUNT,
                METADATA_ZERO_VAT_AMOUNT,
                METADATA_TOTAL_VAT_AMOUNT,
                METADATA_CURRENCY,
                METADATA_INVOICE_DATE,
                METADATA_DUE_DATE,
                METADATA_CUSTOM_DATA,
                METADATA_SEVERA_CUSTOM_DATA,
                METADATA_EXPENSE_CUSTOM_DATA,
                METADATA_VERIFIED,
                METADATA_ORIGINAL_FILE_NAME,
                METADATA_CONTENT_TYPE,
                METADATA_SYNCHRONIZED,
                METADATA_IMAGE,
                _ID,
                METADATA_NOT_SYNCED_DUE_TO_ERROR,
                METADATA_LOCAL_FILE_NAME,
                METADATA_IS_READY_FOR_PAYMENT};
    }

    public static OnlineMetaData getMetaDataFromCursor(Cursor imagesCursor, Gson gson,
                                                       Type customDataListType) {
        OnlineMetaData onlineMetaData = new OnlineMetaData();

        if (gson == null) {
            gson = new GsonBuilder().registerTypeAdapter(Date.class, new
                    DateTypeDeserializer()).create();
        }

        if (customDataListType == null) {
            customDataListType =
                    new TypeToken<ArrayList<OnlineMetaData.CustomDataValue>>() {
                    }.getType();
        }

        // These fields always exists
        onlineMetaData.canDelete =
                imagesCursor.getInt(imagesCursor.getColumnIndex(METADATA_CAN_DELETE)) == 1;
        onlineMetaData.comment = imagesCursor.getString(imagesCursor
                .getColumnIndex(METADATA_COMMENT));
        onlineMetaData.date = new Date(imagesCursor.getLong(imagesCursor
                .getColumnIndex(METADATA_DATE)));
        onlineMetaData.photoId = imagesCursor.getString(imagesCursor
                .getColumnIndex(METADATA_PHOTO_ID));
        onlineMetaData.type = imagesCursor.getInt(imagesCursor
                .getColumnIndex(METADATA_TYPE));

        // These fields might be null and the get functions might throw exceptions in
        // those cases
        if (imagesCursor.getColumnIndex(_ID) != -1 && !imagesCursor.isNull(imagesCursor
                .getColumnIndex(_ID))) {
            onlineMetaData.databaseId = imagesCursor.getLong(imagesCursor
                    .getColumnIndex(_ID));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_IS_PAYED))) {
            onlineMetaData.isPaid = imagesCursor.getInt(
                    imagesCursor.getColumnIndex(METADATA_IS_PAYED)) == 1;
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_PAYMENT_DATE))) {
            onlineMetaData.paymentDate = new Date(imagesCursor.getLong(
                    imagesCursor.getColumnIndex(METADATA_PAYMENT_DATE)));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_USING_QR_STRING))) {
            onlineMetaData.usingQrString = imagesCursor.getString(
                    imagesCursor.getColumnIndex(METADATA_USING_QR_STRING));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_NAME))) {
            onlineMetaData.name =
                    imagesCursor.getString(imagesCursor.getColumnIndex(METADATA_NAME));
        }

        if (!imagesCursor.isNull(imagesCursor
                .getColumnIndex(METADATA_ORGANISATION_NUMBER))) {
            onlineMetaData.organisationNumber = imagesCursor.getString(
                    imagesCursor.getColumnIndex(METADATA_ORGANISATION_NUMBER));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_REFERENCE_NUMBER))) {
            onlineMetaData.referenceNumber = imagesCursor.getString(
                    imagesCursor.getColumnIndex(METADATA_REFERENCE_NUMBER));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_DUE_AMOUNT))) {
            onlineMetaData.dueAmount = imagesCursor.getDouble(
                    imagesCursor.getColumnIndex(METADATA_DUE_AMOUNT));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_HIGH_VAT_AMOUNT))) {
            onlineMetaData.highVatAmount = imagesCursor.getDouble(
                    imagesCursor.getColumnIndex(METADATA_HIGH_VAT_AMOUNT));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_MIDDLE_VAT_AMOUNT))) {
            onlineMetaData.middleVatAmount = imagesCursor.getDouble(
                    imagesCursor.getColumnIndex(METADATA_MIDDLE_VAT_AMOUNT));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_LOW_VAT_AMOUNT))) {
            onlineMetaData.lowVatAmount = imagesCursor.getDouble(
                    imagesCursor.getColumnIndex(METADATA_LOW_VAT_AMOUNT));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_ZERO_VAT_AMOUNT))) {
            onlineMetaData.zeroVatAmount = imagesCursor.getDouble(
                    imagesCursor.getColumnIndex(METADATA_ZERO_VAT_AMOUNT));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_TOTAL_VAT_AMOUNT))) {
            onlineMetaData.totalVatAmount = imagesCursor.getDouble(
                    imagesCursor.getColumnIndex(METADATA_TOTAL_VAT_AMOUNT));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_CURRENCY))) {
            onlineMetaData.currency = imagesCursor.getString(
                    imagesCursor.getColumnIndex(METADATA_CURRENCY));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_INVOICE_DATE))) {
            onlineMetaData.invoiceDate = new Date(imagesCursor.getLong(
                    imagesCursor.getColumnIndex(METADATA_INVOICE_DATE)));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_DUE_DATE))) {
            onlineMetaData.dueDate = new Date(imagesCursor.getLong(
                    imagesCursor.getColumnIndex(METADATA_DUE_DATE)));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_CUSTOM_DATA))) {
            String savedJsonData =
                    imagesCursor.getString(imagesCursor
                            .getColumnIndex(METADATA_CUSTOM_DATA));
            onlineMetaData.customData = gson.fromJson(savedJsonData, customDataListType);
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_SEVERA_CUSTOM_DATA))) {
            String savedJsonData =
                    imagesCursor.getString(imagesCursor
                            .getColumnIndex(METADATA_SEVERA_CUSTOM_DATA));
            onlineMetaData.severaCustomData = gson.fromJson(savedJsonData,
                    SeveraCustomData.class);
        }

        if (!imagesCursor.isNull(imagesCursor
                .getColumnIndex(METADATA_EXPENSE_CUSTOM_DATA))) {
            String savedJsonData =
                    imagesCursor.getString(imagesCursor
                            .getColumnIndex(METADATA_EXPENSE_CUSTOM_DATA));
            onlineMetaData.expenseCustomData = gson.fromJson(savedJsonData,
                    ExpenseCustomData.class);
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_VERIFIED))) {
            onlineMetaData.isVerified = imagesCursor.getInt(
                    imagesCursor.getColumnIndex(METADATA_VERIFIED)) == 1;
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_ORIGINAL_FILE_NAME))) {
            onlineMetaData.originalFilename = imagesCursor.getString(
                    imagesCursor.getColumnIndex(METADATA_ORIGINAL_FILE_NAME));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_CONTENT_TYPE))) {
            onlineMetaData.contentType = imagesCursor.getString(
                    imagesCursor.getColumnIndex(METADATA_CONTENT_TYPE));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_SYNCHRONIZED))) {
            onlineMetaData.isSynchronized = imagesCursor.getColumnIndex(METADATA_SYNCHRONIZED) == 1;
        } else {
            // At the moment all images in the database are unsynchronized
            onlineMetaData.isSynchronized = false;
            if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_IMAGE))) {
                int columnIndex = imagesCursor.getColumnIndexOrThrow(MetadataList.METADATA_IMAGE);
                onlineMetaData.image = imagesCursor.getBlob(columnIndex);
            }
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_NOT_SYNCED_DUE_TO_ERROR))) {
            onlineMetaData.isNotSyncedDueToError =  imagesCursor.getInt(imagesCursor
                    .getColumnIndex(METADATA_NOT_SYNCED_DUE_TO_ERROR)) == 1;
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_LOCAL_FILE_NAME))) {
            onlineMetaData.localFileName = imagesCursor.getString(
                    imagesCursor.getColumnIndex(METADATA_LOCAL_FILE_NAME));
        }

        if (!imagesCursor.isNull(imagesCursor.getColumnIndex(METADATA_IS_READY_FOR_PAYMENT))) {
            onlineMetaData.approvedForPayment = imagesCursor.getInt(
                    imagesCursor.getColumnIndex(METADATA_IS_READY_FOR_PAYMENT)) == 1;
        }


        return onlineMetaData;
    }

    public static ArrayList<OnlineMetaData> getMetaData(Cursor imagesCursor) {
        ArrayList<OnlineMetaData> images = new ArrayList<>();
        if (imagesCursor != null) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new
                    DateTypeDeserializer()).create();
            final Type customDataListType =
                    new TypeToken<ArrayList<OnlineMetaData.CustomDataValue>>() {
                    }.getType();
            boolean rowsLeft = imagesCursor.moveToFirst();
            while (rowsLeft) {
                images.add(getMetaDataFromCursor(imagesCursor, gson, customDataListType));
                rowsLeft = imagesCursor.moveToNext();
            }
        }
        return images;
    }

    private OnlineMetaData getMetaData(ContentValues values, Gson gson, Type customDataListType) {

        if (values != null) {
            OnlineMetaData onlineMetaData = new OnlineMetaData();
            onlineMetaData.canDelete = values.getAsBoolean(METADATA_CAN_DELETE);
            onlineMetaData.comment = values.getAsString(METADATA_COMMENT);
            onlineMetaData.date = new Date(values.getAsLong(METADATA_DATE));
            onlineMetaData.photoId = values.getAsString(METADATA_PHOTO_ID);
            onlineMetaData.type = values.getAsInteger(METADATA_TYPE);
            onlineMetaData.isPaid = values.getAsBoolean(METADATA_IS_PAYED);
            if (values.containsKey(METADATA_PAYMENT_DATE)) {
                onlineMetaData.paymentDate = new Date(values.getAsLong(METADATA_PAYMENT_DATE));
            }
            onlineMetaData.usingQrString = values.getAsString(METADATA_USING_QR_STRING);
            onlineMetaData.name = values.getAsString(METADATA_NAME);
            onlineMetaData.organisationNumber = values.getAsString(METADATA_ORGANISATION_NUMBER);
            onlineMetaData.referenceNumber = values.getAsString(METADATA_REFERENCE_NUMBER);
            onlineMetaData.dueAmount = values.getAsDouble(METADATA_DUE_AMOUNT);
            onlineMetaData.highVatAmount = values.getAsDouble(METADATA_HIGH_VAT_AMOUNT);
            onlineMetaData.middleVatAmount = values.getAsDouble(METADATA_MIDDLE_VAT_AMOUNT);
            onlineMetaData.lowVatAmount = values.getAsDouble(METADATA_LOW_VAT_AMOUNT);
            onlineMetaData.zeroVatAmount = values.getAsDouble(METADATA_ZERO_VAT_AMOUNT);
            onlineMetaData.totalVatAmount = values.getAsDouble(METADATA_TOTAL_VAT_AMOUNT);
            onlineMetaData.currency = values.getAsString(METADATA_CURRENCY);
            if (values.containsKey(METADATA_INVOICE_DATE)) {
                onlineMetaData.invoiceDate = new Date(values.getAsLong(METADATA_INVOICE_DATE));
            }
            if (values.containsKey(METADATA_DUE_DATE)) {
                onlineMetaData.dueDate = new Date(values.getAsLong(METADATA_DUE_DATE));
            }
            String savedCustomData = values.getAsString(METADATA_CUSTOM_DATA);
            if (savedCustomData != null) {
                onlineMetaData.customData = gson.fromJson(savedCustomData, customDataListType);
            }
            String savedSeveraData = values.getAsString(METADATA_SEVERA_CUSTOM_DATA);
            if (savedCustomData != null) {
                onlineMetaData.severaCustomData = gson.fromJson(savedSeveraData,
                        SeveraCustomData.class);
            }
            String savedExpenseData = values.getAsString(METADATA_EXPENSE_CUSTOM_DATA);
            if (savedCustomData != null) {
                onlineMetaData.expenseCustomData = gson.fromJson(savedExpenseData,
                        ExpenseCustomData.class);
            }
            onlineMetaData.isVerified = values.getAsBoolean(METADATA_VERIFIED);
            onlineMetaData.originalFilename = values.getAsString(METADATA_ORIGINAL_FILE_NAME);
            onlineMetaData.contentType = values.getAsString(METADATA_CONTENT_TYPE);
            onlineMetaData.isSynchronized = values.getAsBoolean(METADATA_SYNCHRONIZED);
            onlineMetaData.isNotSyncedDueToError = values.getAsBoolean(METADATA_NOT_SYNCED_DUE_TO_ERROR);
            onlineMetaData.approvedForPayment = values.getAsBoolean(METADATA_IS_READY_FOR_PAYMENT);
            return onlineMetaData;
        }

        return null;
    }

    public synchronized MatrixCursor getMergedMetaData(ArrayList<OnlineMetaData>
                                                               notSyncedMetaDataArray,
                                                       String[] selectionArgs) {
        final String[] columns = getRemoteColumnNames();
        ArrayList<OnlineMetaData> metaDataFromBackend = getMetadataFromBackend(selectionArgs);

        if (notSyncedMetaDataArray.isEmpty() && (metaDataFromBackend == null
                || metaDataFromBackend.isEmpty())) {
            return new MatrixCursor(columns, 0);
        }

        ArrayList<OnlineMetaData> mergedMetaData = getMergedMetadata(notSyncedMetaDataArray,
                metaDataFromBackend);
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeSerializer())
                .create();

        MatrixCursor cursor = new MatrixCursor(columns, mergedMetaData.size());
        MatrixCursor.RowBuilder builder;
        for (OnlineMetaData data : mergedMetaData) {
            builder = cursor.newRow();
            builder.add(data.canDelete ? 1 : 0);
            builder.add(data.comment);
            builder.add(data.date.getTime());
            builder.add(data.photoId);
            builder.add(data.type);
            if (data.isPaid != null) {
                builder.add(data.isPaid ? 1 : 0);
            } else {
                builder.add(null);
            }
            if (data.paymentDate != null) {
                builder.add(data.paymentDate.getTime());
            } else {
                builder.add(null);
            }
            builder.add(data.usingQrString);
            builder.add(data.name);
            builder.add(data.organisationNumber);
            builder.add(data.referenceNumber);

            if (data.dueAmount != null) {
                builder.add(data.dueAmount);
            } else {
                builder.add(null);
            }
            if (data.highVatAmount != null) {
                builder.add(data.highVatAmount);
            } else {
                builder.add(null);
            }
            if (data.middleVatAmount != null) {
                builder.add(data.middleVatAmount);
            } else {
                builder.add(null);
            }
            if (data.lowVatAmount != null) {
                builder.add(data.lowVatAmount);
            } else {
                builder.add(null);
            }
            if (data.zeroVatAmount != null) {
                builder.add(data.zeroVatAmount);
            } else {
                builder.add(null);
            }
            if (data.totalVatAmount != null) {
                builder.add(data.totalVatAmount);
            } else {
                builder.add(null);
            }
            builder.add(data.currency);
            if (data.invoiceDate != null) {
                builder.add(data.invoiceDate.getTime());
            } else {
                builder.add(null);
            }
            if (data.dueDate != null) {
                builder.add(data.dueDate.getTime());
            } else {
                builder.add(null);
            }
            if (data.customData != null) {
                builder.add(gson.toJson(data.customData));
            } else {
                builder.add(null);
            }
            if (data.severaCustomData != null) {
                builder.add(gson.toJson(data.severaCustomData));
            } else {
                builder.add(null);
            }
            if (data.expenseCustomData != null) {
                builder.add(gson.toJson(data.expenseCustomData));
            } else {
                builder.add(null);
            }
            builder.add(data.isVerified ? 1 : 0);
            builder.add(data.originalFilename);
            builder.add(data.contentType);

            builder.add(data.isSynchronized ? 1 : 0);
            builder.add(data.image);
            builder.add(data.databaseId);
            builder.add(data.isNotSyncedDueToError ? 1 : 0);
            builder.add(data.localFileName);
            builder.add(data.approvedForPayment ? 1 : 0);
        }

        return cursor;
    }

    public synchronized void updateMetaData(ContentValues[] values) {
        resetMetaData();
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class,
                new DateTypeSerializer()).create();
        Type customDataListType =
                new TypeToken<ArrayList<OnlineMetaData.CustomDataValue>>() {
                }.getType();
        for (ContentValues contentValues : values) {
            OnlineMetaData metaData = getMetaData(contentValues, gson, customDataListType);
            if (metaData != null) {
                mMetadataFromBackEnd.add(metaData);
            }
        }

        mLastUpdateTime = System.currentTimeMillis();

    }

    public void resetMetaData() {
        if (mMetadataFromBackEnd != null) {
            mMetadataFromBackEnd.clear();
        } else {
            mMetadataFromBackEnd = new ArrayList<>();
        }
    }

    public long getLastUpdateTime() {
        return mLastUpdateTime;
    }

    private ArrayList<OnlineMetaData> getMergedMetadata(ArrayList<OnlineMetaData>
                                                                notSyncedDataArray,
                                                        ArrayList<OnlineMetaData>
                                                                syncedDataArray) {
        ArrayList<OnlineMetaData> mergedMetaData = new ArrayList<>();

        if (syncedDataArray != null) {
            mergedMetaData.addAll(syncedDataArray);
        }

        if (!notSyncedDataArray.isEmpty()) {
            if (mergedMetaData.isEmpty()) {
                return notSyncedDataArray;
            }
            int index = 0;
            for (OnlineMetaData localMetaData : notSyncedDataArray) {

                for (int i = index; i < mergedMetaData.size(); i++) {
                    if (mergedMetaData.get(i).type == OnlinePhotoType.UNKNOWN.getValue()) {
                        index++;
                        continue;
                    }

                    if (mergedMetaData.get(i).isNotSyncedDueToError) {
                        index++;
                        break;
                    }

                    if (localMetaData.date.getTime() > mergedMetaData.get(i).date.getTime()) {
                        break;
                    }
                    index++;

                }
                mergedMetaData.add(index, localMetaData);
            }
        }

        return mergedMetaData;
    }

    public ArrayList<OnlineMetaData> getMetadataFromBackend(String[] selectionArguments) {
        if (selectionArguments == null || selectionArguments.length == 0 || mMetadataFromBackEnd
                == null) {
            return mMetadataFromBackEnd;
        }
        Locale locale = Locale.getDefault();
        String filterValue = selectionArguments[0].toUpperCase(locale);

        ArrayList<OnlineMetaData> filteredMetadata = new ArrayList<>();
        for (OnlineMetaData metaData : mMetadataFromBackEnd) {
            if (metaData.comment.toUpperCase(locale).contains(filterValue)
                    || metaData.organisationNumber != null
                    && metaData.organisationNumber.toUpperCase(locale).contains(filterValue)
                    || metaData.name != null
                    && metaData.name.toUpperCase(locale).contains(filterValue)
                    || metaData.referenceNumber != null
                    && metaData.referenceNumber.toUpperCase(locale).contains(filterValue)) {
                filteredMetadata.add(metaData);
            }
        }

        return filteredMetadata;
    }

}
