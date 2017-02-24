package com.visma.blue.metadata.severa;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.visma.blue.R;
import com.visma.blue.databinding.BlueFragmentMetadataSeveraBinding;
import com.visma.blue.metadata.BaseMetadataFragment;
import com.visma.blue.network.containers.SeveraCustomData;
import com.visma.blue.network.requests.customdata.Severa;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.SeveraCases;
import com.visma.blue.provider.SeveraProducts;
import com.visma.blue.provider.SeveraTaxes;
import com.visma.common.VismaAlertDialog;

import java.util.ArrayList;

public class SeveraMetadataFragment extends BaseMetadataFragment implements
        SeveraMetadataFragmentModelView.SeveraActionListener, LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final int LOADER_ID_SEVERA_CASES = 1;
    private static final int LOADER_ID_SEVERA_PRODUCTS = 2;
    private static final int LOADER_ID_SEVERA_TAXES = 3;

    public static final int SEVERA_CASE_ACTIVITY_COMMUNICATION = 1;
    public static final String SEVERA_DATA = "SEVERA_DATA";
    public static final String SAVED_DATA_FROM_SERVER = "SAVED_DATA_FROM_SERVER";
    public static final String EXTRA_CUSTOM_DATA = "EXTRA_CUSTOM_DATA";

    private SeveraMetadataFragmentModelView mSeveraMetadataModelView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID_SEVERA_CASES, null, this);
        getLoaderManager().initLoader(LOADER_ID_SEVERA_PRODUCTS, null, this);
        getLoaderManager().initLoader(LOADER_ID_SEVERA_TAXES, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        BlueFragmentMetadataSeveraBinding binding =
                DataBindingUtil.inflate(inflater, R.layout
                        .blue_fragment_metadata_severa, container, false);

        mSeveraMetadataModelView = new SeveraMetadataFragmentModelView(mOnlineMetaData,
                getActivity().getFragmentManager(),
                getInputMethodManager(),
                getContext(),
                getCustomSeveraData(savedInstanceState),
                this);

        binding.setMetadataViewModel(mSeveraMetadataModelView);

        return binding.getRoot();
    }

    private Severa getCustomSeveraData(Bundle savedInstanceState) {
        Bundle bundle;
        Severa customSeveraData = null;
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            bundle = savedInstanceState;
        } else {
            bundle = getArguments();
        }

        if (bundle != null) {
            customSeveraData = bundle.getParcelable(EXTRA_CUSTOM_DATA);
        }

        return customSeveraData;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupImageLayout(view,
                R.id.fragment_metadata_layout_image, R.id.fragment_metadata_image_filename);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_CUSTOM_DATA, mSeveraMetadataModelView.getCustomSeveraData());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.blue_fragment_metadata_menu_qr_code).setVisible(false);
    }

    @Override
    protected boolean verifyMetaData() {
        if (mOnlineMetaData.severaCustomData != null) {
            if (mOnlineMetaData.severaCustomData.product != null) {

                if (mOnlineMetaData.severaCustomData.endDateUtc != null) {
                    long startDate = mOnlineMetaData.severaCustomData.startDateUtc.getTime();
                    long endDate = mOnlineMetaData.severaCustomData.endDateUtc.getTime();

                    if (startDate > endDate) {
                        showInvalidDataMessage(R.string.visma_blue_metadata_incorrect_data_message_date);
                        return false;
                    }
                }

                if (mOnlineMetaData.severaCustomData.endDateUtc == null
                        && mOnlineMetaData.severaCustomData.product.useStartAndEndTime) {
                    mOnlineMetaData.severaCustomData.endDateUtc = mOnlineMetaData.severaCustomData
                            .startDateUtc;
                }
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        Bundle receivedBundle = data.getExtras();

        if (resultCode == SEVERA_CASE_ACTIVITY_COMMUNICATION && receivedBundle != null) {
            SeveraCustomData.Case receivedSeveraCase = receivedBundle
                    .getParcelable(SeveraCaseActivity.SAVED_DATA);
            if (receivedSeveraCase != null && receivedSeveraCase.guid != null
                    && !receivedSeveraCase.guid.isEmpty()) {
                mOnlineMetaData.severaCustomData.severaCase = receivedSeveraCase;
            } else {
                mOnlineMetaData.severaCustomData.severaCase = null;
            }
            mSeveraMetadataModelView.updateCasePhaseSelection();
        }
    }

    private void showInvalidDataMessage(int message) {
        final Activity activity = getActivity();

        if (activity == null || activity.isFinishing()) {
            return;
        }

        VismaAlertDialog alert = new VismaAlertDialog(activity);

        alert.showError(message);
    }

    private void startCaseActivity() {
        Severa customSeveraData = mSeveraMetadataModelView.getCustomSeveraData();
        if (customSeveraData == null || customSeveraData.cases == null) {
            return;
        }

        Intent intent = new Intent(getActivity(), SeveraCaseActivity.class);
        Bundle bundle = new Bundle();
        SeveraCustomData.Case savedCase = mOnlineMetaData.severaCustomData.severaCase;

        bundle.putParcelableArrayList(SEVERA_DATA, customSeveraData.cases);
        bundle.putParcelable(SAVED_DATA_FROM_SERVER, savedCase);

        intent.putExtras(bundle);

        startActivityForResult(intent, SEVERA_CASE_ACTIVITY_COMMUNICATION);
    }

    @Override
    public void onCasePhaseClick() {
        startCaseActivity();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_SEVERA_CASES) {
            return new CursorLoader(getActivity(), BlueContentProvider.CONTENT_URI_SEVERA_CASES,
                    new String[]{
                            SeveraCases.GUID,
                            SeveraCases.CASE_NAME,
                            SeveraCases.TASK},
                    null, null, null);
        } else if (id == LOADER_ID_SEVERA_PRODUCTS) {
            return new CursorLoader(getActivity(),
                    BlueContentProvider.CONTENT_URI_SEVERA_PRODUCTS, new String[]{
                    SeveraProducts.GUID,
                    SeveraProducts.PRODUCT_NAME,
                    SeveraProducts.USE_START_AND_END_TIME,
                    SeveraProducts.CURRENCY_CODE,
                    SeveraProducts.PRICE,
                    SeveraProducts.VAT_PERCENTAGE},
                    null, null, null);
        } else if (id == LOADER_ID_SEVERA_TAXES) {
            return new CursorLoader(getActivity(),
                    BlueContentProvider.CONTENT_URI_SEVERA_TAXES, new String[]{
                    SeveraTaxes.GUID,
                    SeveraTaxes.IS_DEFAULT,
                    SeveraTaxes.PERCENTAGE},
                    null, null, null);
        } else {
            throw new UnsupportedOperationException("Trying to use a loader that is not "
                    + "implemented.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final int loaderId = loader.getId();

        if (loaderId == LOADER_ID_SEVERA_CASES) {
            if (data != null) {
                ArrayList<Severa.Case> severaCases = new ArrayList<>(data.getCount());
                Gson gson = new GsonBuilder().create();
                boolean rowsLeft = data.moveToFirst();
                while (rowsLeft) {
                    severaCases.add(new Severa.Case(data, gson));
                    rowsLeft = data.moveToNext();
                }
                mSeveraMetadataModelView.updateCasesPhases(severaCases);
            } else {
                mSeveraMetadataModelView.updateCasesPhases(new ArrayList<Severa.Case>());
            }

        } else if (loaderId == LOADER_ID_SEVERA_PRODUCTS) {
            if (data != null) {
                ArrayList<Severa.Product> severaProducts = new ArrayList<>(data.getCount());
                boolean rowsLeft = data.moveToFirst();
                while (rowsLeft) {
                    severaProducts.add(new Severa.Product(data));
                    rowsLeft = data.moveToNext();
                }
                mSeveraMetadataModelView.updateProducts(severaProducts);
            } else {
                mSeveraMetadataModelView.updateProducts(new ArrayList<Severa.Product>());
            }
        } else if (loaderId == LOADER_ID_SEVERA_TAXES) {
            if (data != null) {
                ArrayList<Severa.Tax> severaTaxes = new ArrayList<>(data.getCount());
                boolean rowsLeft = data.moveToFirst();
                while (rowsLeft) {
                    severaTaxes.add(new Severa.Tax(data));
                    rowsLeft = data.moveToNext();
                }
                mSeveraMetadataModelView.updateTaxes(severaTaxes);
            } else {
                mSeveraMetadataModelView.updateTaxes(new ArrayList<Severa.Tax>());
            }
        } else {
            throw new UnsupportedOperationException("Trying to use a loader that is not "
                    + "implemented.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
