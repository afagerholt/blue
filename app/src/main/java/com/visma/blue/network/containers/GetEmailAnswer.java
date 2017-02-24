package com.visma.blue.network.containers;

import com.google.gson.annotations.SerializedName;

import com.visma.blue.network.Base;

public class GetEmailAnswer extends Base {
    @SerializedName("InboundEmail")
    public String inboundEmail;
}