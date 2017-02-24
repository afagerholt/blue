package com.visma.blue.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.visma.blue.BuildConfig;
import com.visma.blue.network.containers.ServerData;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class VolleySingleton {

    private static final String VISMA_COMMUNICATOR_SERVER_DATA = "VISMA_COMMUNICATOR_SERVER_DATA";
    private static final String VISMA_COMMUNICATOR_SERVER_LIST = "VISMA_COMMUNICATOR_SERVER_LIST";

    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mContext;

    private static String mUrl;

    private ServerData mServerData; // Default is live

    private VolleySingleton(Context context) {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        mContext = context.getApplicationContext();
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });

        initialize(context);
    }

    public static void init(@NonNull Context context) {
        mInstance = new VolleySingleton(context);

        //https://developer.android.com/training/articles/security-gms-provider.html
        ProviderInstaller.installIfNeededAsync(context, new ProviderInstaller
                .ProviderInstallListener() {
            @Override
            public void onProviderInstalled() {
                // Upp to date
            }

            @Override
            public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
                GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

                if (googleApiAvailability.isUserResolvableError(errorCode)) {
                    // Indicates that Google Play services is out of date, disabled, etc.
                    // Prompt the user to install/update/enable Google Play services.
                    googleApiAvailability.showErrorNotification(mContext, errorCode);
                }
            }
        });
    }

    public static VolleySingleton getInstance() {
        return mInstance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext, new OkHttpStack());
            //mRequestQueue = Volley.newRequestQueue(mContext, new HurlStack(null, ));
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    protected static String getBaseUrl() {
        return mUrl;
    }

    protected static int getAppVersion() {
        return 22;
    }

    public enum ServerType {
        ALPHA(1), LIVE(2);

        private final int code;

        ServerType(int code) {
            this.code = code;
        }

        public int getValue() {
            return code;
        }
    }

    private void initialize(Context context) {
        if (BuildConfig.DEBUG) {
            mServerData = getSavedServerData(context);
        } else {
            mServerData = getServerData(ServerType.LIVE);
        }

        setServer(mServerData);
    }

    public void setAndSaveServer(Context context, ServerData serverData) {
        setServer(serverData);

        Gson gson = new GsonBuilder().create();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(VISMA_COMMUNICATOR_SERVER_DATA, gson.toJson(serverData));
        editor.apply();
    }

    public void saveServerList(Context context, ArrayList<ServerData> serverData) {
        Gson gson = new GsonBuilder().create();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(VISMA_COMMUNICATOR_SERVER_LIST, gson.toJson(serverData));
        editor.apply();
    }

    private ServerData getSavedServerData(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String savedServerData = settings.getString(VISMA_COMMUNICATOR_SERVER_DATA, null);

        if (savedServerData != null) {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(savedServerData, ServerData.class);
        }

        return getServerData(ServerType.LIVE);
    }

    public ArrayList<ServerData> getSavedServerList(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String savedServerData = settings.getString(VISMA_COMMUNICATOR_SERVER_LIST, null);

        if (savedServerData != null) {
            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<ArrayList<ServerData>>() {
            }.getType();
            return gson.fromJson(savedServerData, type);
        }

        return new ArrayList<>();
    }

    public ServerData getServer() {
        return mServerData;
    }

    protected void setServer(ServerData serverData) {
        mServerData = serverData;
        mUrl = mServerData.url;
    }

    public ServerData getServerData(ServerType type) {
        ServerData serverData;
        switch (type) {
            case ALPHA:
                String alphaUrl = "https://photoservice.test.vismaonline.com/Api/App/AppService" + ".svc";
                serverData = new ServerData("Test", alphaUrl);
                break;
            case LIVE:
            default:
                String prodUrl = "https://photoservice.vismaonline.com/Api/App/AppService.svc";
                serverData = new ServerData("Production", prodUrl);
                break;
        }
        return serverData;
    }
}