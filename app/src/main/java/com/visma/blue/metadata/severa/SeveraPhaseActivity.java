package com.visma.blue.metadata.severa;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.visma.blue.R;
import com.visma.blue.databinding.BlueActivitySeveraPhaseBinding;
import com.visma.blue.misc.Logger;
import com.visma.blue.network.containers.SeveraCustomData;
import com.visma.blue.network.requests.customdata.Severa;

import java.util.ArrayList;

public class SeveraPhaseActivity extends AppCompatActivity implements SeveraPhaseModelView
        .SeveraPhaseActionListener {

    public static final int CASE_AND_PHASE_ACTIVITIES_COMMUNICATION = 1;

    public static final String SEVERA_DATA = "SEVERA_DATA";
    public static final String SAVED_DATA = "SAVED_DATA";
    public static final String HIERARCHY_LEVEL = "HIERARCHY_LEVEL";
    public static final String SAVED_DATA_FROM_SERVER = "SAVED_DATA_FROM_SERVER";

    private SearchView mSearchView;
    private MenuItem mSearchViewMenuItem;

    private SeveraPhaseModelView mSeveraPhaseModelView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        ArrayList<Severa.Task> phases = null;
        SeveraCustomData.Case savedCasesAndPhases = null;
        ArrayList<SeveraCustomData.Task> savedPhasesFromServer = null;
        int hierarchyLevel = 1;

        if (bundle != null) {
            phases = bundle.getParcelableArrayList(SEVERA_DATA);
            hierarchyLevel = bundle.getInt(HIERARCHY_LEVEL);
            savedCasesAndPhases = bundle.getParcelable(SAVED_DATA);
            savedPhasesFromServer = bundle.getParcelableArrayList(SAVED_DATA_FROM_SERVER);
        }

        setDataBinding(phases, savedCasesAndPhases, savedPhasesFromServer, hierarchyLevel);
        setActionBar();
    }

    private void setDataBinding(ArrayList<Severa.Task> phases,
                                SeveraCustomData.Case savedCasesAndPhases,
                                ArrayList<SeveraCustomData.Task> savedPhasesFromServer,
                                int hierarchyLevel) {

        BlueActivitySeveraPhaseBinding severaPhaseBinding = DataBindingUtil.setContentView(this,
                R.layout.blue_activity_severa_phase);

        mSeveraPhaseModelView = new SeveraPhaseModelView(phases, hierarchyLevel,
                savedPhasesFromServer, savedCasesAndPhases,
                getPhaseAdapter(phases, savedPhasesFromServer), this);
        severaPhaseBinding.setPhaseViewModel(mSeveraPhaseModelView);
    }

    private void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSeveraPhase);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.visma_blue_severa_metadata_phase_activity_title);
        }
    }

    private SeveraPhaseAdapter getPhaseAdapter(ArrayList<Severa.Task> phases,
                                               ArrayList<SeveraCustomData.Task>
                                                       savedPhasesFromServer) {

        return new SeveraPhaseAdapter(this, phases, savedPhasesFromServer);
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
            mSeveraPhaseModelView.setSavedCasesAndPhases((SeveraCustomData.Case) receivedBundle
                    .getParcelable(SAVED_DATA));

            savedBundle.putParcelable(SAVED_DATA, mSeveraPhaseModelView.getSavedCasesAndPhases());
            resultIntent.putExtras(savedBundle);

            setResult(CASE_AND_PHASE_ACTIVITIES_COMMUNICATION, resultIntent);

            finish();
        } else if (resultCode == 0 && mSeveraPhaseModelView.getSavedCasesAndPhases() != null) {
            updateSearchField();

            for (SeveraCustomData.Task customTask
                    : mSeveraPhaseModelView.getSavedCasesAndPhases().tasks) {
                for (Severa.Task task : mSeveraPhaseModelView.getPhases()) {
                    if (customTask.guid.equals(task.guid)) {
                        mSeveraPhaseModelView.getSavedCasesAndPhases().tasks.remove(customTask);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }

        return false;
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
                mSeveraPhaseModelView.filterCases(newText);

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

    public void openPhase(Severa.Task task) {
        Intent intent = new Intent(this, SeveraPhaseActivity.class);
        Bundle bundle = new Bundle();

        if (task.tasks.size() > 0) {
            bundle.putParcelableArrayList(SEVERA_DATA, task.tasks);
            bundle.putParcelable(SAVED_DATA, mSeveraPhaseModelView.getSavedCasesAndPhases());
            bundle.putInt(HIERARCHY_LEVEL, mSeveraPhaseModelView.getHierarchyLevel() + 1);
            bundle.putParcelableArrayList(SAVED_DATA_FROM_SERVER,
                    mSeveraPhaseModelView.getSavedPhasesFromServer());

            intent.putExtras(bundle);

            startActivityForResult(intent, CASE_AND_PHASE_ACTIVITIES_COMMUNICATION);
        } else {
            Intent resultIntent = new Intent();

            bundle.putParcelable(SAVED_DATA, mSeveraPhaseModelView.getSavedCasesAndPhases());

            resultIntent.putExtras(bundle);

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
    public void onPhaseClick(Severa.Task task) {
        openPhase(task);
    }
}
