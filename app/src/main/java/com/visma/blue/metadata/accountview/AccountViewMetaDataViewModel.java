package com.visma.blue.metadata.accountview;

import android.app.FragmentManager;
import android.content.Context;

import com.visma.blue.metadata.BaseMetaDataViewModel;
import com.visma.blue.network.containers.OnlineMetaData;

public class AccountViewMetaDataViewModel extends BaseMetaDataViewModel {

    public AccountViewMetaDataViewModel(FragmentManager fragmentManager,
                                        OnlineMetaData onlineMetaData,
                                        Context context,
                                        boolean hasBitmap) {
        super(fragmentManager, onlineMetaData, context, hasBitmap);
    }
}
