package com.visma.blue.network.requests;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.visma.blue.network.BaseRequest;
import com.visma.blue.network.DateTypeSerializer;
import com.visma.blue.network.containers.OnlinePhoto;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

public class CreatePhotoRequest<T> extends BaseRequest<T> {
    private static final String mRequest = "photosngmultipart";
    private static final String CUSTOM_BOUNDARY = "END_OF_PART";
    private OnlinePhoto mOnlinePhoto;

    public CreatePhotoRequest(Context context,
                              String token,
                              OnlinePhoto photo,
                              Class<T> clazz,
                              Response.Listener<T> listener,
                              Response.ErrorListener errorListener) {
        super(context,
                token,
                Method.POST,
                getCompleteUrl(mRequest),
                clazz,
                listener,
                errorListener);

        this.mOnlinePhoto = photo;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // The server specifies BodyStyle =
            // WebMessageBodyStyle.WrappedRequest
            // and UpdatePhoto(string token, string id, UpdatePhotoRequest
            // request);, i.e., parameter named photo
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .registerTypeAdapter(Date.class, new DateTypeSerializer()).create();
            JSONObject jsonPart = new JSONObject();
            JSONObject jsonMetadata = new JSONObject(gson.toJson(mOnlinePhoto));
            jsonPart.put("photo", jsonMetadata);

            builder.setBoundary(CUSTOM_BOUNDARY);
            builder.addBinaryBody("metadata", jsonPart.toString().getBytes());
            builder.addBinaryBody("file", mOnlinePhoto.getDocument());

            HttpEntity httpEntity = builder.build();
            httpEntity.writeTo(bos);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();

    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + CUSTOM_BOUNDARY;
    }
}