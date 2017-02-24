package com.visma.blue.metadata.severa;

import com.visma.blue.BuildConfig;
import com.visma.blue.custom.CustomApplication;
import com.visma.blue.custom.CustomMetaData;
import com.visma.blue.network.requests.customdata.Severa;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = CustomApplication.class)
public class SeveraTaxAdapterTest {

    private SeveraTaxAdapter mSeveraTaxAdapter;

    @Before
    public void setup() {
        mSeveraTaxAdapter = new SeveraTaxAdapter(RuntimeEnvironment.application);
    }

    @Test
    public void shouldCreateAdapterWithEmptyRow() {
        assertTrue("Adapter should have one row!", 1 == mSeveraTaxAdapter.getCount());
    }

    @Test
    public void shouldUpdateTaxes() {
        mSeveraTaxAdapter.updateTaxes(CustomMetaData.getCustomTaxList());
        assertEquals("Taxes adapter not updated!",
                CustomMetaData.SEVERA_TAX_COUNT + 1,
                mSeveraTaxAdapter.getCount());
    }

    @Test
    public void shouldFindPositionByTaxPercentage() {
        mSeveraTaxAdapter.updateTaxes(CustomMetaData.getCustomTaxList());
        int productPosition = 1;
        Severa.Tax testTax = mSeveraTaxAdapter.getItem(productPosition);
        assertEquals("Tax position by percentage not found!",
                productPosition,
                mSeveraTaxAdapter.getPositionByTaxPercentage(testTax.percentage));
    }

    @After
    public void tearDown() {
        mSeveraTaxAdapter = null;
    }
}
