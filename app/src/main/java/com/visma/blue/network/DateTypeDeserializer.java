package com.visma.blue.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTypeDeserializer implements JsonDeserializer<Date> {
    private static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static Pattern mDatePattern = Pattern.compile("[\\d]{4}-[\\d]{2}-[\\d]{2}");

    static {
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        if (isStringDate(element.getAsString())) {
            String dateString = element.getAsString();
            try {
                return mDateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            return new Date(element.getAsLong());
        }

        return new Date();
    }

    // Matches a date in the format yyyy-mm-dd
    private boolean isStringDate(String input) {
        Matcher matcher = mDatePattern.matcher(input);
        if (matcher.find()) {
            return true;
        } else {
            return false;
        }
    }
}
