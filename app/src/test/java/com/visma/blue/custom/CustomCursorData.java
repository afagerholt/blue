package com.visma.blue.custom;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.visma.blue.network.DateTypeSerializer;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.provider.MetadataList;

import java.util.ArrayList;
import java.util.Date;

public class CustomCursorData {

    public static Cursor getEmptyCursor() {
        final String[] columns = MetadataList.getRemoteColumnNames();
        return new MatrixCursor(columns, 0);
    }


    public static Cursor getFilledCursor() {
        return getMatrixCursor(CustomMetaData.getMetadataArray());
    }

    public static Cursor getFilledCursorWithDifferentComments() {
        return getMatrixCursor(CustomMetaData.getMetadataArrayWithDifferentComment());
    }

    private static Cursor getMatrixCursor(ArrayList<OnlineMetaData> testMetaData) {
        final String[] columns = MetadataList.getRemoteColumnNames();

        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeSerializer())
                .create();
        MatrixCursor cursor = new MatrixCursor(columns, testMetaData.size());
        MatrixCursor.RowBuilder builder;
        for (OnlineMetaData data : testMetaData) {
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
        }

        return cursor;
    }
}
