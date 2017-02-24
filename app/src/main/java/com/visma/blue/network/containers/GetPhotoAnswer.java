package com.visma.blue.network.containers;

import com.google.gson.annotations.SerializedName;

import com.visma.blue.network.Base;

public class GetPhotoAnswer extends Base {
    @SerializedName("PhotoUrl")
    public String photoUrl;

    public byte[] bitmapData;
}