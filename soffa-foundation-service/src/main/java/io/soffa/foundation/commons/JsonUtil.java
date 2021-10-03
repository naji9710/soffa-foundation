package io.soffa.foundation.commons;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import io.soffa.foundation.logging.Logger;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class JsonUtil {

    private static final Logger LOGGER = Logger.create(JsonUtil.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    static {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Date.class, new DateDeserializers.DateDeserializer() {
            @Override
            public Date deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                try {
                    return super.deserialize(parser, ctxt);
                } catch (InvalidFormatException e) {
                    if (parser.hasToken(JsonToken.VALUE_STRING)) {
                        String string = parser.getText().trim();
                        return DateUtil.parse(string);
                    }
                    throw e;
                }
            }
        });
        MAPPER.registerModule(simpleModule);
    }

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
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        if (type == String.class) {
            return type.cast(jsonString);
        }
        return MAPPER.readValue(jsonString, type);
    }

    @SneakyThrows
    public static <T> T convert(Object input, Class<T> type) {
        if (input == null) {
            return ClassUtil.newInstance(type);
        }
        if(type.isInstance(input)) {
            return type.cast(input);
        }
        return MAPPER.convertValue(input, type);
    }

    @SneakyThrows
    public static String serialize(Object src) {
        if (src == null) return null;
        if (src instanceof String) return src.toString();
        return MAPPER.writeValueAsString(src);
    }

    @SneakyThrows
    public static void serializeToFile(Object content, File file) {
        if (content != null) {
            FileUtils.writeStringToFile(file, JsonUtil.serialize(content), StandardCharsets.UTF_8);
        } else {
            LOGGER.warn("Nothing was written to file because provided content is null.");
        }
    }

    @SneakyThrows
    public static <T> Map<String, T> deserializeMap(String input) {
        if (input == null) {
            new HashMap<>();
        }
        MapLikeType mapType = MAPPER.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class);
        return MAPPER.readValue(input, mapType);
    }

    @SneakyThrows
    public static <T> Map<String, T> deserializeMap(InputStream input) {
        if (input == null) {
            new HashMap<>();
        }
        MapLikeType mapType = MAPPER.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class);
        return MAPPER.readValue(input, mapType);
    }

    @SneakyThrows
    public static <T> Map<String, T> deserializeMap(InputStream input, Class<T> type) {
        if (input == null) {
            return new HashMap<>();
        }
        MapLikeType mapType = MAPPER.getTypeFactory().constructMapLikeType(Map.class, String.class, type);
        return MAPPER.readValue(input, mapType);
    }

    @SneakyThrows
    public static <T> Map<String, T> deserializeMap(String input, Class<T> type) {
        if (input == null) {
            return new HashMap<>();
        }
        MapLikeType mapType = MAPPER.getTypeFactory().constructMapLikeType(Map.class, String.class, type);
        return MAPPER.readValue(input, mapType);
    }

    @SneakyThrows
    public static <T> List<T> deserializeList(String input, Class<T> type) {
        if (StringUtils.isBlank(input)) {
            return new ArrayList<>();
        }
        ArrayType mapType = MAPPER.getTypeFactory().constructArrayType(type);
        return Arrays.asList(MAPPER.readValue(input, mapType));
    }

    @SneakyThrows
    public static <T> List<T> deserializeList(InputStream input, Class<T> type) {
        if (input == null) {
            return new ArrayList<>();
        }
        ArrayType mapType = MAPPER.getTypeFactory().constructArrayType(type);
        return Arrays.asList(MAPPER.readValue(input, mapType));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object input) {
        return toMap(input, Object.class);
    }

    @SuppressWarnings("unchecked")
    public static <E> Map<String, E> toMap(Object input, Class<E> valueClass) {
        if (input == null) {
            return new HashMap<>();
        }
        if (input instanceof Map) {
            return (Map<String, E>) input;
        }
        MapLikeType type = MAPPER.getTypeFactory().constructMapLikeType(Map.class, String.class, valueClass);
        if (input instanceof String) {
            try {
                return MAPPER.readValue((String) input, type);
            } catch (IOException e) {
                return new HashMap<>();
            }
        } else {
            return MAPPER.convertValue(input, type);
        }
    }


}
