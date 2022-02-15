package io.soffa.foundation.commons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;


public final class ObjectUtil {

    private static final ObjectMapper MAPPER = ObjectFactory.create(new ObjectMapper());

    private ObjectUtil() {
    }

    public static <T> T convert(Object input, Class<T> type) {
        return ObjectFactory.convert(MAPPER, input, type);
    }

    @SneakyThrows
    public static <T> byte[] serialize(T input) {
        return JsonStream.serialize(input).getBytes(StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static <T> T deserialize(byte[] input, Class<T> type) {
        return JsonIterator.deserialize(input, type);
    }

}
