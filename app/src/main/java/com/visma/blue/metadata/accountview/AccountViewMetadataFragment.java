package com.visma.blue.metadata.accountview;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.visma.blue.R;
import com.visma.blue.databinding.BlueFragmentMetadataAccountViewBinding;
import com.visma.blue.metadata.BaseMetadataFragment;
import com.visma.blue.misc.VismaUtils;

public class AccountViewMetadataFragment extends BaseMetadataFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        BlueFragmentMetadataAccountViewBinding binding =
                DataBindingUtil.inflate(inflater,
                        R.layout.blue_fragment_metadata_account_view,
                        container,
                        false);

        AccountViewMetaDataViewModel accountViewMetadataViewModel = new AccountViewMetaDataViewModel(
                getActivity().getFragmentManager(),
                mOnlineMetaData,
                getActivity(),
                mHasBitmap);

        binding.setMetadataViewModel(accountViewMetadataViewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupImageLayout(view, R.id.fragment_metadata_layout_image, R.id.fragment_metadata_image_filename);
        setTitle(VismaUtils.getTypeTextId(mOnlineMetaData.type));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.blue_fragment_metadata_menu_qr_code).setVisible(false);
    }
}