package io.soffa.foundation.commons.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Jwt {

    private final String token;
    private final String subject;
    private final Map<String, Object> claims;
}
