package com.visma.blue.network;

import android.net.UrlQuerySanitizer;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import com.visma.blue.BuildConfig;
import com.visma.blue.custom.CustomApplication;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = CustomApplication.class)
public abstract class BaseWsTest {

    protected static final String USER_NAME = "UserName";
    protected static final String PASSWORD = "Password";
    protected static final String COMPANY_ID = "CompanyId";
    protected static final String TOKEN = "Token";
    protected static final String DEVICE_TOKEN = "DeviceToken";
    protected static final String PHOTO_ID = "PhotoId";
    protected static final int APP_ID = 1000;

    protected MockWebServer mMockWebServer;
    protected RequestQueue mRequestQueue;
    protected String mLocalServerUrl;

    protected String getResponseFromLocalRequest(String url) throws IOException {
        return new OkHttpClient().newCall(new Request.Builder()
                .url(url)
                .build()).execute()
                .body().string();
    }

    protected String getValueFromURL(String url, String key) throws
            UnsupportedEncodingException {
        UrlQuerySanitizer sanitizer = new UrlQuerySanitizer(url);
        return sanitizer.getValue(key);
    }

    @Before
    public void setUp() throws IOException {
        mRequestQueue = Volley.newRequestQueue(RuntimeEnvironment.application, new OkHttpStack());
        mMockWebServer = new MockWebServer();
        mMockWebServer.start();
        mLocalServerUrl = mMockWebServer.url("/").toString();
    }

    @After
    public void tearDown() throws IOException {
        mMockWebServer.shutdown();
        mRequestQueue.stop();
        mLocalServerUrl = null;
    }

}
