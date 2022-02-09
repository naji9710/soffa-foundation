package io.soffa.foundation.commons;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public final class ObjectUtil {

    private static final ObjectMapper MAPPER = ObjectFactory.create(new ObjectMapper());

    private ObjectUtil() {
    }

    public static <T> T convert(Object input, Class<T> type) {
        return ObjectFactory.convert(MAPPER, input, type);
    }

    public static byte[] serialize(Serializable input) {
        return SerializationUtils.serialize(input);
    }
    public static <T> T deserialize(byte[] input) {
        return SerializationUtils.deserialize(input);
    }

}
