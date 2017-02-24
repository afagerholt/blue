package com.visma.blue.network;

import com.google.gson.annotations.SerializedName;

public class Base {
    @SerializedName("Response")
    public int response = OnlineResponseCodes.NotSet;
    @SerializedName("Message")
    public String message;
}
