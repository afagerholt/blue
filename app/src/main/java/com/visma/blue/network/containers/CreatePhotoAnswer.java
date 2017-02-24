package com.visma.blue.network.containers;

import com.google.gson.annotations.SerializedName;

import com.visma.blue.network.Base;

public class CreatePhotoAnswer extends Base {
    @SerializedName("PhotoId")
    public String photoGuid;
}