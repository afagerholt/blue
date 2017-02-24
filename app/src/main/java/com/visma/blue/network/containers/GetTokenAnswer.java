package com.visma.blue.network.containers;

import com.google.gson.annotations.SerializedName;

import com.visma.blue.network.Base;

public class GetTokenAnswer extends Base {
    @SerializedName("Token")
    public String token;
}
