package com.visma.blue.misc;

import android.support.v4.app.Fragment;

public interface ChangeFragment {
    public void changeFragmentWithBackStack(Fragment fragment);

    public void changeFragmentWithoutBackStack(Fragment fragment);
}
