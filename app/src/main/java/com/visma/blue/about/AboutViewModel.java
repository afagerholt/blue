package com.visma.blue.about;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.visma.blue.BR;
import com.visma.blue.BlueConfig;
import com.visma.blue.EmailPresentationDialog;
import com.visma.blue.LicensesFragment;
import com.visma.blue.misc.ChangeFragment;
import com.visma.blue.misc.VismaUtils;

public class AboutViewModel extends BaseObservable {

    public interface DownloadEmailInterface {
        void downloadInboundEmailAddress();
    }

    private Context mContext;
    private ChangeFragment mChangeFragmentCallback;
    private FragmentManager mFragmentManager;
    private EmailPresentationDialog mEmailPresentationDialog;

    public AboutViewModel(@NonNull Context context,
                          @NonNull ChangeFragment changeFragmentCallback,
                          @NonNull FragmentManager fragmentManager) {
        mContext = context;
        mChangeFragmentCallback = changeFragmentCallback;
        mFragmentManager = fragmentManager;

        mEmailPresentationDialog = new EmailPresentationDialog();
    }

    public String getCompany() {
        return VismaUtils.getCurrentCompany(mContext);
    }

    public int getCompanyVisibility() {
        if (VismaUtils.isDemoMode(mContext)) {
            return View.GONE;
        } else {
            return View.VISIBLE;
        }
    }

    public String getVersion() {
        String versionName = null;
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),
                    PackageManager.GET_META_DATA);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    public View.OnClickListener getLicensesOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LicensesFragment licensesFragment = new LicensesFragment();
                mChangeFragmentCallback.changeFragmentWithBackStack(licensesFragment);
            }
        };
    }
}
