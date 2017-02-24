package com.visma.blue.metadata.severa;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.visma.blue.R;
import com.visma.blue.databinding.BlueActivitySeveraCaseBinding;
import com.visma.blue.misc.Logger;
import com.visma.blue.network.containers.SeveraCustomData;
import com.visma.blue.network.requests.customdata.Severa;

import java.util.ArrayList;

public class SeveraCaseActivity extends AppCompatActivity implements SeveraCaseModelView
        .SeveraCaseActionListener {

    public static final int CASE_AND_PHASE_ACTIVITIES_COMMUNICATION = 1;

    public static final String SEVERA_DATA = "SEVERA_DATA";
    public static final String SAVED_DATA = "SAVED_DATA";
    public static final String HIERARCHY_LEVEL = "HIERARCHY_LEVEL";
    public static final String SAVED_DATA_FROM_SERVER = "SAVED_DATA_FROM_SERVER";

    private SearchView mSearchView;
    private MenuItem mSearchViewMenuItem;

    private SeveraCaseModelView mSeveraCaseModelView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        ArrayList<Severa.Case> cases = null;
        SeveraCustomData.Case savedCaseFromServer = null;
        if (bundle != null) {
            cases = bundle.getParcelableArrayList(SEVERA_DATA);
            savedCaseFromServer = bundle.getParcelable(SAVED_DATA_FROM_SERVER);
        }
        setDataBinding(savedCaseFromServer, cases);
        setActionBar();
    }

    private void setDataBinding(SeveraCustomData.Case savedCaseFromServer,
                                ArrayList<Severa.Case> cases) {

        BlueActivitySeveraCaseBinding severaCaseBinding = DataBindingUtil.setContentView(this, R
                .layout.blue_activity_severa_case);

        mSeveraCaseModelView = new SeveraCaseModelView(savedCaseFromServer, cases,
                getCaseAdapter(savedCaseFromServer, cases), this);
        severaCaseBinding.setCaseViewModel(mSeveraCaseModelView);
    }

    private SeveraCaseAdapter getCaseAdapter(SeveraCustomData.Case savedCaseFromServer,
                                             ArrayList<Severa.Case> cases) {
        return new SeveraCaseAdapter(this, cases, savedCaseFromServer);
    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.visma_blue_severa_metadata_select_case);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == CASE_AND_PHASE_ACTIVITIES_COMMUNICATION) {
            if (data == null) {
                return;
            }

            Bundle receivedBundle = data.getExtras();

            if (receivedBundle == null) {
                return;
            }

            Intent resultIntent = new Intent();
            Bundle savedBundle = new Bundle();

            mSeveraCaseModelView.setSavedCase(
                    (SeveraCustomData.Case) receivedBundle.getParcelable(SAVED_DATA));

            savedBundle.putParcelable(SAVED_DATA, mSeveraCaseModelView.getSavedCase());
            resultIntent.putExtras(savedBundle);
            setResult(CASE_AND_PHASE_ACTIVITIES_COMMUNICATION, resultIntent);

            finish();
        } else if (resultCode == 0 && mSeveraCaseModelView.getSavedCase() != null) {
            mSeveraCaseModelView.setSavedCase(null);
            updateSearchField();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.blue_activity_severa_search, menu);

        mSearchViewMenuItem = menu.findItem(R.id.blue_activity_severa_search);
        mSearchView = (SearchView) mSearchViewMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSeveraCaseModelView.filterCases(newText);
                return true;
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                    mSearchView.setQuery(null, true);
                }
                return true;
            }
        });

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Logger.logAction(Logger.ACTION_SEARCH);
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }

        return false;
    }

    public void openPhase(Severa.Task severaTask) {
        Intent intent = new Intent(this, SeveraPhaseActivity.class);
        Bundle bundle = new Bundle();

        if (severaTask.tasks.size() > 0) {
            bundle.putParcelableArrayList(SEVERA_DATA, severaTask.tasks);
            bundle.putInt(HIERARCHY_LEVEL, 1);
            bundle.putParcelable(SAVED_DATA, mSeveraCaseModelView.getSavedCase());

            ArrayList<SeveraCustomData.Task> tasksFromServer = null;

            if (mSeveraCaseModelView.getSavedCaseFromServer() != null && mSeveraCaseModelView
                    .getSavedCaseFromServer().tasks != null
                    && mSeveraCaseModelView.getSavedCaseFromServer().tasks.size() > 0) {
                tasksFromServer = mSeveraCaseModelView.getSavedCaseFromServer().tasks;
            }

            bundle.putParcelableArrayList(SAVED_DATA_FROM_SERVER, tasksFromServer);

            intent.putExtras(bundle);

            startActivityForResult(intent, CASE_AND_PHASE_ACTIVITIES_COMMUNICATION);
        } else {
            Intent resultIntent = new Intent();
            Bundle savedBundle = new Bundle();

            savedBundle.putParcelable(SAVED_DATA, mSeveraCaseModelView.getSavedCase());

            resultIntent.putExtras(savedBundle);

            setResult(CASE_AND_PHASE_ACTIVITIES_COMMUNICATION, resultIntent);

            // Cannot use updateSearchField() method here because it causes a flash of unfiltered
            // list
            // if list is filtered at this moment. Use keyboard hiding instead
            hideSearchKeyboard();

            finish();
        }
    }

    public void updateSearchField() {
        if (mSearchViewMenuItem != null && mSearchViewMenuItem.isActionViewExpanded()) {
            mSearchViewMenuItem.collapseActionView();
        }
    }

    public void hideSearchKeyboard() {
        if (mSearchViewMenuItem != null && mSearchViewMenuItem.isActionViewExpanded()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), InputMethodManager
                    .HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onCaseClick(Severa.Task task) {
        openPhase(task);
    }
}
