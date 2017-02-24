package com.visma.blue.network.requests.settings;

import com.google.gson.annotations.SerializedName;

import com.visma.blue.network.Base;

public class GetSettingsAnswer extends Base {

    @SerializedName("ScanDirectlyToSystem")
    public boolean scanDirectlyToSystem;

    @SerializedName("DefaultDocumentType")
    public int defaultDocumentType;

    @SerializedName("UsesSupplierInvoiceApproval")
    public boolean usesSupplierInvoiceApproval;
}