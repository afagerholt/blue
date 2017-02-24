package com.visma.blue.metadata.mamut;

import android.app.FragmentManager;
import android.content.Context;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.visma.blue.BR;
import com.visma.blue.metadata.BaseMetaDataViewModel;
import com.visma.blue.metadata.MetadataFragment;
import com.visma.blue.metadata.TypePickerDialog;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.OnlinePhotoType;
import com.visma.blue.network.containers.OnlineMetaData;

public class MamutMetaDataViewModel extends BaseMetaDataViewModel {

    private TypePickerDialog mTypePickerDialog;
    private android.support.v4.app.FragmentManager mSupportFragManager;
    private InputMethodManager mInputMethodManager;

    public MamutMetaDataViewModel(FragmentManager fragmentManager,
                                  android.support.v4.app.FragmentManager supportFragManager,
                                  OnlineMetaData onlineMetaData,
                                  InputMethodManager inputMethodManager,
                                  Fragment mainFragment,
                                  Context context,
                                  boolean hasBitmap) {
        super(fragmentManager, onlineMetaData, context, hasBitmap);
        mSupportFragManager = supportFragManager;
        mInputMethodManager = inputMethodManager;
        mTypePickerDialog = createTypePickerDialog(mainFragment);
    }

    @Bindable
    public int getType() {
        return VismaUtils.getTypeTextId(mOnlineMetaData.type);
    }

    private TypePickerDialog createTypePickerDialog(Fragment targetFragment) {
        TypePickerDialog typePickerDialog = new TypePickerDialog();
        typePickerDialog.setTargetFragment(targetFragment, MetadataFragment.REQUEST_CODE_TYPE);
        return typePickerDialog;
    }

    public void setType(int type) {
        mOnlineMetaData.type = type;
        notifyPropertyChanged(BR.type);
    }

    public View.OnClickListener getTypeChangeOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                closeKeyboard(view);
                showTypePickerDialog();
            }
        };
    }

    private void closeKeyboard(View view) {
        mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void showTypePickerDialog() {
        final String tag = "TypePickerDialog";

        synchronized (mTypePickerDialog) {
            if (mSupportFragManager.findFragmentByTag(tag) != null) {
                return;
            }
            Bundle args = new Bundle();
            args.putInt(TypePickerDialog.EXTRA_TYPE, mOnlineMetaData.type);
            mTypePickerDialog.setArguments(args);
            mTypePickerDialog.show(mSupportFragManager, tag);
            // If we want to query the fragment manager for the fragment we need the fragment to be added immediately
            mSupportFragManager.executePendingTransactions();
        }
    }

    public void updateTypeChange(final int newType) {
        if (newType == OnlinePhotoType.INVOICE.getValue()) {
            mOnlineMetaData.type = OnlinePhotoType.INVOICE.getValue();
            setType(OnlinePhotoType.INVOICE.getValue());
        } else if (newType == OnlinePhotoType.RECEIPT.getValue()) {
            mOnlineMetaData.type = OnlinePhotoType.RECEIPT.getValue();
            setType(OnlinePhotoType.RECEIPT.getValue());
        }
    }
}