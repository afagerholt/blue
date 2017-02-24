package com.visma.blue.network;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.visma.blue.custom.CustomMetaData;
import com.visma.blue.network.containers.CreatePhotoAnswer;
import com.visma.blue.network.containers.OnlinePhoto;
import com.visma.blue.network.requests.CreatePhotoRequest;

import org.json.JSONException;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import okhttp3.mockwebserver.MockResponse;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class CreatePhotoWsTest extends BaseWsTest {

    private CreatePhotoRequest<CreatePhotoAnswer> getCreatePhotoRequest(String token,
                                                                        OnlinePhoto
                                                                                onlinePhoto) {
        return new CreatePhotoRequest<>(RuntimeEnvironment.application,
                token,
                onlinePhoto,
                CreatePhotoAnswer.class,
                null,
                null);
    }

    @Test
    public void shouldHaveRequiredHeaders() throws AuthFailureError {
        CreatePhotoRequest<CreatePhotoAnswer> request = getCreatePhotoRequest(TOKEN,
                CustomMetaData.getCustomOnlinePhoto());
        assertTrue("Create photo request must have token header!",
                request.getHeaders().containsKey("VoToken"));

    }

    @Test
    public void shouldHaveRequiredHeaderCorrectValues() throws AuthFailureError {
        CreatePhotoRequest<CreatePhotoAnswer> request = getCreatePhotoRequest(TOKEN,
                CustomMetaData.getCustomOnlinePhoto());
        Map<String, String> headers = request.getHeaders();

        assertEquals("Set wrong token header value!",
                Base64.encodeToString(TOKEN.getBytes(), Base64.NO_WRAP),
                headers.get("VoToken"));
    }

    @Test
    public void shouldCreateRequestBodyWithPhotoInfo() throws AuthFailureError, JSONException,
            UnsupportedEncodingException {
        CreatePhotoRequest<CreatePhotoAnswer> request = getCreatePhotoRequest(TOKEN,
                CustomMetaData.getCustomOnlinePhoto());

        String body = new String(request.getBody(), "UTF-8");
        assertTrue("Request body must have photo information!",
                body.contains("metadata"));
        assertTrue("Request body must have photo file!",
                body.contains("file"));
    }

    @Test
    public void shouldCreateMultiPartPostRequest() throws AuthFailureError, JSONException,
            UnsupportedEncodingException {
        CreatePhotoRequest<CreatePhotoAnswer> request = getCreatePhotoRequest(TOKEN,
                CustomMetaData.getCustomOnlinePhoto());

        assertTrue("Request content type should be multipart/form-data!",
                request.getBodyContentType().contains("multipart/form-data"));
    }

    @Test
    public void shouldAddBoundaryParameterInRequest() throws AuthFailureError,
            JSONException,
            UnsupportedEncodingException {
        CreatePhotoRequest<CreatePhotoAnswer> request = getCreatePhotoRequest(TOKEN,
                CustomMetaData.getCustomOnlinePhoto());

        assertTrue("Request content type should have boundary parameter!",
                request.getBodyContentType().contains("boundary"));
    }

    @Test
    public void shouldCreatePostRequest() throws AuthFailureError {
        CreatePhotoRequest<CreatePhotoAnswer> request = getCreatePhotoRequest(TOKEN,
                CustomMetaData.getCustomOnlinePhoto());

        assertEquals("Request method should be POST!",
                Request.Method.POST,
                request.getMethod());
    }

    @Test
    public void shouldParseGoodResponse() throws IOException {
        final String goodResponseJSON = "{ Response:1, PhotoId:\"test_photo_id\"}";
        mMockWebServer.enqueue(new MockResponse().setBody(goodResponseJSON));

        String responseFromServer = getResponseFromLocalRequest(mLocalServerUrl);
        assertEquals("Received wrong response!", goodResponseJSON, responseFromServer);
        Gson gson = new GsonBuilder().create();
        CreatePhotoAnswer createPhotoAnswer = gson.fromJson(responseFromServer,
                CreatePhotoAnswer.class);
        assertNotNull("Create photo response should have been created!", createPhotoAnswer);
        assertNotNull("Photo id should not be null!", createPhotoAnswer.photoGuid);
    }

    @Test
    public void shouldParseBadJSONResponse() throws IOException {
        final String badJSONResponse = "{}";
        mMockWebServer.enqueue(new MockResponse().setBody(badJSONResponse));
        String responseFromServer = getResponseFromLocalRequest(mLocalServerUrl);
        assertEquals("Received wrong response!", badJSONResponse, responseFromServer);
        Gson gson = new GsonBuilder().create();
        CreatePhotoAnswer createPhotoAnswer = gson.fromJson(responseFromServer,
                CreatePhotoAnswer.class);
        assertNotNull("Create photo response should have been created!", createPhotoAnswer);
        assertNull("Photo id should not be null!", createPhotoAnswer.photoGuid);
    }
}
