package org.openhab.binding.linktap.internal;

import java.io.Reader;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LinktapUtils {
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private LinktapUtils() {
        // hidden utility class constructor
    }

    public static <T> T fromJson(String json, Class<T> dataClass) {
        return GSON.fromJson(json, dataClass);
    }

    public static <T> T fromJson(Reader reader, Class<T> dataClass) {
        return GSON.fromJson(reader, dataClass);
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }
}
