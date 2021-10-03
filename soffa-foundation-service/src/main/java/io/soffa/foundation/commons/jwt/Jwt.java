package io.soffa.foundation.commons.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
public class Jwt {

    private final String token;
    private final String subject;
    private final Map<String, Object> claims;

    public Optional<String> lookupClaim(String... candidates) {
        if (claims == null || claims.isEmpty()) {
            return Optional.empty();
        }

        for (String claim : claims.keySet()) {
            for (String candidate : candidates) {
                if (claim.equalsIgnoreCase(candidate)) {
                    return Optional.ofNullable(claims.get(claim).toString());
                }
            }
        }
        return Optional.empty();
    }
}
