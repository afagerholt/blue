package com.visma.blue.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.visma.blue.R;
import com.visma.blue.events.MetadataEvent;
import com.visma.blue.misc.ErrorMessage;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.BlueNetworkError;
import com.visma.blue.network.DateTypeSerializer;
import com.visma.blue.network.OnlineResponseCodes;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.GetMetadataListAnswer;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.network.requests.GetMetadataListRequest;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.MetadataList;
import com.visma.common.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;

public class MetadataDownloadService extends IntentService {

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public MetadataDownloadService() {
        super(CustomDataDownloadService.class.toString());
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns,
     * IntentService stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        EventBus.getDefault().post(new MetadataEvent(MetadataEvent.UpdateStatus.STARTED_UPDATE
                .getValue()));

        if (!Util.isConnectedOrConnecting(this)) {
            EventBus.getDefault().post(new MetadataEvent(MetadataEvent.UpdateStatus.UPDATE_ERROR
                    .getValue(), R.string.visma_blue_error_unknown_error));
            return;
        }

        final String token = VismaUtils.getToken();
        final Boolean isDemo = VismaUtils.isDemoMode(this);
        final Boolean isSyncing = VismaUtils.isSyncMode(this);

        if (TextUtils.isEmpty(token) || isDemo || !isSyncing) {
            EventBus.getDefault().post(new MetadataEvent(MetadataEvent.UpdateStatus.UPDATE_ERROR
                    .getValue(), -1));
            return;
        }

        makeMetaDataDownLoadRequest(token);
    }

    private void makeMetaDataDownLoadRequest(String token) {
        GetMetadataListRequest<GetMetadataListAnswer> request =
                new GetMetadataListRequest<>(this, token,
                        GetMetadataListAnswer
                                .class,
                        new Response.Listener<GetMetadataListAnswer>() {
                            @Override
                            public void onResponse(final GetMetadataListAnswer response) {
                                new UpdateMetadataTask().execute(response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int errorMessageId;
                        if (error instanceof BlueNetworkError) {
                            BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                            errorMessageId = ErrorMessage.getErrorMessage(blueNetworkError
                                    .blueError, false);
                        } else {
                            errorMessageId = ErrorMessage.getErrorMessage(OnlineResponseCodes
                                    .NotSet, false);
                        }
                        EventBus.getDefault().post(new MetadataEvent(MetadataEvent.UpdateStatus
                                .UPDATE_ERROR
                                .getValue(), errorMessageId));

                    }
                });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);
        Logger.logAction(Logger.ACTION_FETCH_METADATA_LIST);
    }

    private class UpdateMetadataTask extends AsyncTask<GetMetadataListAnswer, Void, Void> {

        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected Void doInBackground(GetMetadataListAnswer... getMetadataListAnswers) {
            final GetMetadataListAnswer getMetadataListAnswer = getMetadataListAnswers[0];

            if (getMetadataListAnswer.metaDataList != null) {
                ArrayList<OnlineMetaData> metadataList = getMetadataListAnswer.metaDataList;
                ArrayList<ContentValues> bulkValues = new ArrayList<>(metadataList.size());
                Gson gson = new GsonBuilder().create();
                for (OnlineMetaData onlineMetaData : metadataList) {
                    bulkValues.add(MetadataList.getMetadataValues(onlineMetaData, gson));
                }

                getContentResolver().bulkInsert(BlueContentProvider.CONTENT_URI_METADATA_LIST,
                        bulkValues.toArray(new ContentValues[bulkValues.size()]));
                getContentResolver().notifyChange(BlueContentProvider
                        .CONTENT_URI_METADATA_LIST, null);

            }

            EventBus.getDefault().post(new MetadataEvent(MetadataEvent.UpdateStatus.FINISHED_UPDATE
                    .getValue()));

            return null;
        }
    }
}
