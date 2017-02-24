package com.visma.blue.network.containers;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;


public class ServerData implements Parcelable {

    @SerializedName("Name")
    public String name;

    @SerializedName("Url")
    public String url;

    @SerializedName("Developer")
    public boolean developer;

    public ServerData() {
    }

    public ServerData(String name, String url) {
        this.name = name;
        this.url = url;
        developer = false;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.url);
        dest.writeByte(developer ? (byte) 1 : (byte) 0);
    }

    protected ServerData(Parcel in) {
        this.name = in.readString();
        this.url = in.readString();
        this.developer = in.readByte() != 0;

    }

    public static final Parcelable.Creator<ServerData> CREATOR = new Parcelable.Creator<ServerData>() {
        public ServerData createFromParcel(Parcel source) {
            return new ServerData(source);
        }

        public ServerData[] newArray(int size) {
            return new ServerData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
