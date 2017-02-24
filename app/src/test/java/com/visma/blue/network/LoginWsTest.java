package com.visma.blue.network;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.visma.blue.network.containers.GetTokenAnswer;
import com.visma.blue.network.requests.GetTokenRequest;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.util.Map;

import okhttp3.mockwebserver.MockResponse;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class LoginWsTest extends BaseWsTest {

    private GetTokenRequest<GetTokenAnswer> getCompaniesRequest(String userName, String
            password, String companyId) {
        return new GetTokenRequest<>(RuntimeEnvironment.application,
                userName,
                password,
                companyId,
                GetTokenAnswer.class,
                null,
                null);

    }

    @Test
    public void shouldHaveRequiredHeaders() throws AuthFailureError {
        GetTokenRequest<GetTokenAnswer> request = getCompaniesRequest(USER_NAME, PASSWORD,
                COMPANY_ID);
        assertTrue("Companies request must have user name header!",
                request.getHeaders().containsKey("VoUser"));
        assertTrue("Companies request must have password header!",
                request.getHeaders().containsKey("VoPass"));
    }

    @Test
    public void shouldHaveRequiredHeaderCorrectValues() throws AuthFailureError {
        GetTokenRequest<GetTokenAnswer> request = getCompaniesRequest(USER_NAME, PASSWORD,
                COMPANY_ID);
        Map<String, String> headers = request.getHeaders();

        assertEquals("Set wrong user name header!",
                Base64.encodeToString(USER_NAME.getBytes(), Base64.NO_WRAP),
                headers.get("VoUser"));
        assertEquals("Set wrong password header!",
                Base64.encodeToString(PASSWORD.getBytes(), Base64.NO_WRAP),
                headers.get("VoPass"));
    }

    @Test
    public void shouldAddCompanyIdInUrl() throws IOException {
        GetTokenRequest<GetTokenAnswer> request = getCompaniesRequest(USER_NAME, PASSWORD,
                COMPANY_ID);

        String loginWsUrl = request.getUrl();
        assertTrue("Company id should be added in url!", loginWsUrl.contains("customerId"));
        assertEquals("Company id value is not correct!", COMPANY_ID, getValueFromURL(loginWsUrl,
                "customerId"));
    }

    @Test
    public void shouldParseGoodResponse() throws IOException {
        final String goodResponseJSON = "{\n" +
                "  \"Response\": 1,\n" +
                "  \"Token\": \"Vw4xSdtrGkuOsB2n8ImEcQeEjvKQ0LnAnn7Mv7-SxKtQ\"\n" +
                "}";

        mMockWebServer.enqueue(new MockResponse().setBody(goodResponseJSON));

        String responseFromServer = getResponseFromLocalRequest(mLocalServerUrl);
        assertEquals("Received wrong response!", goodResponseJSON, responseFromServer);
        Gson gson = new GsonBuilder().create();
        GetTokenAnswer tokenAnswer = gson.fromJson(responseFromServer, GetTokenAnswer.class);
        assertNotNull("Token response should have been created!", tokenAnswer);
        assertNotNull("token should not be null!", tokenAnswer.token);
    }

    @Test
    public void shouldParseBadJSONResponse() throws IOException {
        final String badJSONResponse = "{}";
        mMockWebServer.enqueue(new MockResponse().setBody(badJSONResponse));
        String responseFromServer = getResponseFromLocalRequest(mLocalServerUrl);
        assertEquals("Received wrong response!", badJSONResponse, responseFromServer);
        Gson gson = new GsonBuilder().create();
        GetTokenAnswer tokenAnswer = gson.fromJson(responseFromServer, GetTokenAnswer.class);
        assertNotNull("Token response should have been created!", tokenAnswer);
        assertNull("token should not be null!", tokenAnswer.token);
    }
}