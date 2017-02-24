package com.visma.blue.misc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import com.visma.blue.BlueConfig;
import com.visma.blue.R;
import com.visma.blue.login.integrations.IntegrationData;
import com.visma.blue.metadata.expense.ExpenseCurrencyAdapter;
import com.visma.blue.network.OnlinePhotoType;
import com.visma.blue.network.containers.ExpenseCustomData;
import com.visma.common.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class VismaUtils {

    private static final String preferenceFile = "MobileScanner";
    private static final String tokenKey = "Token";
    private static final String demoKey = "Demo";
    private static final String syncMode = "SyncMode";
    private static final String currentEmailKey = "CurrentEmail";
    private static final String currentCompanyKey = "CurrentCompany";
    private static final String currentUserIdKey = "CurrentUserId";
    private static final String currentCompanyIdKey = "CurrentCompanyId";
    private static final String currentCompanyCountryCodeAlpha2 = "CurrentCompanyCountryCodeAlpha2";
    private static final String currentFlash = "CurrentFlash";
    private static final String inboundEmailAddress = "InboundEmailAddress";
    private static final String lastSelectedType = "lastSelectedType";
    private static final String lastCachedImageURl = "lastCachedImageURl";
    private static final String DEFAULT_CURRENCY = "defaultCurrency";
    private static final String USES_SUPPLIER_INVOICE_APPROVAL = "USES_SUPPLIER_INVOICE_APPROVAL";

    private static String mCountryCode;
    private static String mToken;
    private static String mCompanyGuid;
    private static String mUserGuid;

    public static boolean isRunningUnitTest = false;

    public static void init(@NonNull Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);

        mCountryCode = settings.getString(currentCompanyCountryCodeAlpha2, "");
        mToken = settings.getString(tokenKey, "");
        mCompanyGuid = settings.getString(currentCompanyIdKey, "");
        mUserGuid = settings.getString(currentUserIdKey, "");
    }

    public static Boolean isLoginNeeded(Context context) {
        final String token = VismaUtils.getToken();
        final boolean isDemoMode = VismaUtils.isDemoMode(context);
        final boolean isSyncMode = VismaUtils.isSyncMode(context);
        final String companyCountryCodeAlpha2 = getCurrentCompanyCountryCodeAlpha2();
        final String userId = getCurrentUserId();
        final String companyId = getCurrentCompanyId();

        AppId appId = BlueConfig.getAppType();


        if (!isSyncMode) {
            return true;
        } else if (isDemoMode) {
            return false;
        } else if (TextUtils.isEmpty(token)) {
            return true;
        } else if ((appId == AppId.UNKNOWN || appId == AppId.VISMA_ONLINE)
                && TextUtils.isEmpty(companyCountryCodeAlpha2)) {
            return true;
            // The old expense integration used the same guid for the company and the user.
        } else if ((appId == AppId.EXPENSE_MANAGER)
                && (Util.compare(companyId, userId) || TextUtils.isEmpty(companyId) || TextUtils.isEmpty(userId))) {
            return true;
        } else if (appId == AppId.UNKNOWN) { // Apps not running in demo mode must have a valid app id
            return true;
        } else {
            return false;
        }
    }

    public static void clearLoginData(Context context) {
        VismaUtils.setToken(context, "");
        VismaUtils.setCurrentUserId(context, "");
        VismaUtils.setCurrentCompanyId(context, "");
        VismaUtils.setCurrentCompany(context, "");
        VismaUtils.setDemoMode(context, false);
        VismaUtils.setSyncMode(context, false);

        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(lastSelectedType);
        editor.apply();

        settings = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settings.edit();
        editor.remove(context.getString(R.string.preference_inbound_email_address));
        editor.apply();
    }

    private static int getDefaultTypeForApp() {
        int type;
        switch (BlueConfig.getAppType()) {
            case UNKNOWN:
            case VISMA_ONLINE:
            case EACCOUNTING:
            case MAMUT:
                type = OnlinePhotoType.INVOICE.getValue();
                break;
            case EXPENSE_MANAGER:
            case NETVISOR:
            case SEVERA:
                type = OnlinePhotoType.RECEIPT.getValue();
                break;
            case ACCOUNTVIEW:
            default:
                type = OnlinePhotoType.DOCUMENT.getValue();
        }

        return type;
    }

    public static int getTypeTextId(int type) {
        int textId = R.string.visma_blue_document_type_unknown;

        if (type == OnlinePhotoType.INVOICE.getValue()) {
            textId = R.string.visma_blue_invoice;
        } else if (type == OnlinePhotoType.RECEIPT.getValue()) {
            textId = R.string.visma_blue_receipt;
        } else if (type == OnlinePhotoType.DOCUMENT.getValue()) {
            textId = R.string.visma_blue_document;
        } else if (type == OnlinePhotoType.UNKNOWN.getValue()) {
            textId = R.string.visma_blue_document_type_unknown;
        }

        return textId;
    }

    public static int getServiceName(AppId appId) {
        switch (appId) {
            case UNKNOWN:
            case VISMA_ONLINE:
            case EACCOUNTING:
                return R.string.visma_blue_service_title_spcs;
            case MAMUT:
                return R.string.visma_blue_service_title_mamut;
            case EXPENSE_MANAGER:
                return R.string.visma_blue_service_title_expense;
            case ACCOUNTVIEW:
                return R.string.visma_blue_service_title_accountview;
            case NETVISOR:
                return R.string.visma_blue_service_title_netvisor;
            case SEVERA:
                return R.string.visma_blue_service_title_severa;
            default:
                throw new UnsupportedOperationException("Not implemented.");
        }
    }

    public static AppId getAppId(int type) {
        AppId appId = AppId.UNKNOWN;

        for (AppId loopType : AppId.values()) {
            if (loopType.getValue() == type) {
                appId = loopType;
                break;
            }
        }

        return appId;
    }

    public static byte[] compress(final Bitmap bitmap) {
        final int sizeLimit = 250 * 1024; // 250 kB
        // Preallocate the array with some sensible data size
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) Math.round(sizeLimit * 1.5));

        if (bitmap != null) {
            int quality = 80;

            do {
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                Log.d("Blue compression",
                        "quality: " + quality
                                + " compressed size (kB): " + baos.size() / 1024
                                + " resolution: " + bitmap.getHeight() + "x" + bitmap.getWidth());
                quality -= 10;
                // Some phones seem to not honor the quality setting as far as I can see on crash reports
            }
            while (baos.size() > sizeLimit && quality >= 20);
        }

        return baos.toByteArray();
    }

    public static Bitmap getResizedBitmap(@NonNull Bitmap bm, int max, boolean rotate) throws
            OutOfMemoryError {
        // We don't want to make it larger. 2048x2048 is always supported by the Canvas
        // http://stackoverflow.com/questions/7428996/hw-accelerated-activity-how-to-get-opengl-texture-size-limit
        max = Math.max(max, 2048);
        int currentMax = bm.getWidth() > bm.getHeight() ? bm.getWidth() : bm.getHeight();

        float scale = ((float) max) / currentMax;

        Matrix matrix = new Matrix();
        if (scale < 1.0) {
            matrix.postScale(scale, scale);
        }
        if (rotate) {
            matrix.postRotate(90);
        }

        if (matrix.isIdentity()) {
            return bm;
        } else {
            Bitmap temp;
            try { // Had a problem that the exception was not thrown out of the function, so I try it like this for now.
                // It looks as if there are cases here where we actually can't catch the error.
                temp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false);
            } catch (Error e) {
                e.printStackTrace();
                throw e;
            }
            bm.recycle();
            bm = null;

            return temp;
        }
    }

    public static boolean shouldRotateBitmap(Bitmap bm) {
        if (bm.getWidth() > bm.getHeight()) {
            return true;
        }
        return false;
    }

    public static void setToken(Context context, String token) {
        if (context != null) {
            mToken = token;

            SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(tokenKey, token);
            editor.apply();
        }
    }

    public static String getToken() {
        return new String(mToken);
    }

    public static void setDemoMode(Context context, Boolean isDemoMode) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(demoKey, isDemoMode);
        editor.apply();
    }

    public static boolean isDemoMode(Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        return settings.getBoolean(demoKey, false);
    }

    public static void setSyncMode(Context context, Boolean isSyncing) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(syncMode, isSyncing);
        editor.apply();
    }

    public static Boolean isSyncMode(Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        return settings.getBoolean(syncMode, false);
    }

    public static void setCurrentEmail(Context context, String email) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(currentEmailKey, email);
        editor.apply();
    }

    public static String getCurrentEmail(Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        return settings.getString(currentEmailKey, "");
    }

    public static void setCurrentCompany(Context context, String company) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(currentCompanyKey, company);
        editor.apply();
    }

    public static String getCurrentCompany(Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        return settings.getString(currentCompanyKey, "");
    }

    public static void setCurrentUserId(Context context, String userId) {
        if (context != null) {
            mUserGuid = userId;

            SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(currentUserIdKey, userId);
            editor.apply();
        }
    }

    public static String getCurrentUserId() {
        return new String(mUserGuid);
    }

    public static void setCurrentCompanyId(Context context, String companyId) {
        if (context != null) {
            mCompanyGuid = companyId;

            SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(currentCompanyIdKey, companyId);
            editor.apply();
        }
    }

    public static String getCurrentCompanyId() {
        return new String(mCompanyGuid);
    }

    public static void setCurrentCompanyCountryCodeAlpha2(Context context, String companyCountryCodeAlpha2) {
        if (context != null) {
            mCountryCode = companyCountryCodeAlpha2;

            SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(currentCompanyCountryCodeAlpha2, companyCountryCodeAlpha2);
            editor.apply();
        }
    }

    public static String getCurrentCompanyCountryCodeAlpha2() {
        return new String(mCountryCode);
    }

    public static void setCurrentFlash(Context context, String flashIndex) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(currentFlash, flashIndex);
        editor.apply();
    }

    public static String getCurrentFlash(Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        return settings.getString(currentFlash, Camera.Parameters.FLASH_MODE_OFF);
    }

    public static String getDefaultEmailRecipient(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final String key = context.getResources().getString(R.string.preference_default_email_recipient);
        return settings.getString(key, "");
    }

    public static void setLastSelectedType(Context context, int type) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(lastSelectedType, type);
        editor.apply();
    }

    public static int getLastSelectedTypeOrDefault(Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        return settings.getInt(lastSelectedType, getDefaultTypeForApp());
    }

    public static String getCommunicationExtraData(Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        String companyId = settings.getString(currentCompanyIdKey, "");
        String userId = settings.getString(currentUserIdKey, "");

        JSONObject json = new JSONObject();
        try {
            json.put("UserId", userId);
            json.put("CompanyId", companyId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    /**
     * @param format one of NV16, NV21, or YV12.
     */
    public static String getQrStringUsingPlayServices(Context context, byte[] data, int width, int height, int format) {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context.getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        if (!barcodeDetector.isOperational()) {
            //txtView.setText("Could not set up the detector!");
        } else {
            Frame frame = new Frame.Builder()
                    .setImageData(ByteBuffer.wrap(data), width, height, format)
                    .build();
            SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);
            for (int i = 0; i < barcodes.size(); i++) {
                Barcode barcode = barcodes.valueAt(i);

                try {
                    JSONObject object = new JSONObject(barcode.rawValue);
                    if (object.has("uqr")) {
                        return barcode.rawValue;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static ArrayList<IntegrationData> getIntegrations(Context context) {
        final String[] integrationNames = context.getResources().getStringArray(R.array
                .integration_names);
        final String[] integrationDescriptions = context.getResources().getStringArray(R.array
                .integration_descriptions);
        final int[] integrationIdies = context.getResources().getIntArray(R.array
                .integrations_idies);
        final TypedArray integrationIconIdies = context.getResources().obtainTypedArray(R.array
                .integrations_icon_idies);

        //TypedArray is not mocked in robolectric so every time it return empty array. So to make
        // testing possible we need to to create int array filled with zeroes.
        final int[] integrationIconIdiesArray;
        if (integrationIconIdies.length() == 0) {
            integrationIconIdiesArray = new int[integrationNames.length];
            for (int i = 0; i < integrationIconIdiesArray.length; i++) {
                integrationIconIdiesArray[i] = 0;
            }
        } else {
            integrationIconIdiesArray = new int[integrationIconIdies.length()];
            for (int i = 0; i < integrationIconIdiesArray.length; i++) {
                integrationIconIdiesArray[i] = integrationIconIdies.getResourceId(i, -1);
            }
        }

        ArrayList<IntegrationData> integrations = new ArrayList<>();

        for (int i = 0; i < integrationIdies.length; i++) {
            integrations.add(new IntegrationData(integrationNames[i], integrationDescriptions[i],
                    integrationIconIdiesArray[i], integrationIdies[i]));
        }

        integrationIconIdies.recycle();
        return integrations;
    }

    public static Bitmap getCompressedBitmapFromUri(Uri selectedImage, Context context) {
        InputStream imageStream = null;
        try {
            imageStream = context.getContentResolver()
                    .openInputStream(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false; // http://developer.android
        // .com/guide/practices/screens_support.html#scaling
        opts.inSampleSize = 1;
        Bitmap resizedBitmap = null;
        while (resizedBitmap == null) {
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(imageStream, null, opts);
                if (bitmap == null) { // unsupported image format
                    Toast toast = Toast.makeText(context, R.string
                            .visma_blue_toast_unsupported_image_format, Toast
                            .LENGTH_LONG);
                    toast.show();
                    return null;
                }
                // This call takes care of recycling bitmap
                resizedBitmap = VismaUtils.getResizedBitmap(bitmap, context.getResources()
                        .getInteger(R.integer.targetImageHeight), VismaUtils
                        .shouldRotateBitmap(bitmap));
            } catch (OutOfMemoryError e) {
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
                if (resizedBitmap != null) {
                    resizedBitmap.recycle();
                    resizedBitmap = null;
                }
                opts.inSampleSize *= 2;
            }

            if (bitmap != null && bitmap != resizedBitmap) {
                bitmap.recycle();
                bitmap = null;
            }
        }

        return resizedBitmap;
    }

    public static void setCachedImageUrl(Context context, String imageUrl) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(lastCachedImageURl, imageUrl);
        editor.apply();
    }

    public static String getCachedImageUrl(Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        return settings.getString(lastCachedImageURl, null);
    }

    public static boolean hasPdfFileSharing() {
        boolean hasPdfShare;
        switch (BlueConfig.getAppType()) {
            case UNKNOWN:
            case MAMUT:
            case EXPENSE_MANAGER:
            case NETVISOR:
            case SEVERA:
            case ACCOUNTVIEW:
                hasPdfShare = false;
                break;
            case VISMA_ONLINE:
            case EACCOUNTING:
                hasPdfShare = true;
                break;
            default:
                hasPdfShare = false;
        }

        return hasPdfShare;
    }

    private static void setDefaultCurrency(Context context, String defaultCurrency) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        if (defaultCurrency == null) {
           defaultCurrency = ExpenseCurrencyAdapter.emptyValue(context).guid;
        }
        editor.putString(DEFAULT_CURRENCY, defaultCurrency);
        editor.apply();
    }

    public static String getDefaultCurrency(Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        return settings.getString(DEFAULT_CURRENCY, null);
    }

    public static void setExpenseDefaultCurrency(Context context, ExpenseCustomData data) {
        if (data == null || data.currency == null) {
            setDefaultCurrency(context, null);
        } else {
            setDefaultCurrency(context, data.currency.guid);
        }
    }

    public static void forceKeyboardClose(@NonNull Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context
                .INPUT_METHOD_SERVICE);
        View currentFocusView = activity.getCurrentFocus();
        if (currentFocusView != null) {
            IBinder binder = currentFocusView.getWindowToken();
            if (binder != null) {
                imm.hideSoftInputFromWindow(binder, 0);
            }
        }

    }

    public static void setUsesSupplierInvoiceApproval(Context context, boolean isEnabled) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(USES_SUPPLIER_INVOICE_APPROVAL, isEnabled);
        editor.apply();
    }

    public static boolean isSupplierInvoiceApprovalEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        return settings.getBoolean(USES_SUPPLIER_INVOICE_APPROVAL, false);
    }
}

