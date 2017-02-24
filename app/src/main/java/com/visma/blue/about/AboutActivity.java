package com.visma.blue.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.visma.blue.R;
import com.visma.blue.misc.ChangeFragment;

public class AboutActivity extends AppCompatActivity implements ChangeFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.blue_activity_about);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Going back from the license fragment does not seem to work unless we add it dynamically
        // and not in the xml.
        setupAboutFragment();
    }

    private void setupAboutFragment() {
        AboutFragment aboutFragment = new AboutFragment();
        String tag = aboutFragment.getClass().getName();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, aboutFragment, tag)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            FragmentManager fm = this.getSupportFragmentManager();
            if (fm.getBackStackEntryCount() == 0) {
                finish();
            } else {
                fm.popBackStack();
            }

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void changeFragmentWithBackStack(Fragment fragment) {
        String tag = fragment.getClass().getName();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    public void changeFragmentWithoutBackStack(Fragment fragment) {
        String tag = fragment.getClass().getName();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, tag)
                .commit();
    }
}