package com.visma.blue.network.requests.customdata;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.ExpenseCurrencies;
import com.visma.blue.provider.ExpenseExpenseTypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Expense implements Parcelable {

    @SerializedName("ExpenseTypes")
    public ArrayList<ExpenseType> expenseTypes;

    @SerializedName("Currencies")
    public ArrayList<Currency> currencies;

    public static class ExpenseType implements Parcelable {

        @Expose
        @SerializedName("Name")
        public String name;

        @Expose
        @SerializedName("Code")
        public String code;

        @Expose
        @SerializedName("ValidFromUtc")
        public Date validFrom;

        @Expose
        @SerializedName("ValidUntilUtc")
        public Date validUntil;

        @Expose
        @SerializedName("TimesUsed")
        public long timesUsed;

        public boolean isEmptyValue;

        public String emptyText;

        public ExpenseType() {
        }

        public ExpenseType(String emptyText) {
            this.isEmptyValue = true;
            this.name = emptyText;
            this.code = ""; // null makes the equals method strange to implement
        }

        public ExpenseType(Cursor cursor) {
            this.name = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseExpenseTypes.NAME));
            this.code = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseExpenseTypes.CODE));
            this.validFrom = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseExpenseTypes.VALID_FROM)));
            if (!cursor.isNull(cursor.getColumnIndex(ExpenseExpenseTypes.VALID_UNTIL))) {
                this.validUntil = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(
                        ExpenseExpenseTypes.VALID_UNTIL)));
            }
            this.timesUsed = cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseExpenseTypes.TIMES_USED));
        }

        @Override
        public String toString() {
            if (isEmptyValue) {
                return emptyText;
            } else {
                return this.name;
            }
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            if (other instanceof ExpenseType) {
                ExpenseType otherExpenseType = (ExpenseType) other;
                return this.code.equals(otherExpenseType.code);
            } else {
                return false;
            }
        }

        public static ExpenseType mockedData() {
            ExpenseType mockedExpenseType = new ExpenseType();
            final int random = (int) (Math.random() * 10);
            mockedExpenseType.name = "Mocked name " + random;
            mockedExpenseType.code = "Mocked code " + random;

            long now = System.currentTimeMillis();
            mockedExpenseType.validFrom = new Date(now - DateUtils.DAY_IN_MILLIS);
            mockedExpenseType.validUntil = new Date(now + DateUtils.DAY_IN_MILLIS);
            mockedExpenseType.timesUsed = random;

            return mockedExpenseType;
        }

        /**
         * Verifies that the expense type is valid for a certain date.
         * @param date Date to verify against.
         * @return true if the expense type is valid for the specified date, otherwise false.
         */
        public boolean isValid(Date date) {
            if (this.validFrom != null && date.before(this.validFrom)) {
                return false;
            } else if (this.validUntil != null && date.after(this.validUntil)) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeString(this.code);
            dest.writeValue(validFrom);
            dest.writeValue(validUntil);
            dest.writeLong(this.timesUsed);
            dest.writeByte(isEmptyValue ? (byte) 1 : (byte) 0);
            dest.writeString(this.emptyText);
        }

        protected ExpenseType(Parcel in) {
            this.name = in.readString();
            this.code = in.readString();
            this.validFrom = (Date) in.readValue(Date.class.getClassLoader());
            this.validUntil = (Date) in.readValue(Date.class.getClassLoader());
            this.timesUsed = in.readLong();
            this.isEmptyValue = in.readByte() != 0;
            this.emptyText = in.readString();
        }

        public static final Parcelable.Creator<ExpenseType> CREATOR = new Parcelable.Creator<ExpenseType>() {
            public ExpenseType createFromParcel(Parcel source) {
                return new ExpenseType(source);
            }

            public ExpenseType[] newArray(int size) {
                return new ExpenseType[size];
            }
        };
    }

    public static class Currency implements Parcelable {

        @Expose
        @SerializedName("CountryName")
        public String countryName;

        @Expose
        @SerializedName("CurrencyCode")
        public String currencyCode;

        @Expose
        @SerializedName("TimesUsed")
        public long timesUsed;

        @Expose
        @SerializedName("Guid")
        public String guid;

        public boolean isEmptyValue;

        public String emptyText;

        public Currency() {
        }

        public Currency(Cursor cursor) {
            this.countryName = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseCurrencies.COUNTRY_NAME));
            this.currencyCode = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseCurrencies.CURRENCY_CODE));
            this.timesUsed = cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseCurrencies.TIMES_USED));
            this.guid = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseCurrencies.GUID));
        }

        public Currency(String emptyText) {
            this.isEmptyValue = true;
            this.emptyText = emptyText;
            this.guid = ""; // null makes the equals method strange to implement
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            if (other instanceof Currency) {
                Currency otherCurrency = (Currency) other;
                return this.guid.equals(otherCurrency.guid);
            } else {
                return false;
            }
        }

        public static Currency mockedData() {

            Currency mockedCurrency = new Currency();
            mockedCurrency.guid = UUID.randomUUID().toString();

            final int random = (int) (Math.random() * 10);
            mockedCurrency.countryName = "Mocked Country " + random;
            mockedCurrency.currencyCode = "Mocked Currency " + random;
            mockedCurrency.timesUsed = random;

            return mockedCurrency;
        }

        @Override
        public String toString() {
            if (isEmptyValue) {
                return emptyText;
            } else {
                return this.countryName + " (" + this.currencyCode + ")";
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.countryName);
            dest.writeString(this.currencyCode);
            dest.writeLong(this.timesUsed);
            dest.writeString(this.guid);
            dest.writeByte(isEmptyValue ? (byte) 1 : (byte) 0);
            dest.writeString(this.emptyText);
        }

        protected Currency(Parcel in) {
            this.countryName = in.readString();
            this.currencyCode = in.readString();
            this.timesUsed = in.readLong();
            this.guid = in.readString();
            this.isEmptyValue = in.readByte() != 0;
            this.emptyText = in.readString();
        }

        public static final Parcelable.Creator<Currency> CREATOR = new Parcelable.Creator<Currency>() {
            public Currency createFromParcel(Parcel source) {
                return new Currency(source);
            }

            public Currency[] newArray(int size) {
                return new Currency[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(expenseTypes);
        dest.writeTypedList(currencies);
    }

    public Expense() {
    }

    protected Expense(Parcel in) {
        this.expenseTypes = in.createTypedArrayList(ExpenseType.CREATOR);
        this.currencies = in.createTypedArrayList(Currency.CREATOR);
    }

    public static final Parcelable.Creator<Expense> CREATOR = new Parcelable.Creator<Expense>() {
        public Expense createFromParcel(Parcel source) {
            return new Expense(source);
        }

        public Expense[] newArray(int size) {
            return new Expense[size];
        }
    };
}
