package com.visma.blue.archive;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.archive.adapter.AttachMetadataAdapter;
import com.visma.blue.archive.adapter.EAccountingMetadataAdapter;
import com.visma.blue.archive.adapter.MetadataAdapter;
import com.visma.blue.archive.adapter.SeveraMetadataAdapter;
import com.visma.blue.background.MetadataUploadJob;
import com.visma.blue.camera.CameraActivity;
import com.visma.blue.events.MetadataEvent;
import com.visma.blue.metadata.BaseMetadataFragment;
import com.visma.blue.metadata.MetadataActivity;
import com.visma.blue.misc.AppId;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.MetadataList;
import com.visma.blue.provider.TempBitmaps;
import com.visma.common.VismaAlertDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.util.Date;

public class ArchiveFragment extends Fragment implements SearchView.OnQueryTextListener,
        SearchView.OnCloseListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int ACTIVITY_REQUEST_CAMERA = 1;
    public static final int ACTIVITY_REQUEST_METADATA = 4;

    private static final int PERMISSION_REQUEST_CAMERA = 0;

    private static final int LOADER_ID_METADATA = 1;

    private MetadataAdapter mMetadataAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    // The SearchView for doing filtering.
    private SearchView mSearchView;
    private MenuItem mSearchViewMenuItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        loadMetaData();
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        final View view = inflater.inflate(R.layout.blue_fragment_archive, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id
                .blue_fragment_archive_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reLoadMetadata();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // OnCreateView is not called when this fragment becomes visible when returning from the
        // camera
        Logger.logPageView(Logger.VIEW_ARCHIVE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.blue_fragment_archive, menu);

        mSearchViewMenuItem = menu.findItem(R.id.blue_fragment_menu_search);
        mSearchView = (SearchView) mSearchViewMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Logger.logAction(Logger.ACTION_SEARCH);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.blue_fragment_menu_add) {
            startCameraActivity();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraActivity();
            } else {
                showSnackBar(R.string.visma_blue_error_missing_permission);
            }
        }
    }

    private void startCameraActivity() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest
                .permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            return;
        }

        int type = VismaUtils.getLastSelectedTypeOrDefault(getContext());

        Intent intent = new Intent(getActivity(), CameraActivity.class);
        intent.putExtra(CameraActivity.EXTRA_DATA_PHOTO_TYPE, type);
        startActivityForResult(intent, ACTIVITY_REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_REQUEST_METADATA && resultCode == Activity.RESULT_OK) {
            reLoadMetadata();
            checkForUploadError(data);
        } else if (requestCode == ACTIVITY_REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(getActivity(), MetadataActivity.class);

            OnlineMetaData metaData = data.getExtras().getParcelable(CameraActivity
                    .ACTIVITY_RESULT_CODE_METADATA);
            if (data.getExtras().containsKey(CameraActivity
                    .ACTIVITY_RESULT_CODE_METADATA_DOCUMENT_CREATION_DATE)) {
                Date documentCreationDate = new Date(
                        data.getExtras().getLong(CameraActivity
                                .ACTIVITY_RESULT_CODE_METADATA_DOCUMENT_CREATION_DATE));
                intent.putExtra(BaseMetadataFragment.EXTRA_DATA_METADATA_DOCUMENT_CREATION_DATE,
                        documentCreationDate.getTime());
            }

            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_METADATA, metaData);
            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_IMAGE_IS_SENT, false);
            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_USE_TEMP_BITMAP, true);
            startActivityForResult(intent, ACTIVITY_REQUEST_METADATA);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void copyImageBitmapToTempBitmap(OnlineMetaData metaData) {
        String selection = MetadataList._ID + " =? ";
        String[] selectionArgs = new String[]{Long.toString(metaData.databaseId)};
        Cursor cursor = getContext().getContentResolver().query(BlueContentProvider
                        .CONTENT_URI_METADATA_NOT_SYNC_IMAGE,
                new String[]{MetadataList.METADATA_IMAGE}, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MetadataList.METADATA_IMAGE);
                byte[] blobData = cursor.getBlob(columnIndex);
                getContext().getContentResolver().update(BlueContentProvider
                        .CONTENT_URI_METADATA_TEMP_BITMAP, TempBitmaps
                        .getTempBitmapValues(blobData), null, null);
                cursor.close();
            }
        }
    }

    private void startMetadataActivity(OnlineMetaData metaData) {
        Intent intent = new Intent(getActivity(), MetadataActivity.class);

        if (metaData.databaseId != 0 && !metaData.contentType.contains("application/")) {
            // The image is saved in the database together with the metadata
            copyImageBitmapToTempBitmap(metaData);

            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_METADATA, metaData);
            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_IMAGE_IS_SENT, false);
            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_USE_TEMP_BITMAP, true);
        } else {
            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_METADATA, metaData);
            if (metaData.localFileName != null) {
                metaData.isVerified = true;
                intent.putExtra(BaseMetadataFragment.EXTRA_DATA_IMAGE_IS_SENT, true);
            } else if (metaData.databaseId == 0) {
                intent.putExtra(BaseMetadataFragment.EXTRA_DATA_IMAGE_IS_SENT, true);
            } else {
                intent.putExtra(BaseMetadataFragment.EXTRA_DATA_IMAGE_IS_SENT, false);
            }
            intent.putExtra(BaseMetadataFragment.EXTRA_DATA_USE_TEMP_BITMAP, false);
        }

        startActivityForResult(intent, ACTIVITY_REQUEST_METADATA);
    }

    @Override
    public boolean onClose() {
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.
        mMetadataAdapter.getFilter().filter(newText);
        return true;
    }

    public void loadMetaData() {
        getLoaderManager().initLoader(LOADER_ID_METADATA, null, this);
    }

    public void reLoadMetadata() {
        getContext().getContentResolver().notifyChange(BlueContentProvider
                .CONTENT_URI_METADATA_LIST, null);
    }

    private void updateMetadataList(Cursor data, View root) {
        final Activity activity = getActivity();
        if (activity == null || activity.isFinishing() || data == null) {
            return;
        }

        View empty = root.findViewById(android.R.id.empty);
        if (data.getCount() > 0) {
            empty.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.VISIBLE);
        }

        if (mMetadataAdapter == null || shouldRecreateMetadataAdapter()) {
            initializeMetaDataList(data, getView());
        } else {
            mMetadataAdapter.changeCursor(data);
        }
    }

    private void onMetadataUpdateError(int errorMessageId) {
        showSnackBar(errorMessageId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_METADATA) {
            return new CursorLoader(getActivity(), BlueContentProvider.CONTENT_URI_METADATA_LIST,
                    MetadataList.getDatabaseColumnNames(),
                    null, null, MetadataList.METADATA_NOT_SYNCED_DUE_TO_ERROR + " DESC, "
                    + MetadataList.METADATA_DATE + " DESC");
        } else {
            throw new UnsupportedOperationException("Trying to use a loader that is not "
                    + "implemented.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final int loaderId = loader.getId();
        if (loaderId == LOADER_ID_METADATA) {
            updateMetadataList(data, getView());
        } else {
            throw new UnsupportedOperationException("Trying to use a loader that is not "
                    + "implemented.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        final int loaderId = loader.getId();
        if (loaderId == LOADER_ID_METADATA) {
            if (mMetadataAdapter != null) {
                mMetadataAdapter.swapCursor(null);
            }
        } else {
            throw new UnsupportedOperationException("Trying to use a loader that is not "
                    + "implemented.");
        }
    }

    private class ArchiveItemClicklistener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            OnlineMetaData metaData = null;
            try {
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
                metaData = MetadataList.getMetaDataFromCursor(cursor, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            startMetadataActivity(metaData.clone());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMetadataUpdate(final MetadataEvent event) {
        if (event.getStatus() == MetadataEvent.UpdateStatus.STARTED_UPDATE.getValue()) {
            mSwipeRefreshLayout.setRefreshing(true);
        } else if (event.getStatus() == MetadataEvent.UpdateStatus.UPDATE_ERROR.getValue()) {
            mSwipeRefreshLayout.setRefreshing(false);
            onMetadataUpdateError(event.getErrorMessageId());
        } else if (event.getStatus() == MetadataEvent.UpdateStatus.FINISHED_UPDATE.getValue()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void onPhotoUploadFail(int errorMessage) {
        final Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (errorMessage == -1) {
            return;
        }

        final VismaAlertDialog alert = new VismaAlertDialog(activity);
        alert.setTitle(R.string.visma_blue_upload_photo_fail_title);
        alert.showError(errorMessage);
    }

    private void initializeMetaDataList(Cursor data, View root) {
        mMetadataAdapter = getMetadataAdapter(getActivity(), data);
        StickyListHeadersListView listView = (StickyListHeadersListView) root
                .findViewById(android.R.id.list);
        listView.setAdapter(mMetadataAdapter);
        listView.setOnItemClickListener(new ArchiveItemClicklistener());
    }

    public void collapseSearchView() {
        if (mSearchViewMenuItem != null) {
            mSearchViewMenuItem.collapseActionView();
        }
    }

    private boolean shouldRecreateMetadataAdapter() {
        final int integrationId = BlueConfig.getAppId();

        if (integrationId == AppId.EXPENSE_MANAGER.getValue() && !(mMetadataAdapter
                instanceof AttachMetadataAdapter)) {
            return true;
        } else if (integrationId == AppId.EACCOUNTING.getValue() && !(mMetadataAdapter
                instanceof EAccountingMetadataAdapter)) {
            return true;
        } else if (integrationId == AppId.EACCOUNTING.getValue() && !(mMetadataAdapter
                instanceof SeveraMetadataAdapter)) {
            return true;
        } else {
            if (!mMetadataAdapter.getClass().toString().equals(MetadataAdapter.class.toString())) {
                return true;
            }
        }

        return false;

    }

    private MetadataAdapter getMetadataAdapter(Context context, Cursor cursor) {
        switch (BlueConfig.getAppType()) {
            case UNKNOWN:
            case VISMA_ONLINE:
            case MAMUT:
            case ACCOUNTVIEW:
            case NETVISOR:
                return new MetadataAdapter(context, cursor);
            case EACCOUNTING:
                return new EAccountingMetadataAdapter(context, cursor);
            case SEVERA:
                return new SeveraMetadataAdapter(context, cursor);
            case EXPENSE_MANAGER:
                return new AttachMetadataAdapter(context, cursor);
            default:
                throw new UnsupportedOperationException("Not implemented.");
        }
    }

    public void checkForUploadError(Intent data) {
        if (data != null && data.hasExtra(BaseMetadataFragment
                .EXTRA_DATA_PHOTO_UPLOAD_FAIL_MESSAGE)) {
            onPhotoUploadFail(data.getIntExtra(BaseMetadataFragment
                    .EXTRA_DATA_PHOTO_UPLOAD_FAIL_MESSAGE, -1));
            data.removeExtra(BaseMetadataFragment.EXTRA_DATA_PHOTO_UPLOAD_FAIL_MESSAGE);
            MetadataUploadJob
                    .schedulePhotoUploadJob(MetadataUploadJob.SCHEDULED_PHOTO_UPLOAD_TIMEOUT);
        }
    }

    public void showSnackBar(int messageId) {
        final Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (messageId != -1) {
            Snackbar.make(getView(), messageId, Snackbar.LENGTH_LONG).show();
            // Don’t forget to show!
        }
    }
}