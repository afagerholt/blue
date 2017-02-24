package com.visma.blue.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.visma.blue.network.requests.DeletePhotoRequest;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class DeletePhotoWsTest extends BaseWsTest {

    private DeletePhotoRequest<Base> getDeletePhotoRequest(String token, String photoId) {
        return new DeletePhotoRequest<>(RuntimeEnvironment.application,
                token,
                photoId,
                Base.class,
                null,
                null);

    }

    @Test
    public void shouldCreateDeleteRequest() throws AuthFailureError {
        DeletePhotoRequest<Base> request = getDeletePhotoRequest(TOKEN, PHOTO_ID);

        assertEquals("Request method should be Delete!",
                Request.Method.DELETE,
                request.getMethod());
    }

    @Test
    public void shouldAddPhotoIdInUrl() throws IOException {
        DeletePhotoRequest<Base> request = getDeletePhotoRequest(TOKEN, PHOTO_ID);

        String loginWsUrl = request.getUrl();
        assertTrue("Photo id should be added in url!", loginWsUrl.contains(PHOTO_ID));
    }
}
