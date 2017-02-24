package com.visma.blue.main;

import com.visma.blue.BuildConfig;
import com.visma.blue.archive.adapter.MetadataAdapter;
import com.visma.blue.custom.CustomApplication;
import com.visma.blue.custom.CustomCursorData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = CustomApplication.class)
public class ArchiveAdapterTest {

    private MetadataAdapter mMetadaAdapter;

    @Test
    public void shouldCreateEmptyAdapter() {
        // when
        mMetadaAdapter = new MetadataAdapter(RuntimeEnvironment.application, CustomCursorData
                .getEmptyCursor());

        // then
        assertNotNull("Should have created empty metadata adapter", mMetadaAdapter);
    }

    @Test
    public void shouldHandleAdapterUpdateWithEmptyArray() {
        // when
        mMetadaAdapter = new MetadataAdapter(RuntimeEnvironment.application, CustomCursorData
                .getEmptyCursor());
        mMetadaAdapter.swapCursor(CustomCursorData.getEmptyCursor());
        // then
        assertTrue("Adapter should be empty!", mMetadaAdapter.getCount() == 0);
    }

    @Test
    public void shouldHandleAdapterUpdateWithFilledArray() {
        // when
        mMetadaAdapter = new MetadataAdapter(RuntimeEnvironment.application, CustomCursorData
                .getEmptyCursor());
        mMetadaAdapter.swapCursor(CustomCursorData.getFilledCursor());

        // then
        assertTrue("Adapter should not be empty!", mMetadaAdapter.getCount() > 0);
    }


    @Test
    public void shouldFilterNoItemsInAdapter() {
        // when
        mMetadaAdapter = new MetadataAdapter(RuntimeEnvironment.application, CustomCursorData
                .getEmptyCursor());
        mMetadaAdapter.swapCursor(CustomCursorData.getFilledCursor());
        mMetadaAdapter.getFilter().filter("Filter no items");

        // then
        assertTrue("Filter should have filtered no items!", mMetadaAdapter.getCount() == 0);
    }

    @Test
    public void shouldFilterAllItemsWithEmptyFraze() {
        // when
        mMetadaAdapter = new MetadataAdapter(RuntimeEnvironment.application, CustomCursorData
                .getEmptyCursor());
        mMetadaAdapter.swapCursor(CustomCursorData.getFilledCursor());
        mMetadaAdapter.getFilter().filter("");

        // then
        assertTrue("Filter should have filtered no items!", mMetadaAdapter.getCount()
                == 0);
    }

    @Test
    public void shouldFilterAllItemsWithNullFraze() {
        // when
        mMetadaAdapter = new MetadataAdapter(RuntimeEnvironment.application, CustomCursorData
                .getEmptyCursor());
        mMetadaAdapter.swapCursor(CustomCursorData.getFilledCursor());
        mMetadaAdapter.getFilter().filter(null);

        // then
        assertTrue("Filter should have filtered no items!", mMetadaAdapter.getCount() == 0);
    }
}
