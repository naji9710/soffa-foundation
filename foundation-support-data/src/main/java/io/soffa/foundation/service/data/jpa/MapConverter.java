package io.soffa.foundation.service.data.jpa;


import io.soffa.foundation.commons.JsonUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Map;

@Converter
public class MapConverter implements AttributeConverter<Map<String, Object>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, Object> map) {
        return JsonUtil.serializeIgnoreAccess(map);
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        return JsonUtil.toMapFullAccess(dbData);
    }


}
