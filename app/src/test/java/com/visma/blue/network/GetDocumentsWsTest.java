package com.visma.blue.network;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.visma.blue.network.containers.GetMetadataListAnswer;
import com.visma.blue.network.requests.GetMetadataListRequest;

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

public class GetDocumentsWsTest extends BaseWsTest {

    private GetMetadataListRequest<GetMetadataListAnswer> getMetaDataListRequest(String token) {
        return new GetMetadataListRequest<GetMetadataListAnswer>(RuntimeEnvironment.application,
                token,
                GetMetadataListAnswer.class,
                null,
                null);
    }

    @Test
    public void shouldHaveRequiredHeaders() throws AuthFailureError {
        GetMetadataListRequest<GetMetadataListAnswer> request = getMetaDataListRequest(TOKEN);
        assertTrue("Metadatalist request must have token header!",
                request.getHeaders().containsKey("VoToken"));

    }

    @Test
    public void shouldHaveRequiredHeaderCorrectValues() throws AuthFailureError {
        GetMetadataListRequest<GetMetadataListAnswer> request = getMetaDataListRequest(TOKEN);
        Map<String, String> headers = request.getHeaders();

        assertEquals("Set wrong token header value!",
                Base64.encodeToString(TOKEN.getBytes(), Base64.NO_WRAP),
                headers.get("VoToken"));
    }

    @Test
    public void shouldParseGoodResponse() throws IOException {
        final String goodResponseJSON = "{ \n" +
                "   \"Response\":1,\n" +
                "   \"MetaData\":[ \n" +
                "      { \n" +
                "         \"CanDelete\":true,\n" +
                "         \"ChangedUtc\":1423215787513,\n" +
                "         \"Comment\":\"Comment\",\n" +
                "         \"ContentType\":\"image\\/jpeg\",\n" +
                "         \"Currency\":\"EUR\",\n" +
                "         \"CustomData\":[],\n" +
                "         \"Date\":\"2015-02-06\",\n" +
                "         \"DueAmount\":55555.00,\n" +
                "         \"DueDate\":\"2015-02-28\",\n" +
                "         \"InvoiceDate\":\"2015-02-06\",\n" +
                "         \"IsPaid\":false,\n" +
                "         \"Name\":\"Karl Svensson\",\n" +
                "         \"OrganisationNumber\":\"468644686\",\n" +
                "         \"OriginalFilename\":null,\n" +
                "         \"PhotoId\":\"61233706-5944-4984-9ef0-9da17be7dee7\",\n" +
                "         \"ReferenceNumber\":\"7357863\",\n" +
                "         \"TotalVatAmount\":55.00,\n" +
                "         \"Type\":\"0\",\n" +
                "         \"Verified\":true\n" +
                "      },\n" +
                "      { \n" +
                "         \"CanDelete\":false,\n" +
                "         \"ChangedUtc\":1422273679587,\n" +
                "         \"Comment\":\"\",\n" +
                "         \"ContentType\":\"image\\/jpeg\",\n" +
                "         \"Currency\":\"\",\n" +
                "         \"CustomData\":[],\n" +
                "         \"Date\":\"2015-01-26\",\n" +
                "         \"IsPaid\":false,\n" +
                "         \"Name\":\"\",\n" +
                "         \"OrganisationNumber\":\"\",\n" +
                "         \"OriginalFilename\":null,\n" +
                "         \"PhotoId\":\"be983056-5446-4d42-834c-c647e6dfb0a1\",\n" +
                "         \"ReferenceNumber\":\"\",\n" +
                "         \"Type\":\"0\",\n" +
                "         \"Verified\":true\n" +
                "      },\n" +
                "      { \n" +
                "         \"CanDelete\":true,\n" +
                "         \"ChangedUtc\":1433843096780,\n" +
                "         \"Comment\":\"\",\n" +
                "         \"ContentType\":\"image\\/jpeg\",\n" +
                "         \"Currency\":\"\",\n" +
                "         \"CustomData\":[ \n" +
                "            { \n" +
                "               \"Id\":\"paymentmethod_1\",\n" +
                "               \"Title\":\"Payment method\",\n" +
                "               \"Type\":0,\n" +
                "               \"ValueId\":\"-1\",\n" +
                "               \"ValueSubTitle\":null,\n" +
                "               \"ValueTitle\":\"Book travel expense claim\"\n" +
                "            },\n" +
                "            { \n" +
                "               \"Id\":\"dimension_1\",\n" +
                "               \"Title\":\"Projekti\",\n" +
                "               \"Type\":0,\n" +
                "               \"ValueId\":\"1\",\n" +
                "               \"ValueSubTitle\":null,\n" +
                "               \"ValueTitle\":\"Netvisor\"\n" +
                "            },\n" +
                "            { \n" +
                "               \"Id\":\"payrollratio_11\",\n" +
                "               \"Title\":\"Cost type\",\n" +
                "               \"Type\":0,\n" +
                "               \"ValueId\":\"159\",\n" +
                "               \"ValueSubTitle\":null,\n" +
                "               \"ValueTitle\":\"Taxi costs\"\n" +
                "            }\n" +
                "         ],\n" +
                "         \"Date\":\"2015-06-09\",\n" +
                "         \"IsPaid\":true,\n" +
                "         \"Name\":\"\",\n" +
                "         \"OrganisationNumber\":\"\",\n" +
                "         \"OriginalFilename\":null,\n" +
                "         \"PaymentDate\":\"2015-06-09\",\n" +
                "         \"PhotoId\":\"b80ab0ac-6bd9-4703-87b4-71fc1fd8c059\",\n" +
                "         \"ReferenceNumber\":\"\",\n" +
                "         \"Type\":\"1\",\n" +
                "         \"Verified\":true\n" +
                "      }\n" +
                "   ]\n" +
                "}";
        mMockWebServer.enqueue(new MockResponse().setBody(goodResponseJSON));

        String responseFromServer = getResponseFromLocalRequest(mLocalServerUrl);
        assertEquals("Received wrong response!", goodResponseJSON, responseFromServer);
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeDeserializer())
                .create();
        GetMetadataListAnswer metadataListAnswer = gson.fromJson(responseFromServer,
                GetMetadataListAnswer.class);
        assertNotNull("MetaDataList response should have been created!", metadataListAnswer);
        assertNotNull("MetaDataList list should not be null!", metadataListAnswer.metaDataList);
    }

    @Test
    public void shouldParseBadJSONResponse() throws IOException {
        final String badJSONResponse = "{}";
        mMockWebServer.enqueue(new MockResponse().setBody(badJSONResponse));
        String responseFromServer = getResponseFromLocalRequest(mLocalServerUrl);
        assertEquals("Received wrong response!", badJSONResponse, responseFromServer);
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeDeserializer())
                .create();
        GetMetadataListAnswer metadataListAnswer = gson.fromJson(responseFromServer,
                GetMetadataListAnswer.class);
        assertNotNull("MetaDataList response should have been created!", metadataListAnswer);
        assertNull("MetaDataList list should not be null!", metadataListAnswer.metaDataList);
    }

    @Test
    public void shouldAddPageNumberInUrl() throws IOException {
        GetMetadataListRequest<GetMetadataListAnswer> request = getMetaDataListRequest(TOKEN);

        String loginWsUrl = request.getUrl();
        assertTrue("Page number should be added in url!", loginWsUrl.contains("pageNumber"));
    }

    @Test
    public void shouldAddPageSizeInUrl() throws IOException {
        GetMetadataListRequest<GetMetadataListAnswer> request = getMetaDataListRequest(TOKEN);

        String loginWsUrl = request.getUrl();
        assertTrue("Page number should be added in url!", loginWsUrl.contains("pageSize"));
    }
}
