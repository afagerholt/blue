package com.visma.blue.network.containers;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import com.visma.blue.network.Base;

import java.util.ArrayList;

public class GetCompaniesAnswer extends Base {

    @SerializedName("OnlineCustomers")
    public ArrayList<OnlineCustomer> onlineCustomers;

    @SerializedName("Token")
    public String token;

    @SerializedName("UserId")
    public String userId;

    public static class OnlineCustomer implements Parcelable {

        @SerializedName("Id")
        public String companyId;

        @SerializedName("Name")
        public String companyDisplayName;

        @SerializedName("CountryCodeAlpha2")
        public String countryCodeAlpha2;

        @SerializedName("OrgNo")
        public String orgNo;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(this.companyId);
            out.writeString(this.companyDisplayName);
            out.writeString(this.countryCodeAlpha2);
            out.writeString(this.orgNo);
        }

        public static final Creator<OnlineCustomer> CREATOR = new Creator<OnlineCustomer>() {
            public OnlineCustomer createFromParcel(Parcel in) {
                return new OnlineCustomer(in);
            }

            public OnlineCustomer[] newArray(int size) {
                return new OnlineCustomer[size];
            }
        };

        private OnlineCustomer(Parcel in) {
            this.companyId = in.readString();
            this.companyDisplayName = in.readString();
            this.countryCodeAlpha2 = in.readString();
            this.orgNo = in.readString();
        }
    }
}

