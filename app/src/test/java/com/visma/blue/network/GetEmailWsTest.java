package com.visma.blue.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.visma.blue.network.containers.GetEmailAnswer;
import com.visma.blue.network.requests.GetEmailRequest;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

public class GetEmailWsTest extends BaseWsTest {

    private GetEmailRequest<GetEmailAnswer> getEmailRequest(String token) {
        return new GetEmailRequest<>(RuntimeEnvironment.application,
                token,
                GetEmailAnswer.class,
                null,
                null);

    }

    @Test
    public void shouldCreateGetRequest() throws AuthFailureError {
        GetEmailRequest<GetEmailAnswer> request = getEmailRequest(TOKEN);

        assertEquals("Request method should be GET!",
                Request.Method.GET,
                request.getMethod());
    }

    @Test
    public void shouldParseGoodResponse() throws IOException {
        final String goodResponseJSON = "{ Response:1, InboundEmail:\"test_inbound@visma.com\"}";
        mMockWebServer.enqueue(new MockResponse().setBody(goodResponseJSON));

        String responseFromServer = getResponseFromLocalRequest(mLocalServerUrl);
        assertEquals("Received wrong response!", goodResponseJSON, responseFromServer);
        Gson gson = new GsonBuilder().create();
        GetEmailAnswer getEmailAnswer = gson.fromJson(responseFromServer,
                GetEmailAnswer.class);
        assertNotNull("Create photo response should have been created!", getEmailAnswer);
        assertNotNull("Inbound email should not be null!", getEmailAnswer.inboundEmail);
    }

    @Test
    public void shouldParseBadJSONResponse() throws IOException {
        final String badJSONResponse = "{}";
        mMockWebServer.enqueue(new MockResponse().setBody(badJSONResponse));
        String responseFromServer = getResponseFromLocalRequest(mLocalServerUrl);
        assertEquals("Received wrong response!", badJSONResponse, responseFromServer);
        Gson gson = new GsonBuilder().create();
        GetEmailAnswer getEmailAnswer = gson.fromJson(responseFromServer,
                GetEmailAnswer.class);
        assertNotNull("Email response should have been created!", getEmailAnswer);
        assertNull("Inbound email should  be null!", getEmailAnswer.inboundEmail);
    }
}
