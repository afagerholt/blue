package com.visma.blue.metadata.vismaonline;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.visma.blue.R;
import com.visma.blue.databinding.BlueFragmentMetadataVismaOnlineBinding;
import com.visma.blue.metadata.BaseMetadataFragment;
import com.visma.blue.metadata.TypePickerDialog;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.qr.QrActivity;

public class VismaOnlineMetadataFragment extends BaseMetadataFragment {

    private static final int ACTIVITY_REQUEST_CODE_RETRIEVE_QR_CODE = 1;
    private static final int REQUEST_CODE_TYPE = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 10;

    private VismaOnlineMetadataModelView mVismaOnlineMetadaModelView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        BlueFragmentMetadataVismaOnlineBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.blue_fragment_metadata_visma_online, container, false);

        mVismaOnlineMetadaModelView = new VismaOnlineMetadataModelView(
                getActivity().getFragmentManager(),
                getFragmentManager(),
                mOnlineMetaData,
                getInputMethodManager(),
                this,
                getContext(),
                mHasBitmap);

        binding.setMetadataViewModel(mVismaOnlineMetadaModelView);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupImageLayout(view, R.id.fragment_metadata_layout_image, R.id.fragment_metadata_image_filename);
        setTitle(VismaUtils.getTypeTextId(mOnlineMetaData.type));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.blue_fragment_metadata_menu_qr_code) {
            startQr();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQr();
            } else {
                Snackbar.make(getView(), R.string.visma_blue_error_missing_permission, Snackbar.LENGTH_LONG)
                        .show(); // Don’t forget to show!
            }
        }
    }

    private void startQr() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.

            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);

            return;
        }

        Intent intent = new Intent(getActivity(), QrActivity.class);
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE_RETRIEVE_QR_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_REQUEST_CODE_RETRIEVE_QR_CODE
                && resultCode == FragmentActivity.RESULT_OK) {

            mVismaOnlineMetadaModelView.updateDataAfterQrScan(data.getExtras().getString(
                    QrActivity.ACTIVITY_RESULT_CODE_QR_MESSAGE));
        }

        if (requestCode == REQUEST_CODE_TYPE && resultCode == Activity.RESULT_OK) {
            mVismaOnlineMetadaModelView.updateTypeChange(data.getIntExtra(TypePickerDialog.EXTRA_TYPE, -1));
            setTitle(VismaUtils.getTypeTextId(mOnlineMetaData.type));
        }
    }

    @Override
    protected synchronized void showTypePickerDialog() {
        mVismaOnlineMetadaModelView.showTypePickerDialog();
    }
}
