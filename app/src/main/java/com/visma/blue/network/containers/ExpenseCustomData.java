package com.visma.blue.network.containers;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.visma.blue.network.requests.customdata.Expense;

public class ExpenseCustomData implements Parcelable {

    @Expose
    @SerializedName("ExpenseType")
    public Expense.ExpenseType expenseType;

    @Expose
    @SerializedName("Currency")
    public Expense.Currency currency;

    public static ExpenseCustomData removeEmptyValues(ExpenseCustomData expenseCustomData) {
        if (expenseCustomData == null) {
            return null;
        }

        ExpenseCustomData filteredExpenseCustomData = null;
        if (expenseCustomData.expenseType != null && !expenseCustomData.expenseType.isEmptyValue) {
            if (filteredExpenseCustomData == null) {
                filteredExpenseCustomData = new ExpenseCustomData();
            }

            filteredExpenseCustomData.expenseType = expenseCustomData.expenseType;
        }

        if (expenseCustomData.currency != null && !expenseCustomData.currency.isEmptyValue) {
            if (filteredExpenseCustomData == null) {
                filteredExpenseCustomData = new ExpenseCustomData();
            }

            filteredExpenseCustomData.currency = expenseCustomData.currency;
        }

        return filteredExpenseCustomData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.expenseType, 0);
        dest.writeParcelable(this.currency, 0);
    }

    public ExpenseCustomData() {
    }

    protected ExpenseCustomData(Parcel in) {
        this.expenseType = in.readParcelable(Expense.ExpenseType.class.getClassLoader());
        this.currency = in.readParcelable(Expense.Currency.class.getClassLoader());
    }

    public static final Parcelable.Creator<ExpenseCustomData> CREATOR = new Parcelable.Creator<ExpenseCustomData>() {
        public ExpenseCustomData createFromParcel(Parcel source) {
            return new ExpenseCustomData(source);
        }

        public ExpenseCustomData[] newArray(int size) {
            return new ExpenseCustomData[size];
        }
    };
}
