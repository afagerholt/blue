package com.visma.blue.login.integrations;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

public class IntegrationData implements Parcelable {
    private String name;
    private String description;
    private int iconResId;
    private int id;

    public IntegrationData(String name, String description, @DrawableRes int iconResId, int id) {
        this.name = name;
        this.description = description;
        this.iconResId = iconResId;
        this.id = id;
    }

    protected IntegrationData(Parcel in) {
        name = in.readString();
        description = in.readString();
        iconResId = in.readInt();
        id = in.readInt();
    }

    public static final Creator<IntegrationData> CREATOR = new Creator<IntegrationData>() {
        @Override
        public IntegrationData createFromParcel(Parcel in) {
            return new IntegrationData(in);
        }

        @Override
        public IntegrationData[] newArray(int size) {
            return new IntegrationData[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeInt(this.iconResId);
        dest.writeInt(this.id);
    }
}
