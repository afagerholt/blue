package com.visma.blue.network.containers;

import android.util.Base64;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class OnlinePhoto {
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Expose
    @SerializedName("Comment")
    private String comment;

    @Expose
    @SerializedName("Type")
    private int type;

    @Expose
    @SerializedName("Timestamp")
    private String timestamp;

    @Expose
    @SerializedName("ContentType")
    private String contentType = "image/jpeg";

    @Expose
    @SerializedName("OriginalFilename")
    public String originalFilename;

    private byte [] document;

    // More information
    @Expose
    @SerializedName("UsingQRString")
    private String usingQrString;

    @Expose
    @SerializedName("Name")
    private String name;

    @Expose
    @SerializedName("OrganisationNumber")
    private String organisationNumber;

    @Expose
    @SerializedName("ReferenceNumber")
    private String referenceNumber;

    @Expose
    @SerializedName("DueAmount")
    private Double dueAmount;

    @Expose
    @SerializedName("TotalVatAmount")
    private Double totalVatAmount;

    @Expose
    @SerializedName("HighVatAmount")
    private Double highVatAmount;

    @Expose
    @SerializedName("MiddleVatAmount")
    private Double middleVatAmount;

    @Expose
    @SerializedName("LowVatAmount")
    private Double lowVatAmount;

    @Expose
    @SerializedName("ZeroVatAmount")
    private Double zeroVatAmount;

    @Expose
    @SerializedName("Currency")
    private String currency;

    @Expose
    @SerializedName("InvoiceDate")
    private String invoiceDate;

    @Expose
    @SerializedName("DueDate")
    private String dueDate;

    @Expose
    @SerializedName("IsPaid")
    private Boolean isPaid;

    @Expose
    @SerializedName("PaymentDate")
    private String paymentDate;

    @Expose
    @SerializedName("approvedForPayment")
    private boolean approvedForPayment;

    // Custom data
    @Expose
    @SerializedName("CustomData")
    public ArrayList<OnlineMetaData.CustomDataValue> customData;

    @Expose
    @SerializedName("SeveraCustomData")
    public SeveraCustomData severaCustomData;

    @Expose
    @SerializedName("ExpenseCustomData")
    public ExpenseCustomData expenseCustomData;

    public OnlinePhoto(OnlineMetaData onlineMetaData, byte[] documentData) {
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        this.comment = onlineMetaData.comment;
        this.type = onlineMetaData.type;
        this.document = documentData;
        this.timestamp = mDateFormat.format(onlineMetaData.date);

        // More information
        this.usingQrString = onlineMetaData.usingQrString;
        this.name = onlineMetaData.name;
        this.organisationNumber = onlineMetaData.organisationNumber;
        this.referenceNumber = onlineMetaData.referenceNumber;
        this.dueAmount = onlineMetaData.dueAmount;
        this.totalVatAmount = onlineMetaData.totalVatAmount;
        this.highVatAmount = onlineMetaData.highVatAmount;
        this.middleVatAmount = onlineMetaData.middleVatAmount;
        this.lowVatAmount = onlineMetaData.lowVatAmount;
        this.zeroVatAmount = onlineMetaData.zeroVatAmount;
        this.currency = onlineMetaData.currency;
        this.invoiceDate = onlineMetaData.invoiceDate == null ? null : mDateFormat.format(onlineMetaData.invoiceDate);
        this.dueDate = onlineMetaData.dueDate == null ? null : mDateFormat.format(onlineMetaData.dueDate);
        this.isPaid = onlineMetaData.isPaid;
        this.paymentDate = onlineMetaData.paymentDate == null ? null : mDateFormat.format(onlineMetaData.paymentDate);
        this.approvedForPayment = onlineMetaData.approvedForPayment;
        if (onlineMetaData.contentType != null) {
            this.contentType = onlineMetaData.contentType;
        }

        if (onlineMetaData.originalFilename != null) {
            this.originalFilename = onlineMetaData.originalFilename;
        }

        // Custom data
        this.customData = OnlineMetaData.CustomDataValue.removeEmptyValues(onlineMetaData.customData);
        this.severaCustomData = SeveraCustomData.removeEmptyValues(onlineMetaData.severaCustomData);
        this.expenseCustomData = ExpenseCustomData.removeEmptyValues(onlineMetaData.expenseCustomData);
    }


    public byte[] getDocument() {
        return document;
    }
}
