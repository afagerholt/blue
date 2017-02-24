package com.visma.blue.metadata.netvisor;

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

import com.visma.blue.R;
import com.visma.blue.databinding.BlueFragmentMetadataNetvisorBinding;
import com.visma.blue.metadata.BaseMetadataFragment;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.requests.customdata.Netvisor;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.NetvisorPayloads;

import java.util.ArrayList;

public class NetvisorMetadaFragment extends BaseMetadataFragment implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final int LOADER_ID_NETVISOR_PAYLOADS = 1;

    private NetvisorMetadataViewModel mNetvisorMetadaViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID_NETVISOR_PAYLOADS, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        BlueFragmentMetadataNetvisorBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.blue_fragment_metadata_netvisor,
                        container, false);

        mNetvisorMetadaViewModel = new NetvisorMetadataViewModel(
                getActivity().getFragmentManager(),
                mOnlineMetaData,
                getContext(),
                mHasBitmap,
                getInputMethodManager());

        binding.setMetadataViewModel(mNetvisorMetadaViewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupImageLayout(view, R.id.fragment_metadata_layout_image, R.id
                .fragment_metadata_image_filename);
        mNetvisorMetadaViewModel.setupCustomDataDropDowns(view);
        setTitle(VismaUtils.getTypeTextId(mOnlineMetaData.type));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.blue_fragment_metadata_menu_qr_code).setVisible(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_NETVISOR_PAYLOADS) {
            return new CursorLoader(getActivity(), BlueContentProvider
                    .CONTENT_URI_NETVISOR_PAYLOADS,
                    new String[]{
                            NetvisorPayloads.PAYLOAD_ID,
                            NetvisorPayloads.TYPE,
                            NetvisorPayloads.TITLE,
                            NetvisorPayloads.VALUES},
                    null, null, null);
        } else {
            throw new UnsupportedOperationException("Trying to use a loader that is not "
                    + "implemented.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final int loaderId = loader.getId();

        if (loaderId == LOADER_ID_NETVISOR_PAYLOADS) {
            if (data != null) {
                ArrayList<Netvisor.DropDown> netvisorPayloads = new ArrayList<>(data.getCount());
                boolean rowsLeft = data.moveToFirst();
                while (rowsLeft) {
                    netvisorPayloads.add(new Netvisor.DropDown(data));
                    rowsLeft = data.moveToNext();
                }
                mNetvisorMetadaViewModel.updateCustomDataDropDowns(getView(), netvisorPayloads);
            } else {
                mNetvisorMetadaViewModel.updateCustomDataDropDowns(getView(), new
                        ArrayList<Netvisor.DropDown>());
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
