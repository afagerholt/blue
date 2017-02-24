package com.visma.blue.metadata.mamut;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.visma.blue.R;
import com.visma.blue.databinding.BlueFragmentMetadataMamutBinding;
import com.visma.blue.metadata.BaseMetadataFragment;
import com.visma.blue.metadata.MetadataFragment;
import com.visma.blue.metadata.TypePickerDialog;
import com.visma.blue.misc.VismaUtils;

public class MamutMetadataFragment extends BaseMetadataFragment {

    private MamutMetaDataViewModel mMamutMetadataViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        BlueFragmentMetadataMamutBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.blue_fragment_metadata_mamut, container, false);
        mMamutMetadataViewModel = new MamutMetaDataViewModel(
                getActivity().getFragmentManager(),
                getFragmentManager(),
                mOnlineMetaData,
                getInputMethodManager(),
                this,
                getContext(),
                mHasBitmap);
        binding.setMetadataViewModel(mMamutMetadataViewModel);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MetadataFragment.REQUEST_CODE_TYPE && resultCode == Activity.RESULT_OK) {
            mMamutMetadataViewModel.updateTypeChange(data.getIntExtra(TypePickerDialog.EXTRA_TYPE, -1));
            setTitle(VismaUtils.getTypeTextId(mOnlineMetaData.type));
        }
    }
}