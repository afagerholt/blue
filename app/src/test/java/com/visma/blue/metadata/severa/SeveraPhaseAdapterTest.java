package com.visma.blue.metadata.severa;

import com.visma.blue.BuildConfig;
import com.visma.blue.custom.CustomApplication;
import com.visma.blue.custom.CustomMetaData;
import com.visma.blue.network.containers.SeveraCustomData;
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
public class SeveraPhaseAdapterTest {

    private SeveraPhaseAdapter mSeveraPhaseAdapter;

    @Test
    public void shouldCreateEmptyTaxAdapter() {
        mSeveraPhaseAdapter = new SeveraPhaseAdapter(RuntimeEnvironment.application,
                new ArrayList<Severa.Task>(), null);
        assertTrue("Phase adapter should be empty!", mSeveraPhaseAdapter.isEmpty());
    }

    @Test
    public void shouldCreateNotEmptyTaxAdapter() {
        mSeveraPhaseAdapter = new SeveraPhaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getMockedSeveraTasks(), null);
        assertFalse("Phase adapter should have items!", mSeveraPhaseAdapter.isEmpty());
    }

    @Test
    public void shouldFilterOnePhase() {
        mSeveraPhaseAdapter = new SeveraPhaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getMockedSeveraTasks(), null);
        Severa.Task testCase = mSeveraPhaseAdapter.getItem(0);
        mSeveraPhaseAdapter.getFilter().filter(testCase.name);
        assertTrue("Cases adapter should have one item!", mSeveraPhaseAdapter.getCount() == 1);
    }

    @Test
    public void shouldReturnTrueWhenCheckingIfTaskEnabled() {
        mSeveraPhaseAdapter = new SeveraPhaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getMockedSeveraTasks(), null);
        Severa.Task testTask = mSeveraPhaseAdapter.getItem(0);
        assertTrue("Task should be enabled!", mSeveraPhaseAdapter.checkIfEnabled(testTask));
    }

    @Test
    public void shouldReturnFalseWhenCheckingIfTaskEnabled() {
        mSeveraPhaseAdapter = new SeveraPhaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getMockedSeveraTasks(), null);
        Severa.Task testTask = mSeveraPhaseAdapter.getItem(0);
        testTask.isLocked = true;
        for (Severa.Task task : testTask.tasks) {
            task.isLocked = true;
        }
        assertFalse("Task should be disabled!", mSeveraPhaseAdapter.checkIfEnabled(testTask));
    }

    @Test
    public void shouldReturnFalseWhenCheckingEmptySavedTask() {
        mSeveraPhaseAdapter = new SeveraPhaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getMockedSeveraTasks(), null);
        assertFalse("Case should be not saved!", mSeveraPhaseAdapter.isSaved(0));
    }

    @Test
    public void shouldReturnFalseWhenCheckingSavedTask() {
        mSeveraPhaseAdapter = new SeveraPhaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getMockedSeveraTasks(), CustomMetaData.getCustomDataSeveraTasks());
        assertFalse("Task should not be saved!", mSeveraPhaseAdapter.isSaved(0));
    }

    @Test
    public void shouldReturnTrueWhenCheckingSavedTask() {
        ArrayList<SeveraCustomData.Task> savedSeveraTasks = CustomMetaData
                .getCustomDataSeveraTasks();
        savedSeveraTasks.get(0).guid = "Severa_task_guid_0";
        mSeveraPhaseAdapter = new SeveraPhaseAdapter(RuntimeEnvironment.application,
                CustomMetaData.getMockedSeveraTasks(), savedSeveraTasks);
        assertTrue("Task should be saved!", mSeveraPhaseAdapter.isSaved(0));
    }

    @Test
    public void shouldReturnTrueWhenCheckingIfCaseEnabled() {
        ArrayList<Severa.Task> testSeveraTasks = CustomMetaData.getMockedSeveraTasks();
        Severa.Task testTask = testSeveraTasks.get(1);
        testTask.isLocked = true;
        for (Severa.Task task : testTask.tasks) {
            task.isLocked = true;
        }
        mSeveraPhaseAdapter = new SeveraPhaseAdapter(RuntimeEnvironment.application,
                testSeveraTasks, null);
        assertTrue("Task should be enabled!", mSeveraPhaseAdapter.isEnabled(0));
    }

    @Test
    public void shouldReturnFalseWhenCheckingIfCaseEnabled() {
        ArrayList<Severa.Task> testSeveraTasks = CustomMetaData.getMockedSeveraTasks();
        Severa.Task testTask = testSeveraTasks.get(1);
        testTask.isLocked = true;
        for (Severa.Task task : testTask.tasks) {
            task.isLocked = true;
        }
        mSeveraPhaseAdapter = new SeveraPhaseAdapter(RuntimeEnvironment.application,
                testSeveraTasks, null);
        assertFalse("Task should be disabled!", mSeveraPhaseAdapter.isEnabled(1));
    }

    @After
    public void tearDown() {
        mSeveraPhaseAdapter = null;
    }
}
