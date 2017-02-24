package com.visma.blue.network;

import android.content.Context;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import com.visma.blue.BlueConfig;
import com.visma.blue.misc.Logger;
import com.visma.blue.misc.VismaUtils;
import com.visma.blue.network.containers.ServerData;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class BaseRequest<T> extends Request<T> {

    private static final int TIME_OUT_NORMAL = 15 * 1000;
    private static final int TIME_OUT_DEV = 15 * 60 * 1000;

    private static final Gson sGson;

    static {
        sGson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeDeserializer()).create();
    }

    private final Class<T> clazz;
    private final Response.Listener<T> listener;

    private final Context mContext;
    private final String mToken;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url   URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     */
    public BaseRequest(Context context,
                       String token,
                       int method,
                       String url,
                       Class<T> clazz,
                       Response.Listener<T> listener,
                       Response.ErrorListener errorListener) {
        super(method, url, errorListener);

        this.clazz = clazz;
        this.listener = listener;
        this.mContext = context;
        this.mToken = token;

        this.setRetryPolicy(new DefaultRetryPolicy(
                getTimeout(), //Default is 2.5 seconds.
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            T parsedData = sGson.fromJson(json, clazz);
            // Handle the errors that are sent with the 200 code
            int responseCode = ((Base) parsedData).response;
            if (responseCode != OnlineResponseCodes.Ok) {
                final BlueNetworkError exception = new BlueNetworkError(responseCode, ((Base) parsedData).message);
                Logger.logError(new Exception("Network " + responseCode, exception));
                return Response.error(new BlueNetworkError(responseCode, ((Base) parsedData).message));
            }

            return Response.success(
                    parsedData,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        /*
        TimeoutError -- ConnectionTimeout or SocketTimeout
        AuthFailureError -- 401 ( UNAUTHORIZED ) && 403 ( FORBIDDEN )
        ServerError -- 5xx
        ClientError -- 4xx(Created in this demo for handling all 4xx error which are treated as Client side errors)
        NetworkError -- No network found
        ParseError -- Error while converting HTTP Response to JSONObject.
         */

        if (volleyError instanceof ServerError) {
            try {
                String json = new String(
                        volleyError.networkResponse.data,
                        HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));
                JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
                if (object.has("response") && object.has("message")) {
                    final int response = object.getInt("response");
                    final String message = object.getString("Message");
                    Logger.logError(new Exception("Network " + response, volleyError));
                    return new BlueNetworkError(response, message);
                } else if (object.has("Response")) {
                    final int response = object.getInt("Response");
                    Logger.logError(new Exception("Network " + response, volleyError));
                    return new BlueNetworkError(object.getInt("Response"));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (volleyError.networkResponse != null) {
            Logger.logError(new Exception("Network " + volleyError.networkResponse.statusCode, volleyError));
        } else {
            Logger.logError(new Exception("Network", volleyError));
        }

        return super.parseNetworkError(volleyError);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new ArrayMap<String, String>(4);

        Locale currentLocale = Locale.getDefault();
        String acceptLanguage = String.format(Locale.US,
                "%1$s-%2$s, %3$s;q=0.9, en;q=0.8",
                currentLocale.getLanguage(),
                currentLocale.getCountry().toLowerCase(Locale.US),
                currentLocale.getLanguage());

        headers.put("Accept-Language", acceptLanguage);
        //headers.put("Accept-Language", "en");
        headers.put("AppVersion", Integer.toString(VolleySingleton.getAppVersion()));
        headers.put("AppId", Integer.toString(BlueConfig.getAppId()));
        headers.put("VismaCustomData",
                Base64.encodeToString(VismaUtils.getCommunicationExtraData(mContext).getBytes(), Base64.NO_WRAP));

        if (!TextUtils.isEmpty(mToken)) {
            headers.put("VoToken", Base64.encodeToString(mToken.getBytes(), Base64.NO_WRAP));
        }

        return headers;
    }

    @SafeVarargs
    protected static String getCompleteUrl(String request, Pair<String, String>... params) {
        Uri.Builder uriBuilder = Uri.parse(VolleySingleton.getBaseUrl()).buildUpon().appendEncodedPath(request);

        //add parameters
        if (params.length != 0) {
            for (Pair<String, String> p : params) {
                uriBuilder.appendQueryParameter(p.first, p.second);
            }
        }

        return uriBuilder.build().toString();
    }

    private int getTimeout() {
        ServerData serverData = VolleySingleton.getInstance().getServer();
        if (serverData == null || !serverData.developer) {
             return TIME_OUT_NORMAL;
        } else {
            return TIME_OUT_DEV;
        }
    }
}
