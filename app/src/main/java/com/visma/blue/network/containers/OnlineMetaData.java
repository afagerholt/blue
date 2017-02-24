package com.visma.blue.network.containers;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import com.visma.blue.network.DateTypeSerializer;
import com.visma.blue.network.OnlinePhotoType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

public class OnlineMetaData implements Parcelable, Cloneable {
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @SerializedName("CanDelete")
    public Boolean canDelete;
    @SerializedName("Comment")
    public String comment;
    @SerializedName("Date")
    public Date date;
    @SerializedName("PhotoId")
    public String photoId;
    @SerializedName("Type")
    public int type;
    @SerializedName("IsPaid")
    public Boolean isPaid;
    @SerializedName("PaymentDate")
    public Date paymentDate;

    @SerializedName("Verified")
    public boolean isVerified;
    @SerializedName("OriginalFilename")
    public String originalFilename;
    @SerializedName("ContentType")
    public String contentType = "image/jpeg";

    // More information
    @SerializedName("UsingQRString")
    public String usingQrString;
    @SerializedName("Name")
    public String name;
    @SerializedName("OrganisationNumber")
    public String organisationNumber;
    @SerializedName("ReferenceNumber")
    public String referenceNumber;
    @SerializedName("DueAmount")
    public Double dueAmount;
    @SerializedName("TotalVatAmount")
    public Double totalVatAmount;
    @SerializedName("HighVatAmount")
    public Double highVatAmount;
    @SerializedName("MiddleVatAmount")
    public Double middleVatAmount;
    @SerializedName("LowVatAmount")
    public Double lowVatAmount;
    @SerializedName("ZeroVatAmount")
    public Double zeroVatAmount;
    @SerializedName("Currency")
    public String currency;
    @SerializedName("InvoiceDate")
    public Date invoiceDate;
    @SerializedName("DueDate")
    public Date dueDate;
    @SerializedName("ApprovedForPayment")
    public boolean approvedForPayment;

    // Custom data
    @SerializedName("CustomData")
    public ArrayList<CustomDataValue> customData;

    @SerializedName("SeveraCustomData")
    public SeveraCustomData severaCustomData;

    @SerializedName("ExpenseCustomData")
    public ExpenseCustomData expenseCustomData;

    // Local variables that are never synced to the server
    public boolean isSynchronized = true;
    public boolean isNotSyncedDueToError = false;
    public long databaseId = 0;
    public byte [] image;
    public String localFileName;

    // Used when creating from the database
    public OnlineMetaData() {
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        customData = new ArrayList<>();
    }

    // Used when creating new ones
    public OnlineMetaData(Boolean canDelete, String comment, Date date, String owner, String photoId, int type) {
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        customData = new ArrayList<>();

        this.canDelete = canDelete;
        this.comment = comment;
        this.date = date;
        this.photoId = photoId;
        this.type = type;

        if (type == OnlinePhotoType.INVOICE.getValue()) {
            this.isPaid = false;
        } else if (type == OnlinePhotoType.RECEIPT.getValue()) {
            this.isPaid = true;
            this.paymentDate = new Date();
        } else {
            this.isPaid = true;
        }

        this.isSynchronized = false;
    }

    public String getLocalDateString() {
        DateFormat dateFormat = SimpleDateFormat.getDateInstance();
        return dateFormat.format(this.date);
    }

    public JSONObject getUpdateJson() throws JSONException {
        JSONObject temp = new JSONObject();

        temp.put("Comment", this.comment);
        temp.put("Type", this.type);
        temp.put("Timestamp", mDateFormat.format(date)); //Different name when we upload/download
        temp.put("IsPaid", this.isPaid);
        temp.put("PaymentDate", this.paymentDate == null ? null : mDateFormat.format(this.paymentDate));

        // More information
        temp.put("UsingQRString", this.usingQrString);
        temp.put("Name", this.name);
        temp.put("OrganisationNumber", this.organisationNumber);
        temp.put("ReferenceNumber", this.referenceNumber);
        temp.put("DueAmount", this.dueAmount);
        temp.put("TotalVatAmount", this.totalVatAmount);
        temp.put("HighVatAmount", this.highVatAmount);
        temp.put("MiddleVatAmount", this.middleVatAmount);
        temp.put("LowVatAmount", this.lowVatAmount);
        temp.put("ZeroVatAmount", this.zeroVatAmount);
        temp.put("Currency", this.currency);
        temp.put("InvoiceDate", this.invoiceDate == null ? null : mDateFormat.format(this.invoiceDate));
        temp.put("DueDate", this.dueDate == null ? null : mDateFormat.format(this.dueDate));
        temp.put("ApprovedForPayment", this.approvedForPayment);

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        Type listType = new TypeToken<ArrayList<CustomDataValue>>() {}.getType();
        ArrayList<CustomDataValue> filteredCustomData = new ArrayList<CustomDataValue>(customData);
        Iterator<CustomDataValue> iterator = filteredCustomData.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isEmptyValue) {
                iterator.remove();
            }
        }
        temp.put("CustomData", new JSONArray(gson.toJson(filteredCustomData, listType)));

        if (severaCustomData != null) {
            Gson severaGson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateTypeSerializer())
                    .create();
            temp.put("SeveraCustomData", new JSONObject(severaGson.toJson(severaCustomData)));
        }

        ExpenseCustomData filteredExpenseCustomData = ExpenseCustomData.removeEmptyValues(this.expenseCustomData);
        if (filteredExpenseCustomData != null) {
            Gson severaGson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .registerTypeAdapter(Date.class, new DateTypeSerializer())
                    .create();
            temp.put("ExpenseCustomData", new JSONObject(severaGson.toJson(filteredExpenseCustomData)));
        }

        return temp;
    }

    public OnlineMetaData clone() {
        try {
            return (OnlineMetaData) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeBooleanArray(new boolean[]{canDelete, isSynchronized, isVerified, approvedForPayment});
        out.writeString(comment);
        out.writeLong(date.getTime());
        out.writeString(photoId);
        out.writeInt(type);
        out.writeValue(isPaid);
        out.writeValue(paymentDate);
        out.writeValue(originalFilename);
        out.writeValue(contentType);

        out.writeValue(usingQrString);
        out.writeValue(name);
        out.writeValue(organisationNumber);
        out.writeValue(referenceNumber);
        out.writeValue(dueAmount);
        out.writeValue(totalVatAmount);
        out.writeValue(highVatAmount);
        out.writeValue(middleVatAmount);
        out.writeValue(lowVatAmount);
        out.writeValue(zeroVatAmount);
        out.writeValue(currency);
        out.writeValue(invoiceDate);
        out.writeValue(dueDate);

        out.writeTypedList(this.customData);
        out.writeValue(this.severaCustomData);
        out.writeValue(this.expenseCustomData);

        out.writeValue(databaseId);
        out.writeValue(localFileName);
    }

    public static final Creator<OnlineMetaData> CREATOR = new Creator<OnlineMetaData>() {
        public OnlineMetaData createFromParcel(Parcel in) {
            return new OnlineMetaData(in);
        }

        public OnlineMetaData[] newArray(int size) {
            return new OnlineMetaData[size];
        }
    };

    private OnlineMetaData(Parcel in) {
        customData = new ArrayList<CustomDataValue>();

        boolean[] booleans = new boolean[4];
        in.readBooleanArray(booleans);
        canDelete = booleans[0];
        isSynchronized = booleans[1];
        isVerified = booleans[2];
        approvedForPayment = booleans[3];
        comment = in.readString();
        date = new Date(in.readLong());
        photoId = in.readString();
        type = in.readInt();
        isPaid = (Boolean) in.readValue(Boolean.class.getClassLoader());
        paymentDate = (Date) in.readValue(Date.class.getClassLoader());
        originalFilename = (String) in.readValue(String.class.getClassLoader());
        contentType = (String) in.readValue(String.class.getClassLoader());

        usingQrString = (String) in.readValue(String.class.getClassLoader());
        name = (String) in.readValue(String.class.getClassLoader());
        organisationNumber = (String) in.readValue(String.class.getClassLoader());
        referenceNumber = (String) in.readValue(String.class.getClassLoader());
        dueAmount = (Double) in.readValue(Double.class.getClassLoader());
        totalVatAmount = (Double) in.readValue(Double.class.getClassLoader());
        highVatAmount = (Double) in.readValue(Double.class.getClassLoader());
        middleVatAmount = (Double) in.readValue(Double.class.getClassLoader());
        lowVatAmount = (Double) in.readValue(Double.class.getClassLoader());
        zeroVatAmount = (Double) in.readValue(Double.class.getClassLoader());
        currency = (String) in.readValue(String.class.getClassLoader());
        invoiceDate = (Date) in.readValue(Date.class.getClassLoader());
        dueDate = (Date) in.readValue(Date.class.getClassLoader());

        in.readTypedList(this.customData, CustomDataValue.CREATOR);
        severaCustomData = (SeveraCustomData) in.readValue(SeveraCustomData.class.getClassLoader());
        expenseCustomData = (ExpenseCustomData) in.readValue(ExpenseCustomData.class.getClassLoader());

        databaseId = (Long) in.readValue(Long.class.getClassLoader());
        localFileName = (String) in.readValue(String.class.getClassLoader());
    }

    public static class CustomDataValue implements Parcelable {
        public static final int TYPE_SPINNER = 0;

        @Expose
        @SerializedName("Id")
        public String id;
        @Expose
        @SerializedName("Type")
        public int type;
        @Expose
        @SerializedName("Title")
        public String name;
        @Expose
        @SerializedName("ValueId")
        public String valueId;
        @Expose
        @SerializedName("ValueTitle")
        public String valueTitle;
        @Expose
        @SerializedName("ValueSubTitle")
        public String valueSubTitle;

        public boolean isEmptyValue;

        public CustomDataValue() {
        }

        public static ArrayList<CustomDataValue> removeEmptyValues(ArrayList<CustomDataValue> customData) {
            if (customData == null) {
                return null;
            }
            ArrayList<CustomDataValue> filteredCustomData =
                    (ArrayList<OnlineMetaData.CustomDataValue>) customData.clone();

            Iterator<OnlineMetaData.CustomDataValue> iterator = filteredCustomData.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().isEmptyValue) {
                    iterator.remove();
                }
            }

            return filteredCustomData;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(this.id);
            out.writeInt(this.type);
            out.writeString(this.name);
            out.writeString(this.valueId);
            out.writeString(this.valueTitle);
            out.writeString(this.valueSubTitle);
            out.writeInt(isEmptyValue ? 1 : 0);
        }

        public static final Creator<CustomDataValue> CREATOR = new Creator<CustomDataValue>() {
            public CustomDataValue createFromParcel(Parcel in) {
                return new CustomDataValue(in);
            }

            public CustomDataValue[] newArray(int size) {
                return new CustomDataValue[size];
            }
        };

        private CustomDataValue(Parcel in) {
            this.id = in.readString();
            this.type = in.readInt();
            this.name = in.readString();
            this.valueId = in.readString();
            this.valueTitle = in.readString();
            this.valueSubTitle = in.readString();
            this.isEmptyValue = in.readInt() == 1;
        }
    }
}
