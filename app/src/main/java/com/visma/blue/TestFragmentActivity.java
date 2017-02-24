package com.visma.blue;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.visma.blue.misc.ChangeFragment;

public class TestFragmentActivity extends AppCompatActivity implements ChangeFragment {
    @Override
    public void changeFragmentWithBackStack(Fragment fragment) {
    }

    @Override
    public void changeFragmentWithoutBackStack(Fragment fragment) {
    }
}
