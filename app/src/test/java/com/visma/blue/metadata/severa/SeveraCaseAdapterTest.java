package com.visma.blue.metadata.severa;

import com.visma.blue.BuildConfig;
import com.visma.blue.custom.CustomApplication;
import com.visma.blue.custom.CustomMetaData;
import com.visma.blue.network.requests.customdata.Severa;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;


import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = CustomApplication.class)
public class SeveraCaseAdapterTest {

    private SeveraCaseAdapter mSeveraCaseAdapter;

    @Test
    public void shouldCreateAdapterWithEmptyCase() {
        mSeveraCaseAdapter = new SeveraCaseAdapter(RuntimeEnvironment.application,
                new ArrayList<Severa.Case>(), null);
        assertTrue("Adapter should have one item!", mSeveraCaseAdapter.getCount() == 1);
    }

    @Test
    public void shouldCreateNotEmptyCaseAdapter() {
        mSeveraCaseAdapter = new SeveraCaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getCustomSeveraCases(), null);
        assertFalse("Adapter should have items!", mSeveraCaseAdapter.isEmpty());
    }

    @Test
    public void shouldFilterOneCase() {
        mSeveraCaseAdapter = new SeveraCaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getCustomSeveraCases(), null);
        Severa.Case testCase = mSeveraCaseAdapter.getItem(0);
        mSeveraCaseAdapter.getFilter().filter(testCase.name);
        assertTrue("Cases adapter should have one item!", mSeveraCaseAdapter.getCount() == 1);
    }

    @Test
    public void shouldReturnTrueWhenCheckingIfTaskEnabled() {
        mSeveraCaseAdapter = new SeveraCaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getCustomSeveraCases(), null);
        Severa.Case testCase = mSeveraCaseAdapter.getItem(1);
        assertTrue("Task should be enabled!", mSeveraCaseAdapter
                .checkIfEnabled(testCase.task));
    }

    @Test
    public void shouldReturnFalseWhenCheckingIfTaskEnabled() {
        mSeveraCaseAdapter = new SeveraCaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getCustomSeveraCases(), null);
        Severa.Case testCase = mSeveraCaseAdapter.getItem(1);
        testCase.task.isLocked = true;
        for (Severa.Task task : testCase.task.tasks) {
            task.isLocked = true;
        }
        assertFalse("Task should be disabled!", mSeveraCaseAdapter
                .checkIfEnabled(testCase.task));
    }

    @Test
    public void shouldReturnTrueWhenCheckingEmptyCaseAdapter() {
        mSeveraCaseAdapter = new SeveraCaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getCustomSeveraCases(), null);
        assertTrue("Case should be saved!", mSeveraCaseAdapter
                .isSaved(0));
    }

    @Test
    public void shouldReturnFalseWhenCheckingSavedCase() {
        mSeveraCaseAdapter = new SeveraCaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getCustomSeveraCases(), CustomMetaData.getCustomCasePhase());
        assertFalse("Case should be not saved!", mSeveraCaseAdapter.isSaved(0));
    }

    @Test
    public void shouldReturnTrueWhenCheckingSavedCase() {
        mSeveraCaseAdapter = new SeveraCaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getCustomSeveraCases(), CustomMetaData.getCustomCasePhase());
        assertTrue("Case should be saved!", mSeveraCaseAdapter.isSaved(1));
    }

    @Test
    public void shouldReturnTrueWhenCheckingIfCaseEnabled() {
        ArrayList<Severa.Case> testSeveraCases = CustomMetaData.getCustomSeveraCases();
        Severa.Case testCase = testSeveraCases.get(0);
        testCase.task.isLocked = true;
        for (Severa.Task task : testCase.task.tasks) {
            task.isLocked = true;
        }
        mSeveraCaseAdapter = new SeveraCaseAdapter(RuntimeEnvironment.application,
                testSeveraCases, null);
        assertTrue("Case should be enabled!", mSeveraCaseAdapter.isEnabled(2));
    }

    @Test
    public void shouldReturnFalseWhenCheckingIfCaseEnabled() {
        ArrayList<Severa.Case> testSeveraCases = CustomMetaData.getCustomSeveraCases();
        Severa.Case testCase = testSeveraCases.get(0);
        testCase.task.isLocked = true;
        for (Severa.Task task : testCase.task.tasks) {
            task.isLocked = true;
        }
        mSeveraCaseAdapter = new SeveraCaseAdapter(RuntimeEnvironment.application,
                testSeveraCases, null);
        assertFalse("Case should be disabled!", mSeveraCaseAdapter.isEnabled(1));
    }

    @After
    public void tearDown() {
        mSeveraCaseAdapter = null;
    }
}
