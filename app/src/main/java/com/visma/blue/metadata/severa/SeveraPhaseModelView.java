package com.visma.blue.metadata.severa;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.visma.blue.network.containers.SeveraCustomData;
import com.visma.blue.network.requests.customdata.Severa;

import java.util.ArrayList;

public class SeveraPhaseModelView extends BaseObservable {

    public interface SeveraPhaseActionListener {
        void onPhaseClick(Severa.Task task);
    }

    private SeveraPhaseAdapter mSeveraPhaseAdapter;
    private ArrayList<Severa.Task> mPhases;
    private SeveraCustomData.Case mSavedCasesAndPhases;
    private SeveraCustomData.Task mSavedTask;
    private SeveraPhaseActionListener mPhaseActionListener;

    private ArrayList<SeveraCustomData.Task> mSavedPhasesFromServer;

    private int mHierarchyLevel;

    public SeveraPhaseModelView(ArrayList<Severa.Task> phases, int hierarchyLevel,
                                ArrayList<SeveraCustomData.Task> savedPhasesFromServer,
                                SeveraCustomData.Case savedCasesAndPhases,
                                SeveraPhaseAdapter severaPhaseAdapter,
                                SeveraPhaseActionListener phaseActionListener) {
        mPhases = phases;
        mHierarchyLevel = hierarchyLevel;
        mSavedPhasesFromServer = savedPhasesFromServer;
        mSavedCasesAndPhases = savedCasesAndPhases;
        mSeveraPhaseAdapter = severaPhaseAdapter;
        mPhaseActionListener = phaseActionListener;
    }

    @Bindable
    public String getHeaderCase() {
        if (mSavedCasesAndPhases == null) {
            return "";
        }
        return mSavedCasesAndPhases.name;
    }

    @Bindable
    public BaseAdapter getSeveraPhaseAdapter() {
        return mSeveraPhaseAdapter;
    }

    public AdapterView.OnItemClickListener getSeveraPhaseItemClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Severa.Task selectedTask = (Severa.Task) parent.getItemAtPosition(position);

                mSavedTask = new SeveraCustomData.Task();
                mSavedTask.name = selectedTask.name;
                mSavedTask.guid = selectedTask.guid;
                mSavedTask.hierarchyLevel = mHierarchyLevel;

                mSavedCasesAndPhases.tasks.add(mSavedTask);
                mPhaseActionListener.onPhaseClick(selectedTask);
            }
        };
    }

    @Bindable
    public int getSelectedPhasesVisibility() {
        if (mSavedCasesAndPhases.tasks != null && mSavedCasesAndPhases.tasks.size() > 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    @Bindable
    public String getSelectedPhases() {
        String phasesNames = "";
        if (mSavedCasesAndPhases.tasks != null && mSavedCasesAndPhases.tasks.size() > 1) {
            phasesNames = mSavedCasesAndPhases.tasks.get(1).name;

            for (SeveraCustomData.Task task : mSavedCasesAndPhases.tasks) {
                if (task.hierarchyLevel > 1) {
                    phasesNames += "\n" + task.name;
                }
            }
        }

        return phasesNames;
    }

    public void filterCases(String queryText) {
        mSeveraPhaseAdapter.getFilter().filter(queryText);
    }

    public int getHierarchyLevel() {
        return mHierarchyLevel;
    }

    public ArrayList<Severa.Task> getPhases() {
        return mPhases;
    }

    public SeveraCustomData.Case getSavedCasesAndPhases() {
        return mSavedCasesAndPhases;
    }

    public void setSavedCasesAndPhases(SeveraCustomData.Case savedCasesAndPhases) {
        mSavedCasesAndPhases = savedCasesAndPhases;
    }

    public ArrayList<SeveraCustomData.Task> getSavedPhasesFromServer() {
        return mSavedPhasesFromServer;
    }
}