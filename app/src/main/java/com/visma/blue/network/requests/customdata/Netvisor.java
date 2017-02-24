package com.visma.blue.network.requests.customdata;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import com.visma.blue.provider.NetvisorPayloads;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Netvisor {

    @SerializedName("Payload")
    public ArrayList<DropDown> dropDowns;

    public static class DropDown implements Parcelable {
        private static final Gson sGson;
        private static final Type sType;

        static {
            sGson = new GsonBuilder().create();
            sType = new TypeToken<ArrayList<Value>>() {}.getType();
        }

        @SerializedName("Id")
        public String id;
        @SerializedName("Type")
        public int type;
        @SerializedName("Title")
        public String title;
        @SerializedName("Values")
        public ArrayList<Value> values;

        public DropDown() {
        }

        public DropDown(Cursor cursor) {
            this.id = cursor.getString(cursor.getColumnIndexOrThrow(NetvisorPayloads.PAYLOAD_ID));
            this.type = cursor.getInt(cursor.getColumnIndexOrThrow(NetvisorPayloads.TYPE));
            this.title = cursor.getString(cursor.getColumnIndexOrThrow(NetvisorPayloads.TITLE));

            if (!cursor.isNull(cursor.getColumnIndex(NetvisorPayloads.VALUES))) {
                this.values = sGson.fromJson(cursor.getString(cursor
                        .getColumnIndexOrThrow(NetvisorPayloads.VALUES)), sType);
            }
        }

        @Override
        public String toString() {
            return this.title;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(this.id);
            out.writeInt(this.type);
            out.writeString(this.title);
            out.writeTypedList(this.values);
        }

        public static final Creator<DropDown> CREATOR = new Creator<DropDown>() {
            public DropDown createFromParcel(Parcel in) {
                return new DropDown(in);
            }

            public DropDown[] newArray(int size) {
                return new DropDown[size];
            }
        };

        private DropDown(Parcel in) {
            this.id = in.readString();
            this.type = in.readInt();
            this.title = in.readString();
            in.readTypedList(this.values, Value.CREATOR);
        }
    }

    public static class Value implements Parcelable {
        @Expose
        @SerializedName("Id")
        public String id;
        @Expose
        @SerializedName("Title")
        public String title;
        @Expose
        @SerializedName("SubTitle")
        public String subTitle;

        public boolean isEmptyValue;

        public Value(String id, String title, String subTitle) {
            this(id, title, subTitle, false);
        }

        public Value(String id, String title, String subTitle, boolean isEmptyValue) {
            this.id = id;
            this.title = title;
            this.subTitle = subTitle;
            this.isEmptyValue = isEmptyValue;
        }

        @Override
        public String toString() {
            return this.title;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(this.id);
            out.writeString(this.title);
            out.writeString(this.subTitle);
            out.writeInt(isEmptyValue ? 1 : 0);
        }

        public static final Creator<Value> CREATOR = new Creator<Value>() {
            public Value createFromParcel(Parcel in) {
                return new Value(in);
            }

            public Value[] newArray(int size) {
                return new Value[size];
            }
        };

        private Value(Parcel in) {
            this.id = in.readString();
            this.title = in.readString();
            this.subTitle = in.readString();
            this.isEmptyValue = in.readInt() == 1;
        }
    }
}
