package io.soffa.foundation.commons;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.soffa.foundation.core.RequestContext;
import lombok.SneakyThrows;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class JsonUtil {

    private static final ObjectMapper MAPPER = ObjectFactory.create(new ObjectMapper());

    private JsonUtil() {
    }

    public static boolean isJson(String input) {
        try {
            new JSONObject(input);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public static String fromXml(String xmlInput) {
        if (xmlInput == null) return null;
        return XML.toJSONObject(xmlInput).toString();
    }

    @SneakyThrows
    public static <T> T fromXml(String xmlInput, String root, Class<T> kind) {
        if (xmlInput == null) {
            return ClassUtil.newInstance(kind);
        }
        JSONObject object = XML.toJSONObject(xmlInput);
        return deserialize(object.getJSONObject(root).toString(), kind);
    }

    @SneakyThrows
    public static <T> T deserialize(String jsonString, Class<T> type) {
        return ObjectFactory.deserialize(MAPPER, jsonString, type);
    }

    @SneakyThrows
    public static <T> T deserialize(byte[] data, Class<T> type) {
        return ObjectFactory.deserialize(MAPPER, new String(data, StandardCharsets.UTF_8), type);
    }

    public static <T> T convert(Object input, Class<T> type) {
        return ObjectFactory.convert(MAPPER, input, type);
    }

    public static String serialize(Object src) {
        return ObjectFactory.serialize(MAPPER, src);
    }

    public static void serializeToFile(Object content, File file) {
        ObjectFactory.serializeToFile(MAPPER, content, file);
    }

    public static <T> Map<String, T> deserializeMap(String input) {
        return ObjectFactory.deserializeMap(MAPPER, input);
    }

    public static <T> Map<String, T> deserializeMap(InputStream input) {
        return ObjectFactory.deserializeMap(MAPPER, input);
    }

    public static <T> Map<String, T> deserializeMap(InputStream input, Class<T> type) {
        return ObjectFactory.deserializeMap(MAPPER, input, type);
    }

    public static <T> Map<String, T> deserializeMap(String input, Class<T> type) {
        return ObjectFactory.deserializeMap(MAPPER, input, type);
    }

    public static <T> T deserializeParametricType(String input, Class<?> rawType, Class<?>... parameterClasses) {
        return ObjectFactory.deserializeParametricType(MAPPER, input, rawType, parameterClasses);

    }

    public static <T> List<T> deserializeList(String input, Class<T> type) {
        return ObjectFactory.deserializeList(MAPPER, input, type);
    }

    public static <T> List<T> deserializeList(InputStream input, Class<T> type) {
        return ObjectFactory.deserializeList(MAPPER, input, type);
    }

    public static Map<String, Object> toMap(Object input) {
        return toMap(input, Object.class);
    }

    public static <E> Map<String, E> toMap(Object input, Class<E> valueClass) {
        return ObjectFactory.toMap(MAPPER, input, valueClass);
    }


    public static RequestContext clone(RequestContext context) {
        return JsonUtil.deserialize(JsonUtil.serialize(context), RequestContext.class);
    }
}
