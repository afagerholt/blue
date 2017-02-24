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
public class SeveraProductAdapterTest {

    private SeveraProductAdapter mSeveraProductAdapter;

    @Before
    public void setup() {
        mSeveraProductAdapter = new SeveraProductAdapter(RuntimeEnvironment.application);
    }

    @Test
    public void shouldCreateAdapterWithEmptyRow() {
        assertTrue("Adapter should have one row!", 1 == mSeveraProductAdapter.getCount());
    }

    @Test
    public void shouldUpdateProducts() {
        mSeveraProductAdapter.updateProducts(RuntimeEnvironment.application,
                CustomMetaData.getCustomProductList());
        assertEquals("Product adapter not updated!",
                CustomMetaData.SEVERA_PRODUCT_COUNT + 1,
                mSeveraProductAdapter.getCount());
    }

    @Test
    public void shouldFindProductPositionByGuid() {
        mSeveraProductAdapter.updateProducts(RuntimeEnvironment.application,
                CustomMetaData.getCustomProductList());
        int productPosition = 1;
        Severa.Product testProduct = mSeveraProductAdapter.getItem(productPosition);
        assertEquals("Product position by guid not found!",
                productPosition,
                mSeveraProductAdapter.getPositionByProductGuid(testProduct.guid));
    }

    @After
    public void tearDown() {
        mSeveraProductAdapter = null;
    }
}
