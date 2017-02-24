package com.visma.blue.network.requests.customdata;

import com.google.gson.annotations.SerializedName;

import com.visma.blue.network.Base;

public class GetCustomDataAnswer extends Base {

    @SerializedName("Netvisor")
    public Netvisor netvisor;

    @SerializedName("Severa")
    public Severa severa;

    @SerializedName("Expense")
    public Expense expense;
}