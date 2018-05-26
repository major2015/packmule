package com.borderx.packamule.util;

import com.borderx.packamule.exceptions.DataSourceException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nullable;
import java.io.IOException;

public class ObjectMappers {

    private static final ObjectMapper DEFAULT_INSTANCE = new ObjectMapper();

    private static final ObjectMapper COMPACT_INSTANCE = new ObjectMapper();

    static {
        DEFAULT_INSTANCE.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        DEFAULT_INSTANCE.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DEFAULT_INSTANCE.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

        COMPACT_INSTANCE.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        COMPACT_INSTANCE.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        COMPACT_INSTANCE.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    public static ObjectMapper get() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Deserialize <code>json</code> to an object of type <code>clazz</code>.
     *
     * @throws DataSourceException
     */
    public static <T> T mustReadValue(@Nullable String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.readValue(json, clazz);
        } catch (IOException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * Deserialize <code>json</code> to an object, using <code>typeRef</code>
     * to determine class of the object.
     *
     * @throws DataSourceException
     */
    public static <T> T mustReadValue(@Nullable String json, TypeReference<T> typeRef) {
        if (json == null) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.readValue(json, typeRef);
        } catch (IOException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * Serialize <code>o</code> to a string.
     *
     * @throws DataSourceException
     */
    public static String mustWriteValue(@Nullable Object o) {
        if (o == null) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.writeValueAsString(o);
        } catch (IOException e) {
            throw new DataSourceException(e);
        }
    }

    public static String mustWriteValuePretty(@Nullable Object o) {
        if (o == null) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (IOException e) {
            throw new DataSourceException(e);
        }
    }

    public static JsonNode mustReadTree(@Nullable String json) {
        if (json == null) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.readTree(json);
        } catch (IOException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * Serialize <code>o</code> to a string using compact JSON serializer
     * (i.e., NON_DEFAULT only) if possible.
     *
     * @throws DataSourceException
     */
    public static String writeObjectCompact(@Nullable Object o) {
        try {
            try {
                // Not all objects can be serialized using COMPACT_INSTANCE.
                // E.g., objects without default constructor. So we fall back
                // to standard serializer.
                return COMPACT_INSTANCE.writeValueAsString(o);
            } catch (JsonMappingException e) {
                return DEFAULT_INSTANCE.writeValueAsString(o);
            }
        } catch (IOException e) {
            throw new DataSourceException(e);
        }
    }
}
