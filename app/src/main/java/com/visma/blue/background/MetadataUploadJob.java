package com.visma.blue.background;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.visma.blue.events.MetadataEvent;
import com.visma.blue.misc.FileManager;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.CreatePhotoAnswer;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.network.containers.OnlinePhoto;
import com.visma.blue.network.requests.CreatePhotoRequest;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.MetadataList;
import com.visma.common.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class MetadataUploadJob extends Job {

    public static final String TAG = "metadata_upload_tag";
    public static final long SCHEDULED_PHOTO_UPLOAD_TIMEOUT = 60 * 60 * 1000;
    public static final long INSTANT_PHOTO_UPLOAD_TIMEOUT = 1;

    private static final Object LOCK = new Object();
    private boolean hasUpdateFailed = false;
    private ArrayList<Long> syncedPhotoIdies = new ArrayList<>();

    @Override
    @NonNull
    protected synchronized Result onRunJob(Params params) {
        synchronized (LOCK) {
            final Context context = getContext();
            final String token = VismaUtils.getToken();
            final Boolean isDemo = VismaUtils.isDemoMode(context);
            final Boolean isSyncing = VismaUtils.isSyncMode(context);

            if (TextUtils.isEmpty(token) || isDemo || !isSyncing) {
                EventBus.getDefault().post(new MetadataEvent(MetadataEvent.UpdateStatus.UPDATE_ERROR
                        .getValue(), -1));
                return Result.FAILURE;
            }

            if (!Util.isConnectedOrConnecting(context)) {
                return Result.RESCHEDULE;
            }

            final ArrayList<OnlineMetaData> metaDataList = getNotSynchronizedMetaData();
            if (metaDataList.isEmpty()) {
                return Result.SUCCESS;
            }

            final CountDownLatch signal = new CountDownLatch(metaDataList.size());
            sendMetaData(metaDataList, 0, signal);
            try {
                signal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            deleteAlreadySyncedPhotos();

            if (hasUpdateFailed) {
                return Result.RESCHEDULE;
            } else {
                return Result.SUCCESS;
            }
        }

    }

    private ArrayList<OnlineMetaData> getNotSynchronizedMetaData() {
        Cursor data = getContext().getContentResolver().query(BlueContentProvider
                        .CONTENT_URI_METADATA_NOT_SYNC,
                MetadataList.getDatabaseColumnNames(), null, null, null);

        return MetadataList.getMetaData(data);
    }

    private void sendMetaData(final ArrayList<OnlineMetaData> metadataList, final int index,
                              final CountDownLatch signal) {
        final OnlineMetaData metaData = metadataList.get(index);
        final String finalToken = VismaUtils.getToken();
        final OnlinePhoto onlinePhoto = new OnlinePhoto(metaData, metaData.image);

        CreatePhotoRequest<CreatePhotoAnswer> request = new CreatePhotoRequest<>(getContext(),
                finalToken,
                onlinePhoto,
                CreatePhotoAnswer.class,
                new Response.Listener<CreatePhotoAnswer>() {
                    @Override
                    public void onResponse(final CreatePhotoAnswer response) {
                        if (metaData.databaseId != 0) {
                            syncedPhotoIdies.add(metaData.databaseId);
                            FileManager.removeDocumentFromLocalStorage(getContext(),metaData
                                    .localFileName);
                        }
                        signal.countDown();
                        if (signal.getCount() > 0) {
                            sendMetaData(metadataList, index + 1, signal);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        hasUpdateFailed = true;
                        signal.countDown();
                        if (signal.getCount() > 0) {
                            sendMetaData(metadataList, index + 1, signal);
                        }
                    }
                });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);
        Logger.logAction(Logger.ACTION_CREATE,
                Pair.create("type", Logger.getLoggerTypeName(metaData.type)));
    }

    public static void schedulePhotoUploadJob(long delay) {
        JobManager.instance().cancelAllForTag(TAG);
            new JobRequest.Builder(MetadataUploadJob.TAG)
                    .setExact(delay)
                    .build()
                    .schedule();


    }

    private void deleteAlreadySyncedPhotos() {
        if (syncedPhotoIdies.isEmpty()) {
            return;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        ContentProviderOperation operation;
        for (Long rowId : syncedPhotoIdies) {
            operation = ContentProviderOperation
                    .newDelete(BlueContentProvider.CONTENT_URI_METADATA_LIST)
                    .withSelection(MetadataList._ID + " = ?", new String[]{"" + rowId})
                    .build();

            operations.add(operation);
        }

        try {
            getContext().getContentResolver().applyBatch(BlueContentProvider.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException  e) {
            e.printStackTrace();
        }
    }
}
