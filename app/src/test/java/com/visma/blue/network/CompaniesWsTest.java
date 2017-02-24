package com.visma.blue.network;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.visma.blue.network.containers.GetCompaniesAnswer;
import com.visma.blue.network.requests.GetCompaniesRequest;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import okhttp3.mockwebserver.MockResponse;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;


public class CompaniesWsTest extends BaseWsTest {

    private GetCompaniesRequest<GetCompaniesAnswer> getCompaniesRequest(String userName, String
            password, int appId) {
        return new GetCompaniesRequest<GetCompaniesAnswer>(RuntimeEnvironment.application,
                userName,
                password,
                appId,
                GetCompaniesAnswer.class,
                new Response.Listener<GetCompaniesAnswer>() {
                    @Override
                    public void onResponse(final GetCompaniesAnswer response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
    }

    @Test
    public void shouldHaveRequiredHeaders() throws AuthFailureError {
        GetCompaniesRequest<GetCompaniesAnswer> request = getCompaniesRequest(USER_NAME, PASSWORD,
                APP_ID);
        assertTrue("Companies request must have user name header!",
                request.getHeaders().containsKey("VoUser"));
        assertTrue("Companies request must have password header!",
                request.getHeaders().containsKey("VoPass"));
        assertTrue("Companies request must have user name header!",
                request.getHeaders().containsKey("AppId"));
    }

    @Test
    public void shouldHaveRequiredHeaderCorrectValues() throws AuthFailureError {
        GetCompaniesRequest<GetCompaniesAnswer> request = getCompaniesRequest(USER_NAME, PASSWORD,
                APP_ID);
        Map<String, String> headers = request.getHeaders();

        assertEquals("Set wrong user name header!",
                Base64.encodeToString(USER_NAME.getBytes(), Base64.NO_WRAP),
                headers.get("VoUser"));
        assertEquals("Set wrong password header!",
                Base64.encodeToString(PASSWORD.getBytes(), Base64.NO_WRAP),
                headers.get("VoPass"));
        assertEquals("Set wrong app id header!",
                "" + APP_ID,
                headers.get("AppId"));
    }

    @Test
    public void shouldParseGoodResponse() throws IOException {
        final String goodResponseJSON = "{\n" +
                "  \"Response\": 1,\n" +
                "  \"OnlineCustomers\": [\n" +
                "    {\n" +
                "      \"CountryCodeAlpha2\": \"SE\",\n" +
                "      \"Id\": \"cd69a2f0-87e4-4854-b4bd-a130a31ad5f8\",\n" +
                "      \"Name\": \"Appteam\",\n" +
                "      \"OrgNo\": \"555555-5555\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"CountryCodeAlpha2\": \"SE\",\n" +
                "      \"Id\": \"9f6cc76f-7b8e-4a08-a907-c2a1113a5480\",\n" +
                "      \"Name\": \"Appteam Ftg3\",\n" +
                "      \"OrgNo\": \"555555-5555\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"CountryCodeAlpha2\": \"SE\",\n" +
                "      \"Id\": \"e685cdd7-d6e1-47d6-a176-f900a66ed6fb\",\n" +
                "      \"Name\": \"Appteam Spcs\",\n" +
                "      \"OrgNo\": \"555555-5555\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"CountryCodeAlpha2\": \"DK\",\n" +
                "      \"Id\": \"3f334834-541a-46ee-b82d-17ea234b32f7\",\n" +
                "      \"Name\": \"Danska kunden\",\n" +
                "      \"OrgNo\": \"78541253\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"CountryCodeAlpha2\": \"NO\",\n" +
                "      \"Id\": \"355e3360-279d-4d42-81c1-9a5f92035787\",\n" +
                "      \"Name\": \"Norska kunden\",\n" +
                "      \"OrgNo\": \"789425892\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"CountryCodeAlpha2\": \"SE\",\n" +
                "      \"Id\": \"8c4123bf-e760-4270-9024-6a067b4474f6\",\n" +
                "      \"Name\": \"Nya företaget\",\n" +
                "      \"OrgNo\": \"555555-5555\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"CountryCodeAlpha2\": \"SE\",\n" +
                "      \"Id\": \"e6f12386-1434-4d2f-bad9-6fa2d94c000b\",\n" +
                "      \"Name\": \"Spcs2\",\n" +
                "      \"OrgNo\": \"555555-5555\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"UserId\": \"00000000-0000-0000-0000-000000000000\"\n" +
                "}";
        mMockWebServer.enqueue(new MockResponse().setBody(goodResponseJSON));

        String responseFromServer = getResponseFromLocalRequest(mLocalServerUrl);
        assertEquals("Received wrong response!", goodResponseJSON, responseFromServer);
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeDeserializer())
                .create();
        GetCompaniesAnswer companiesAnswer = gson.fromJson(responseFromServer, GetCompaniesAnswer
                .class);
        assertNotNull("Companies response should have been created!", companiesAnswer);
        assertNotNull("Companies list should not be null!", companiesAnswer.onlineCustomers);
        assertNotNull("User id should not be null!", companiesAnswer.userId);
    }

    @Test
    public void shouldParseBadJSONResponse() throws IOException {
        final String badJSONResponse = "{}";
        mMockWebServer.enqueue(new MockResponse().setBody(badJSONResponse));
        String responseFromServer = getResponseFromLocalRequest(mLocalServerUrl);
        assertEquals("Received wrong response!", badJSONResponse, responseFromServer);
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeDeserializer())
                .create();
        GetCompaniesAnswer companiesAnswer = gson.fromJson(responseFromServer, GetCompaniesAnswer
                .class);
        assertNotNull("Companies response should have been created!", companiesAnswer);
        assertNull("Companies list should be null!", companiesAnswer.onlineCustomers);
        assertNull("User id should  be null!", companiesAnswer.userId);
    }

}
