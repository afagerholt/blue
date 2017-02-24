package com.visma.blue.metadata;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.visma.blue.R;
import com.visma.blue.misc.ErrorMessage;
import com.visma.blue.misc.FileDownloader;
import com.visma.blue.misc.FileManager;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.Base;
import com.visma.blue.network.BlueNetworkError;
import com.visma.blue.network.OnlinePhotoType;
import com.visma.blue.network.OnlineResponseCodes;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.CreatePhotoAnswer;
import com.visma.blue.network.containers.GetDocumentAnswer;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.network.containers.OnlinePhoto;
import com.visma.blue.network.requests.CreatePhotoRequest;
import com.visma.blue.network.requests.DeletePhotoRequest;
import com.visma.blue.network.requests.GetDocumentRequest;
import com.visma.blue.network.requests.UpdatePhotoRequest;
import com.visma.blue.network.requests.customdata.GetCustomDataAnswer;
import com.visma.blue.network.requests.customdata.GetCustomDataRequest;
import com.visma.blue.provider.BlueContentProvider;
import com.visma.blue.provider.MetadataList;
import com.visma.blue.provider.TempBitmaps;
import com.visma.common.VismaAlertDialog;
import com.visma.common.VismaAlertDialog.AnimationEndingListener;
import com.visma.common.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class BaseMetadataFragment extends Fragment {

    public static final String METADATA = "METADATA";
    public static final String USE_TEMP_BITMAP = "USE_TEMP_BITMAP";
    public static final String LOCAL_PDF_FILE_PATH = "LOCAL_PDF_FILE_PATH";
    public static final String IMAGE_IS_SENT = "IMAGE_IS_SENT";
    public static final String EXTRA_DATA_METADATA = "EXTRA_DATA_METADATA";
    public static final String EXTRA_DATA_METADATA_DOCUMENT_CREATION_DATE =
            "EXTRA_DATA_METADATA_DOCUMENT_CREATION_DATE";
    public static final String EXTRA_DATA_USE_TEMP_BITMAP = "EXTRA_DATA_USE_TEMP_BITMAP";
    public static final String EXTRA_DATA_IMAGE_IS_SENT = "EXTRA_DATA_IMAGE_IS_SENT";
    public static final String EXTRA_DATA_LOCAL_PDF = "EXTRA_DATA_LOCAL_PDF";
    public static final String EXTRA_DATA_PHOTO_UPLOAD_FAIL_MESSAGE =
            "EXTRA_DATA_PHOTO_UPLOAD_FAIL_MESSAGE";

    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 100;

    // Data that is saved and restored
    protected OnlineMetaData mOnlineMetaData;
    private Boolean mIsSent = false;
    protected boolean mHasBitmap = false;

    // Local pdf parameters
    private String mLocalPdfPath;
    private boolean mUseLocalPdf = false;

    protected static final DecimalFormat mLocalDecimalFormatter = new DecimalFormat();
    private byte[] documentData = null;
    private File mDownloadedFile = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the formatter to be used for decimal formatting/parsing
        mLocalDecimalFormatter.setMinimumFractionDigits(2);
        mLocalDecimalFormatter.setMaximumFractionDigits(2);

        Bundle bundle;
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            bundle = savedInstanceState;
        } else {
            bundle = getArguments();
        }

        if (bundle != null) {
            this.mOnlineMetaData = bundle.getParcelable(METADATA);
            this.mIsSent = bundle.getBoolean(IMAGE_IS_SENT, true);
            this.mHasBitmap = bundle.getBoolean(USE_TEMP_BITMAP, false);
            this.mLocalPdfPath = bundle.getString(LOCAL_PDF_FILE_PATH, null);
            this.mUseLocalPdf = mLocalPdfPath != null;
        }

        // Needed for onOptionsItemSelected to be called later on
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Logger.logPageView(Logger.VIEW_METADATA);

        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(METADATA, mOnlineMetaData);
        outState.putBoolean(USE_TEMP_BITMAP, mHasBitmap);
        outState.putBoolean(IMAGE_IS_SENT, mIsSent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDownloadedFile != null && !mUseLocalPdf && mOnlineMetaData.localFileName == null) {
            mDownloadedFile.delete();
        }
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSION_REQUEST_EXTERNAL_STORAGE) {
            // Request for camera permission.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendMetadata();
            } else {
                Snackbar
                        .make(getView(), R.string.visma_blue_error_missing_permission, Snackbar
                                .LENGTH_LONG)
                        .show(); // Don’t forget to show!
            }
        }
    }

    protected void setupImageLayout(View rootView, @IdRes int imageLayoutView, @IdRes int
            fileNameView) {
        View layoutImage = rootView.findViewById(imageLayoutView);
        layoutImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager inputManager =
                        (InputMethodManager) getActivity().getSystemService(Context
                                .INPUT_METHOD_SERVICE);
                boolean keyboardWasHidden =
                        inputManager.hideSoftInputFromWindow(v.getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS,
                                new ResultReceiver(new Handler()) {
                                    @Override
                                    protected void onReceiveResult(int resultCode, Bundle
                                            resultData) {
                                        // This seems to be called only if the keyboard was hidden
                                        super.onReceiveResult(resultCode, resultData);

                                        showPhoto();
                                    }
                                });

                // If the keyboard was already hidden.
                if (!keyboardWasHidden) {
                    showPhoto();
                }
            }
        });

        if (!TextUtils.isEmpty(mOnlineMetaData.originalFilename)) {
            TextView fileNameTextView = (TextView) rootView.findViewById(fileNameView);
            fileNameTextView.setText(mOnlineMetaData.originalFilename);
            fileNameTextView.setVisibility(View.VISIBLE);
            fileNameTextView.setSelected(true); // Makes the Textview scroll if
            // android:ellipsize="marquee"
        }
    }

    private void showPhoto() {
        if (mHasBitmap) {
            showPhotoActivity(null);
        } else if (mOnlineMetaData.contentType.equals("image/jpeg")
                || mOnlineMetaData.contentType.equals("image/png")
                || mOnlineMetaData.contentType.equals("image/gif")
                || mOnlineMetaData.contentType.equals("image/bmp")) {
            showPhotoActivity(mOnlineMetaData.photoId);
        } else {
            // Is the file already downloaded?
            if (mLocalPdfPath != null) {
                mDownloadedFile = new File(mLocalPdfPath);
            } else if (mOnlineMetaData.localFileName != null) {
                try {
                    mDownloadedFile = FileManager.exportFile(FileManager.getLocallySavedFile(
                            getContext(), mOnlineMetaData.localFileName),
                            FileManager.getDownloadFolderPath(getContext()),
                            mOnlineMetaData.localFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (mDownloadedFile != null && mDownloadedFile.exists()) {
                viewInExternalActivity(mDownloadedFile);
                return;
            }

            // Can we display the file if we download it?
            Intent target = new Intent(Intent.ACTION_VIEW);
            setIntentType(target, mOnlineMetaData.contentType);
            if (target.resolveActivity(getActivity().getPackageManager()) == null) {
                Toast.makeText(getActivity(), R.string.visma_blue_no_supporting_app_installed,
                        Toast.LENGTH_LONG)
                        .show();
                return;
            }

            // Download the document
            final String finalToken = VismaUtils.getToken();
            final String finalPhotoId = mOnlineMetaData.photoId;
            final VismaAlertDialog alert = new VismaAlertDialog(getActivity());
            alert.showProgressBar();

            GetDocumentRequest<GetDocumentAnswer> request = new
                    GetDocumentRequest<GetDocumentAnswer>(getActivity(),
                    finalToken,
                    finalPhotoId,
                    GetDocumentAnswer.class,
                    new Response.Listener<GetDocumentAnswer>() {
                        @Override
                        public void onResponse(final GetDocumentAnswer response) {
                            URL url = null;
                            try {
                                url = new URL(response.photoUrl);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }

                            new AsyncTask<URL, Void, Boolean>() {
                                @Override
                                protected Boolean doInBackground(URL... url) {
                                    File path = FileManager.getDownloadFolderPath(getContext());
                                    File file = new File(path, mOnlineMetaData.originalFilename
                                            == null
                                            ? "Test.pdf" : mOnlineMetaData.originalFilename);
                                    if (FileDownloader.downloadFile(url[0], file)) {
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }

                                protected void onPostExecute(Boolean success) {
                                    alert.closeDialog();
                                    if (success) {
                                        File path = FileManager.getDownloadFolderPath(getContext());
                                        mDownloadedFile = new File(path, mOnlineMetaData
                                                .originalFilename == null
                                                ? "Test.pdf" : mOnlineMetaData.originalFilename);
                                        // If the file ended up in the internal storage, then we
                                        // need to allow the
                                        // external app to read it.
                                        mDownloadedFile.setReadable(true, false);
                                        viewInExternalActivity(mDownloadedFile);
                                    } else {
                                        Toast.makeText(getActivity(), R.string
                                                        .visma_blue_error_unknown_error,
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            }.execute(url);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            final Activity activity = getActivity();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }

                            int errorMessageId;
                            if (error instanceof BlueNetworkError) {
                                BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                                errorMessageId = ErrorMessage.getErrorMessage(blueNetworkError
                                        .blueError, false);
                            } else {
                                errorMessageId = ErrorMessage.getErrorMessage(OnlineResponseCodes
                                        .NotSet, false);
                            }

                            alert.showError(errorMessageId);
                        }
                    });

            // Add the request to the RequestQueue.
            VolleySingleton.getInstance().addToRequestQueue(request);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setIntentType(Intent target, String type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            target.setType(Intent.normalizeMimeType(type));
        } else {
            // Let's hope there is no strange data appended to the mime type.
            target.setType(type.toLowerCase(Locale.US));
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setIntentDataAndType(Intent target, Uri data, String type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            target.setDataAndType(data, Intent.normalizeMimeType(type));
        } else {
            // Let's hope there is no strange data appended to the mime type.
            target.setDataAndType(data, type.toLowerCase(Locale.US));
        }
    }

    private void viewInExternalActivity(File file) {
        Intent target = new Intent(Intent.ACTION_VIEW);
        setIntentDataAndType(target, Uri.fromFile(file), mOnlineMetaData.contentType);
        if (target.resolveActivity(getActivity().getPackageManager()) != null) {
            //Intent intent = Intent.createChooser(target, null); // Force a chooser
            Logger.logPageView(Logger.VIEW_DOCUMENT);
            startActivity(target);
        } else {
            Toast.makeText(getActivity(), R.string.visma_blue_no_supporting_app_installed, Toast
                    .LENGTH_LONG).show();
        }
    }

    private static double parseDouble(String doubleString) throws ParseException {
        //The double string may not be valid in any locale as it enters this method. The reason is
        // that we may be stripping away characters from the end of it.
        // E.g., "2 000" is valid in Sweden but "2 00" is not.

        // The string may end with a "," or a "." so that must be stripped away
        if (doubleString.endsWith(".") || doubleString.endsWith(",")) {
            doubleString = doubleString.substring(0, doubleString.length() - 1);
        }

        final char thousandSeparator = new DecimalFormatSymbols().getGroupingSeparator();
        doubleString = doubleString.replace(String.valueOf(thousandSeparator), "");

        NumberFormat nf = NumberFormat.getInstance();
        double numberFormatParsedValue = nf.parse(doubleString).doubleValue();

        return numberFormatParsedValue;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.blue_fragment_metadata, menu);
        if (!mOnlineMetaData.canDelete) {
            menu.findItem(R.id.blue_fragment_metadata_menu_send).setVisible(false);
            menu.findItem(R.id.blue_fragment_metadata_menu_discard).setVisible(false);
            menu.findItem(R.id.blue_fragment_metadata_menu_qr_code).setVisible(false);
        } else if (mHasBitmap && mOnlineMetaData.databaseId == 0) { // If locally stored.
            menu.findItem(R.id.blue_fragment_metadata_menu_discard).setVisible(false);
        } else if (mUseLocalPdf && mOnlineMetaData.databaseId == 0) { // If pdf file sharing.
            menu.findItem(R.id.blue_fragment_metadata_menu_discard).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        } else if (item.getItemId() == R.id.blue_fragment_metadata_menu_send) {
            item.setEnabled(false);
            if (mOnlineMetaData.type == OnlinePhotoType.UNKNOWN.getValue()) {
                showTypePickerDialog();
                item.setEnabled(true);
            } else {
                if (!verifyMetaData()) {
                    item.setEnabled(true);
                    return false;
                }

                sendMetadata();
            }
            return true;
        } else if (item.getItemId() == R.id.blue_fragment_metadata_menu_discard) {
            discardMetadata();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected synchronized void showTypePickerDialog() {

    }

    protected boolean verifyMetaData() {
        return true;
    }

    private void sendMetadata() {
        final VismaAlertDialog alert = new VismaAlertDialog(getActivity());
        alert.showProgressBar();

        InputMethodManager inputManager =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager
                .HIDE_NOT_ALWAYS);

        VismaUtils.setLastSelectedType(getContext(), mOnlineMetaData.type);
        VismaUtils.setExpenseDefaultCurrency(getContext(), mOnlineMetaData.expenseCustomData);
        getDocumentDataAsBytesArray();

        if (!mIsSent) {
            final String finalToken = VismaUtils.getToken();
            final OnlinePhoto onlinePhoto = new OnlinePhoto(mOnlineMetaData, documentData);

            CreatePhotoRequest<CreatePhotoAnswer> request = new
                    CreatePhotoRequest<CreatePhotoAnswer>(getActivity(),
                    finalToken,
                    onlinePhoto,
                    CreatePhotoAnswer.class,
                    new Response.Listener<CreatePhotoAnswer>() {
                        @Override
                        public void onResponse(final CreatePhotoAnswer response) {
                            final Activity activity = getActivity();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }

                            removeLocallySavedData();

                            alert.closeDialog(new AnimationEndingListener() {
                                public void onAnimationEnding() {
                                    getActivity().runOnUiThread(new Runnable() {

                                        public void run() {
                                            finishActivityWithOk();
                                        }
                                    });
                                }
                            });
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            final String user = VismaUtils.getCurrentEmail(getActivity());

                            int errorMessageId;
                            if (error instanceof BlueNetworkError) {
                                BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                                errorMessageId = ErrorMessage.getErrorMessage(blueNetworkError
                                        .blueError, false);
                                mOnlineMetaData.isNotSyncedDueToError = true;
                            } else {
                                errorMessageId = R.string.visma_blue_upload_photo_fail_message;
                                mOnlineMetaData.isNotSyncedDueToError = false;
                            }

                            // Check if this photo was already saved in the phone. If so, delete it
                            // before saving the updated version
                            removeLocallySavedData();

                            if (mOnlineMetaData.contentType != null && mOnlineMetaData.contentType
                                    .contains("application/")) {
                                //TODO remove this when photo name returned from backend
                                mOnlineMetaData.localFileName = FileManager.generateLocalFileName(
                                        mOnlineMetaData.originalFilename == null ? "Test.pdf"
                                                : mOnlineMetaData.originalFilename);
                            }

                            getContext().getContentResolver().insert(BlueContentProvider
                                            .CONTENT_URI_METADATA_LIST,
                                    MetadataList.getMetadataValues(mOnlineMetaData,
                                            documentData, user));

                            FileManager.saveDocumentInLocalStorage(getContext(), documentData,
                                    mOnlineMetaData.localFileName);

                            finishActivityAfterFailedPhotoUpload(errorMessageId);
                        }
                    });

            // Add the request to the RequestQueue.
            VolleySingleton.getInstance().addToRequestQueue(request);
            Logger.logAction(Logger.ACTION_CREATE,
                    Pair.create("type", Logger.getLoggerTypeName(mOnlineMetaData.type)));
        } else { // The photo has already been sent once, so we only
            // update it this time.
            final String finalToken = VismaUtils.getToken();
            UpdatePhotoRequest<Base> request = new UpdatePhotoRequest<Base>(getActivity(),
                    finalToken,
                    mOnlineMetaData,
                    Base.class,
                    new Response.Listener<Base>() {
                        @Override
                        public void onResponse(final Base response) {
                            final Activity activity = getActivity();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }

                            alert.closeDialog(new AnimationEndingListener() {
                                public void onAnimationEnding() {
                                    getActivity().runOnUiThread(new Runnable() {

                                        public void run() {
                                            finishActivityWithOk();
                                        }
                                    });
                                }
                            });
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            final Activity activity = getActivity();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }

                            int errorMessageId;
                            if (error instanceof BlueNetworkError) {
                                BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                                errorMessageId = ErrorMessage.getErrorMessage(blueNetworkError
                                        .blueError, false);
                            } else {
                                errorMessageId = ErrorMessage.getErrorMessage(OnlineResponseCodes
                                        .NotSet, false);
                            }

                            // The photo has already been sent once, so there is nothing in the
                            // database to update.
                            // That also means that the changes the user just did will be discarded.

                            alert.setOnCloseListener(new VismaAlertDialog.OnCloseListener() {
                                @Override
                                public void onClose() {
                                    // Recreates option menu so that send menu item would be
                                    // enabled again.
                                    getActivity().invalidateOptionsMenu();
                                }
                            });

                            alert.showError(errorMessageId);
                        }
                    });

            // Add the request to the RequestQueue.
            VolleySingleton.getInstance().addToRequestQueue(request);
            Logger.logAction(Logger.ACTION_UPDATE,
                    Pair.create("type", Logger.getLoggerTypeName(mOnlineMetaData.type)));
        }

    }

    private void discardMetadata() {
        InputMethodManager inputManager =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager
                .HIDE_NOT_ALWAYS);

        if (mOnlineMetaData.databaseId != 0) {
            final VismaAlertDialog alert = new VismaAlertDialog(getActivity());
            alert.showProgressBar();
            removeLocallySavedData();
            alert.closeDialog();
            finishActivityWithOk();
            return;
        }

        final String finalToken = VismaUtils.getToken();
        final VismaAlertDialog alert = new VismaAlertDialog(getActivity());
        alert.showProgressBar();

        DeletePhotoRequest<Base> request = new DeletePhotoRequest<Base>(getActivity(),
                finalToken,
                mOnlineMetaData.photoId,
                Base.class,
                new Response.Listener<Base>() {
                    @Override
                    public void onResponse(final Base response) {
                        final Activity activity = getActivity();
                        if (activity == null || activity.isFinishing()) {
                            return;
                        }

                        removeLocallySavedData();

                        alert.closeDialog(new AnimationEndingListener() {
                            public void onAnimationEnding() {
                                getActivity().runOnUiThread(new Runnable() {

                                    public void run() {
                                        finishActivityWithOk();
                                    }
                                });
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        final Activity activity = getActivity();
                        if (activity == null || activity.isFinishing()) {
                            return;
                        }

                        int errorMessageId;
                        if (error instanceof BlueNetworkError) {
                            BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                            errorMessageId = ErrorMessage.getErrorMessage(blueNetworkError
                                    .blueError, false);
                        } else {
                            errorMessageId = ErrorMessage.getErrorMessage(OnlineResponseCodes
                                    .NotSet, false);
                        }

                        // The photo has already been sent once, so there is nothing in the
                        // database to update.
                        // That also means that the changes the user just did will be discarded.
                        alert.showError(errorMessageId);
                    }
                });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);
        Logger.logAction(Logger.ACTION_DELETE);
    }

    private void finishActivityWithOk() {
        getActivity().setResult(FragmentActivity.RESULT_OK);
        getActivity().finish();
    }

    private void finishActivityAfterFailedPhotoUpload(int errorMessage) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATA_PHOTO_UPLOAD_FAIL_MESSAGE, errorMessage);
        getActivity().setResult(FragmentActivity.RESULT_OK, intent);
        getActivity().finish();
    }

    private void showPhotoActivity(String photoId) {
        Intent intent = new Intent(getActivity(), PhotoActivity.class);
        if (photoId != null) {
            intent.putExtra(PhotoActivity.EXTRA_PHOTO_ID, photoId);
        }
        startActivity(intent);
    }

    protected void downloadCustomData(Context context,
                                      Response.Listener<GetCustomDataAnswer> responseListener) {
        downloadCustomData(context, responseListener, mCustomDataDownloadErrorListener);
    }

    protected void downloadCustomData(Context context,
                                      Response.Listener<GetCustomDataAnswer> responseListener,
                                      Response.ErrorListener errorListener) {
        if (!Util.isConnectedOrConnecting(context)) {
            return;
        }

        final String token = VismaUtils.getToken();
        if (TextUtils.isEmpty(token)) {
            return;
        }

        // Formulate the request and handle the response.
        GetCustomDataRequest request = new GetCustomDataRequest<>(context,
                token,
                GetCustomDataAnswer.class,
                responseListener,
                errorListener
        );

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);
    }

    private Response.ErrorListener mCustomDataDownloadErrorListener =
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    final Activity activity = getActivity();
                    if (activity == null || activity.isFinishing()) {
                        return;
                    }

                    int errorMessageId;
                    if (error instanceof BlueNetworkError) {
                        BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                        errorMessageId = ErrorMessage.getErrorMessage(blueNetworkError.blueError,
                                false);
                    } else {
                        errorMessageId = R.string.visma_blue_error_failed_to_download_custom_data;
                    }

                    VismaAlertDialog alert = new VismaAlertDialog(activity);
                    alert.showError(errorMessageId);
                }
            };

    protected void setTitle(int title) {
        getActivity().setTitle(title);
    }


    protected InputMethodManager getInputMethodManager() {
        return (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void removeLocallySavedData() {
        if (mOnlineMetaData.databaseId != 0) {
            String selection = MetadataList._ID + " =? ";
            String[] selectionArgs = new String[]{Long.toString(mOnlineMetaData
                    .databaseId)};
            getContext().getContentResolver().delete(BlueContentProvider
                    .CONTENT_URI_METADATA_LIST, selection, selectionArgs);
            FileManager.removeDocumentFromLocalStorage(getContext(),
                    mOnlineMetaData.localFileName);
        }
    }

    private void getDocumentDataAsBytesArray() {
        if (mHasBitmap) {
            this.documentData = TempBitmaps.getBitmapData(getContext().getContentResolver()
                    .query(BlueContentProvider.CONTENT_URI_METADATA_TEMP_BITMAP, TempBitmaps
                            .getAllColumnNames(), null, null, null));
        } else if (mUseLocalPdf || mOnlineMetaData.localFileName != null) {
            if (mUseLocalPdf) {
                if (mDownloadedFile == null) {
                    mDownloadedFile = new File(mLocalPdfPath);
                }
            } else  {
                try {
                    mDownloadedFile = FileManager.exportFile(FileManager.getLocallySavedFile(
                            getContext(), mOnlineMetaData.localFileName),
                            FileManager.getDownloadFolderPath(getContext()),
                            mOnlineMetaData.localFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    this.documentData = new byte[0];
                    return;
                }
            }

            this.documentData = new byte[(int) mDownloadedFile.length()];
            try {
                new FileInputStream(mDownloadedFile).read(documentData);
            } catch (Exception e) {
                e.printStackTrace();
                this.documentData = new byte[0];
            }
        }
    }

    protected void registerEventListener() {
        EventBus.getDefault().register(this);
    }

    protected void unregisterEventListener() {
        EventBus.getDefault().unregister(this);
    }
}
