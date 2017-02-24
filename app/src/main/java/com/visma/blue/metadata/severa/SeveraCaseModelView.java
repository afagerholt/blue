package com.visma.blue.metadata.severa;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.visma.blue.network.containers.SeveraCustomData;
import com.visma.blue.network.requests.customdata.Severa;

import java.util.ArrayList;

public class SeveraCaseModelView extends BaseObservable {

    public interface SeveraCaseActionListener {
        void onCaseClick(Severa.Task task);
    }

    private SeveraCaseAdapter mSeveraCaseAdapter;
    private ArrayList<Severa.Case> mCases;
    private SeveraCustomData.Case mSavedCase;
    private SeveraCustomData.Case mSavedCaseFromServer;
    private SeveraCaseActionListener mCaseActionListener;

    public SeveraCaseModelView(SeveraCustomData.Case savedCaseFromServer,
                               ArrayList<Severa.Case> cases,
                               SeveraCaseAdapter severaCaseAdapter,
                               SeveraCaseActionListener severaCaseActionListener) {
        mSavedCaseFromServer = savedCaseFromServer;
        mCases = cases;
        mCaseActionListener = severaCaseActionListener;
        mSeveraCaseAdapter = severaCaseAdapter;
    }

    @Bindable
    public BaseAdapter getSeveraCaseAdapter() {
        return mSeveraCaseAdapter;
    }

    public AdapterView.OnItemClickListener getSeveraCaseItemClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Severa.Case selectedCase = (Severa.Case) parent.getItemAtPosition(position);

                mSavedCase = new SeveraCustomData.Case();
                mSavedCase.name = selectedCase.name;
                mSavedCase.guid = selectedCase.guid;
                mSavedCase.tasks = new ArrayList<>();

                SeveraCustomData.Task taskForSaving = new SeveraCustomData.Task();
                taskForSaving.name = selectedCase.task.name;
                taskForSaving.guid = selectedCase.task.guid;
                taskForSaving.hierarchyLevel = 0;

                mSavedCase.tasks.add(taskForSaving);

                Severa.Task severaTask = selectedCase.task;
                mCaseActionListener.onCaseClick(severaTask);
            }
        };
    }

    public ArrayList<Severa.Case> getCases() {
        return mCases;
    }

    public void setCases(ArrayList<Severa.Case> cases) {
        mCases = cases;
    }

    public SeveraCustomData.Case getSavedCase() {
        return mSavedCase;
    }

    public void setSavedCase(SeveraCustomData.Case savedCase) {
        mSavedCase = savedCase;
    }

    public SeveraCustomData.Case getSavedCaseFromServer() {
        return mSavedCaseFromServer;
    }

    public void filterCases(String queryText) {
        mSeveraCaseAdapter.getFilter().filter(queryText);
    }
}
