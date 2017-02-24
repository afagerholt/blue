package com.visma.blue.login;

import android.app.Fragment;

public interface ChangeFragment {
    public void changeFragmentWithBackStack(Fragment fragment);

    public void changeFragmentWithoutBackStack(Fragment fragment);
}
