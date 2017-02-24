package com.visma.blue.login.integrations;

import com.visma.blue.BuildConfig;
import com.visma.blue.custom.CustomApplication;
import com.visma.blue.custom.CustomLoginData;
import com.visma.blue.login.integrations.IntegrationData;
import com.visma.blue.login.integrations.IntegrationsAdapter;
import com.visma.blue.misc.VismaUtils;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;


import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = CustomApplication.class)
public class IntegrationsAdapterTest {

    private IntegrationsAdapter mIntegrationsAdapter;

    @Test
    public void shouldCreateIntegrationsAdapterWithoutIntegrationsArray() {
        mIntegrationsAdapter = new IntegrationsAdapter(RuntimeEnvironment.application, null);
        assertNotNull("Integrations adapter should be created!", mIntegrationsAdapter);
    }

    @Test
    public void shouldCreateIntegrationsAdapterWithEmptyIntegrationsArray() {
        mIntegrationsAdapter = new IntegrationsAdapter(RuntimeEnvironment.application,
                new ArrayList<IntegrationData>());
        assertNotNull("Integrations adapter should be created!", mIntegrationsAdapter);
    }

    @Test
    public void shouldCreateIntegrationsAdapterWithoutSelectedIntegration() {
        mIntegrationsAdapter = new IntegrationsAdapter(RuntimeEnvironment.application,
                VismaUtils.getIntegrations(RuntimeEnvironment.application));
        assertNotNull("Integrations adapter should be created!", mIntegrationsAdapter);
    }

    @Test
    public void shouldCreateIntegrationsAdapterWithSelectedIntegration() {
        ArrayList<IntegrationData> integrations = VismaUtils
                .getIntegrations(RuntimeEnvironment.application);
        mIntegrationsAdapter = new IntegrationsAdapter(RuntimeEnvironment.application, integrations);
        assertNotNull("Integrations adapter should be created!", mIntegrationsAdapter);
    }

    @Test
    public void shouldCreateIntegrationsAdapterWithNotExistingIntegration() {
        mIntegrationsAdapter = new IntegrationsAdapter(RuntimeEnvironment.application,
                VismaUtils.getIntegrations(RuntimeEnvironment.application));
        assertNotNull("Integrations adapter should be created!", mIntegrationsAdapter);
    }

    @Test
    public void shouldSortIntegrationsByName() {
        mIntegrationsAdapter = new IntegrationsAdapter(RuntimeEnvironment.application,
                VismaUtils.getIntegrations(RuntimeEnvironment.application));
        int integrationsCount = mIntegrationsAdapter.getCount();
        boolean isIntegrationSorted = true;
        for (int i = 0; i < integrationsCount - 1; i++) {
            if (((IntegrationData) mIntegrationsAdapter.getItem(i)).getName().compareToIgnoreCase(
                    ((IntegrationData) mIntegrationsAdapter.getItem(i + 1)).getName()) >= 0) {
                isIntegrationSorted = false;
                break;
            }
        }
        assertTrue("Integrations should be sorted by name!", isIntegrationSorted);
    }

    @After
    public void tearDown(){
        mIntegrationsAdapter = null;
    }
}
