package io.soffa.foundation.service.data.jpa;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Map;

@Converter
public class MapConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final Logger LOG = LoggerFactory.getLogger(MapConverter.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        MAPPER.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    }

    @Override
    public String convertToDatabaseColumn(Map<String, Object> map) {

        String jsonString = null;
        try {
            jsonString = MAPPER.writeValueAsString(map);
        } catch (final JsonProcessingException e) {
            LOG.error("JSON writing error", e);
        }

        return jsonString;
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        Map<String, Object> metas = null;
        try {
            MapLikeType type = MAPPER.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class);
            metas = MAPPER.readValue(dbData, type);
        } catch (final IOException e) {
            LOG.error("JSON reading error", e);
        }

        return metas;
    }
}
