package com.visma.blue.network.requests.settings;

import com.google.gson.annotations.SerializedName;

public class Settings {

    @SerializedName("ScanDirectlyToSystem")
    public boolean scanDirectlyToSystem;

    @SerializedName("DefaultDocumentType")
    public int defaultDocumentType;
}
