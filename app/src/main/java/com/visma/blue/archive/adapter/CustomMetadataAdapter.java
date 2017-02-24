package com.visma.blue.archive.adapter;

import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.visma.blue.network.DateTypeDeserializer;

import java.util.Date;

public class CustomMetadataAdapter extends MetadataAdapter {

    protected static final Gson sGson;

    static {
        sGson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeDeserializer()).create();
    }

    public CustomMetadataAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }
}
