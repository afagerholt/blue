package com.visma.blue.network.requests.customdata;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import com.visma.blue.provider.SeveraCases;
import com.visma.blue.provider.SeveraProducts;
import com.visma.blue.provider.SeveraTaxes;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class Severa implements Parcelable {

    @SerializedName("Cases")
    public ArrayList<Case> cases;

    @SerializedName("Products")
    public ArrayList<Product> products;

    @SerializedName("Taxes")
    public ArrayList<Tax> taxes;

    public static class Case implements Parcelable {
        @SerializedName("Guid")
        public String guid;

        @SerializedName("Name")
        public String name;

        @SerializedName("Task")
        public Task task;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.guid);
            dest.writeString(this.name);
            dest.writeParcelable(this.task, 0);
        }

        @Override
        public String toString() {
            return this.name;
        }

        public Case() {
        }

        public Case(Cursor cursor, Gson gson) {
            this.guid = cursor.getString(cursor.getColumnIndexOrThrow(SeveraCases.GUID));
            this.name = cursor.getString(cursor.getColumnIndexOrThrow(SeveraCases.CASE_NAME));

            if (!cursor.isNull(cursor.getColumnIndex(SeveraCases.TASK))) {
                this.task = gson.fromJson(cursor.getString(cursor
                        .getColumnIndexOrThrow(SeveraCases.TASK)), Task.class);
            }
        }

        protected Case(Parcel in) {
            this.guid = in.readString();
            this.name = in.readString();
            this.task = in.readParcelable(Task.class.getClassLoader());
        }

        // Creates case with empty guid
        public Case(String name) {
            this.name = name;
            this.guid = "";
            this.task = new Task();
        }

        public static final Creator<Case> CREATOR = new Creator<Case>() {
            public Case createFromParcel(Parcel source) {
                return new Case(source);
            }

            public Case[] newArray(int size) {
                return new Case[size];
            }
        };
    }

    public static class Task implements Parcelable {
        @SerializedName("Guid")
        public String guid;

        @SerializedName("Name")
        public String name;

        @SerializedName("IsLocked")
        public boolean isLocked;

        @SerializedName("Tasks")
        public ArrayList<Task> tasks;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.guid);
            dest.writeString(this.name);
            dest.writeByte(isLocked ? (byte) 1 : (byte) 0);
            dest.writeTypedList(this.tasks);
        }

        public Task() {
            tasks = new ArrayList<>();
        }

        protected Task(Parcel in) {
            this.guid = in.readString();
            this.name = in.readString();
            this.isLocked = in.readByte() != 0;
            this.tasks = new ArrayList<Task>();
            in.readTypedList(this.tasks, Task.CREATOR);
        }

        public static final Creator<Task> CREATOR = new Creator<Task>() {
            public Task createFromParcel(Parcel source) {
                return new Task(source);
            }

            public Task[] newArray(int size) {
                return new Task[size];
            }
        };
    }

    public static class Product implements Parcelable {
        @SerializedName("Guid")
        public String guid;

        @SerializedName("Name")
        public String name;

        @SerializedName("UseStartAndEndTime")
        public boolean useStartAndEndTime;

        @SerializedName("Price")
        public Double price;

        @SerializedName("CurrencyCode")
        public String currencyCode;

        @SerializedName("VatPercentage")
        public Double vatPercentage;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.guid);
            dest.writeString(this.name);
            dest.writeByte(useStartAndEndTime ? (byte) 1 : (byte) 0);
            dest.writeDouble(this.price);
            dest.writeString(this.currencyCode);
            dest.writeDouble(this.vatPercentage);
        }

        @Override
        public String toString() {
            return this.name;
        }

        public Product() {
        }

        public Product(Cursor cursor) {
            this.guid = cursor.getString(cursor.getColumnIndexOrThrow(SeveraProducts.GUID));
            this.name = cursor.getString(cursor.getColumnIndexOrThrow(SeveraProducts.PRODUCT_NAME));
            this.useStartAndEndTime = cursor.getInt(cursor.getColumnIndexOrThrow(SeveraProducts
                    .USE_START_AND_END_TIME)) == 1;
            this.price = cursor.getDouble(cursor.getColumnIndexOrThrow(SeveraProducts.PRICE));
            this.currencyCode = cursor.getString(cursor.getColumnIndexOrThrow(SeveraProducts
                    .CURRENCY_CODE));
            this.vatPercentage = cursor.getDouble(cursor.getColumnIndexOrThrow(SeveraProducts
                    .VAT_PERCENTAGE));
        }

        public Product(String guid, String name, boolean useStartAndEndTime, Double vatPercentage,
                       String currencyCode, Double price) {
            this.guid = guid;
            this.name = name;
            this.useStartAndEndTime = useStartAndEndTime;
            this.vatPercentage = vatPercentage;
            this.currencyCode = currencyCode;
            this.price = price;
        }

        protected Product(Parcel in) {
            this.guid = in.readString();
            this.name = in.readString();
            this.useStartAndEndTime = in.readByte() != 0;
            this.price = in.readDouble();
            this.currencyCode = in.readString();
            this.vatPercentage = in.readDouble();
        }

        public static final Creator<Product> CREATOR = new Creator<Product>() {
            public Product createFromParcel(Parcel source) {
                return new Product(source);
            }

            public Product[] newArray(int size) {
                return new Product[size];
            }
        };
    }

    public static class Tax implements Parcelable {
        @SerializedName("Guid")
        public String guid;

        @SerializedName("IsDefault")
        public boolean isDefault;

        @SerializedName("Percentage")
        public double percentage;


        public String emptyValueText;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.guid);
            dest.writeByte(isDefault ? (byte) 1 : (byte) 0);
            dest.writeDouble(this.percentage);
        }

        @Override
        public String toString() {
            String value;

            // TODO: its probably inappropriate to do this here

            if (this.percentage < 0) {
                value = this.emptyValueText;
            } else {
                NumberFormat nf = new DecimalFormat("#.####");
                value = nf.format(this.percentage) + "%";
            }
            return value;
        }

        public Tax() {
        }

        public Tax(Cursor cursor) {
            if (!cursor.isNull(cursor.getColumnIndex(SeveraTaxes.GUID))) {
                this.guid = cursor.getString(cursor.getColumnIndexOrThrow(SeveraTaxes.GUID));
            }
            this.isDefault = cursor.getInt(cursor.getColumnIndexOrThrow(SeveraTaxes.IS_DEFAULT))
                    == 1;
            this.percentage = cursor.getDouble(cursor.getColumnIndexOrThrow(SeveraTaxes
                    .PERCENTAGE));
        }


        public Tax(double percentage) {
            this.percentage = percentage;
        }

        public Tax(double percentage, String emptyValueText) {
            this.percentage = percentage;
            this.emptyValueText = emptyValueText;
        }

        protected Tax(Parcel in) {
            this.guid = in.readString();
            this.isDefault = in.readByte() != 0;
            this.percentage = in.readDouble();
        }

        public static final Creator<Tax> CREATOR = new Creator<Tax>() {
            public Tax createFromParcel(Parcel source) {
                return new Tax(source);
            }

            public Tax[] newArray(int size) {
                return new Tax[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(cases);
        dest.writeTypedList(products);
        dest.writeTypedList(taxes);
    }

    public Severa() {
    }

    protected Severa(Parcel in) {
        this.cases = in.createTypedArrayList(Case.CREATOR);
        this.products = in.createTypedArrayList(Product.CREATOR);
        this.taxes = in.createTypedArrayList(Tax.CREATOR);
    }

    public static final Creator<Severa> CREATOR = new Creator<Severa>() {
        public Severa createFromParcel(Parcel source) {
            return new Severa(source);
        }

        public Severa[] newArray(int size) {
            return new Severa[size];
        }
    };
}
