package com.visma.blue.metadata;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.metadata.accountview.AccountViewMetadataFragment;
import com.visma.blue.metadata.eaccounting.EAcountingMetadataFragment;
import com.visma.blue.metadata.expense.ExpenseMetadataFragment;
import com.visma.blue.metadata.mamut.MamutMetadataFragment;
import com.visma.blue.metadata.netvisor.NetvisorMetadaFragment;
import com.visma.blue.metadata.severa.SeveraMetadataFragment;
import com.visma.blue.metadata.vismaonline.VismaOnlineMetadataFragment;
import com.visma.blue.misc.ChangeFragment;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.containers.OnlineMetaData;

public class MetadataActivity extends AppCompatActivity implements ChangeFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.blue_activity_metadata);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        setupMetadataFragment(extras);
    }

    private void setupMetadataFragment(Bundle extras) {
        if (extras != null) {
            Bundle args = new Bundle();

            OnlineMetaData onlineMetadata = extras.getParcelable(BaseMetadataFragment.EXTRA_DATA_METADATA);
            if (extras.containsKey(BaseMetadataFragment.EXTRA_DATA_METADATA_DOCUMENT_CREATION_DATE)) {
                long documentCreationDate = extras.getLong(BaseMetadataFragment.EXTRA_DATA_METADATA_DOCUMENT_CREATION_DATE);
                args.putLong(MetadataFragment.EXTRA_DATA_METADATA_DOCUMENT_CREATION_DATE, documentCreationDate);
            }

            boolean isSent = extras.getBoolean(BaseMetadataFragment.EXTRA_DATA_IMAGE_IS_SENT, true);
            boolean useTempBitmap = extras.getBoolean(BaseMetadataFragment.EXTRA_DATA_USE_TEMP_BITMAP, false);
            String localPdfFilePath = extras.getString(BaseMetadataFragment.EXTRA_DATA_LOCAL_PDF, null);

            args.putParcelable(MetadataFragment.METADATA, onlineMetadata);
            args.putBoolean(MetadataFragment.IMAGE_IS_SENT, isSent);
            args.putBoolean(MetadataFragment.USE_TEMP_BITMAP, useTempBitmap);
            args.putString(MetadataFragment.LOCAL_PDF_FILE_PATH, localPdfFilePath);

            BaseMetadataFragment metadataFragment = createMetadataFragment();
            metadataFragment.setArguments(args);
            changeFragmentWithoutBackStack(metadataFragment);
        }
    }

    private BaseMetadataFragment createMetadataFragment() {
        switch (BlueConfig.getAppType()) {
            case UNKNOWN:
                return new MetadataFragment();
            case VISMA_ONLINE:
                return new VismaOnlineMetadataFragment();
            case EACCOUNTING:
                return new EAcountingMetadataFragment();
            case MAMUT:
                return new MamutMetadataFragment();
            case ACCOUNTVIEW:
                return new AccountViewMetadataFragment();
            case NETVISOR:
                return new NetvisorMetadaFragment();
            case EXPENSE_MANAGER:
                return new ExpenseMetadataFragment();
            case SEVERA:
                return new SeveraMetadataFragment();
            default:
                throw new UnsupportedOperationException("Not implemented.");
        }
    }

    public void changeFragmentWithBackStack(Fragment fragment) {
        String tag = fragment.getClass().getName();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    public void changeFragmentWithoutBackStack(Fragment fragment) {
        String tag = fragment.getClass().getName();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, tag)
                .commit();
    }

    @Override
    protected void onDestroy() {
        VismaUtils.setCachedImageUrl(MetadataActivity.this, null);
        super.onDestroy();
    }
}
