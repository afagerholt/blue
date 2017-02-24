package com.visma.blue.network.containers;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

public class SeveraCustomData implements Parcelable {

    @Expose
    @SerializedName("StartDateUtc")
    public Date startDateUtc;

    @Expose
    @SerializedName("EndDateUtc")
    public Date endDateUtc;

    @Expose
    @SerializedName("Product")
    public Product product;

    // "case" is a reserved word...
    @Expose
    @SerializedName("Case")
    public Case severaCase;

    @Expose
    @SerializedName("VatPercentage")
    public Double vatPercentage;

    public static SeveraCustomData removeEmptyValues(SeveraCustomData severaCustomData) {
        if (severaCustomData == null) {
            return null;
        }

        SeveraCustomData filteredSeveraCustomData = new SeveraCustomData();
        filteredSeveraCustomData.startDateUtc = severaCustomData.startDateUtc;
        filteredSeveraCustomData.endDateUtc = severaCustomData.endDateUtc;
        filteredSeveraCustomData.severaCase = severaCustomData.severaCase;
        filteredSeveraCustomData.product = severaCustomData.product;

        if (severaCustomData.product != null) {
            if (severaCustomData.vatPercentage >= 0) {
                filteredSeveraCustomData.vatPercentage = severaCustomData.vatPercentage;
            }

            if (severaCustomData.endDateUtc == null
                    && severaCustomData.product.useStartAndEndTime) {
                filteredSeveraCustomData.endDateUtc = severaCustomData.startDateUtc;
            }
        } else {
            filteredSeveraCustomData.endDateUtc = null;
            filteredSeveraCustomData.vatPercentage = null;
        }

        return filteredSeveraCustomData;
    }

    public static class Product implements Parcelable {
        @Expose
        @SerializedName("Guid")
        public String guid;

        @Expose
        @SerializedName("Name")
        public String name;

        @Expose
        @SerializedName("UseStartAndEndTime")
        public boolean useStartAndEndTime;

        public Product() {
        }

        public Product(String guid, String name, boolean useStartAndEndTime) {
            this.guid = guid;
            this.name = name;
            this.useStartAndEndTime = useStartAndEndTime;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.guid);
            dest.writeString(this.name);
            dest.writeByte(useStartAndEndTime ? (byte) 1 : (byte) 0);
        }

        protected Product(Parcel in) {
            this.guid = in.readString();
            this.name = in.readString();
            this.useStartAndEndTime = in.readByte() != 0;
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

    public static class Case implements Parcelable {
        @Expose
        @SerializedName("Guid")
        public String guid;

        @Expose
        @SerializedName("Name")
        public String name;

        @Expose
        @SerializedName("Tasks")
        public ArrayList<Task> tasks;

        public Case() {
        }

        public Case(String guid, String name, ArrayList<Task> tasks) {
            this.guid = guid;
            this.name = name;
            this.tasks = tasks;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.guid);
            dest.writeString(this.name);
            dest.writeTypedList(tasks);
        }

        protected Case(Parcel in) {
            this.guid = in.readString();
            this.name = in.readString();
            this.tasks = in.createTypedArrayList(Task.CREATOR);
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
        @Expose
        @SerializedName("Guid")
        public String guid;

        @Expose
        @SerializedName("Name")
        public String name;

        @Expose
        @SerializedName("HierarchyLevel")
        public int hierarchyLevel;

        public Task() {
        }

        public Task(String guid, String name, int hierarchyLevel) {
            this.guid = guid;
            this.name = name;
            this.hierarchyLevel = hierarchyLevel;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.guid);
            dest.writeString(this.name);
            dest.writeInt(this.hierarchyLevel);
        }

        protected Task(Parcel in) {
            this.guid = in.readString();
            this.name = in.readString();
            this.hierarchyLevel = in.readInt();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.startDateUtc);
        dest.writeValue(this.endDateUtc);
        dest.writeValue(this.vatPercentage);
        dest.writeParcelable(this.product, 0);
        dest.writeParcelable(this.severaCase, 0);
    }

    public SeveraCustomData() {
    }

    protected SeveraCustomData(Parcel in) {
        this.startDateUtc = (Date) in.readValue(Date.class.getClassLoader());
        this.endDateUtc = (Date) in.readValue(Date.class.getClassLoader());
        this.vatPercentage = (Double) in.readValue(Double.class.getClassLoader());
        this.product = in.readParcelable(Product.class.getClassLoader());
        this.severaCase = in.readParcelable(Case.class.getClassLoader());
    }

    public static final Creator<SeveraCustomData> CREATOR = new Creator<SeveraCustomData>() {
        public SeveraCustomData createFromParcel(Parcel source) {
            return new SeveraCustomData(source);
        }

        public SeveraCustomData[] newArray(int size) {
            return new SeveraCustomData[size];
        }
    };
}
