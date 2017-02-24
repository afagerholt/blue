package com.visma.blue.network.containers;

import com.google.gson.annotations.SerializedName;

import com.visma.blue.network.Base;

import java.util.ArrayList;

public class GetMetadataListAnswer extends Base {

    @SerializedName("MetaData")
    public ArrayList<OnlineMetaData> metaDataList;
}
