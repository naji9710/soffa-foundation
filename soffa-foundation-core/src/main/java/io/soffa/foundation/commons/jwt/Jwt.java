package io.soffa.foundation.commons.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Jwt {

    private String type = "JWT";
    private String token;
    private String subject;
    private Map<String, Object> claims;
    private int expiresIn;

    public Jwt(String token) {
        this.token = token;
    }

    public Jwt(String token, String subject, Map<String, Object> claims) {
        this.token = token;
        this.subject = subject;
        this.claims = claims;
    }

    public Jwt(String token, String subject, Map<String, Object> claims, int expiresIn) {
        this(token, subject, claims);
        this.expiresIn = expiresIn;
    }

    public Optional<String> lookupClaim(String... candidates) {
        if (claims == null || claims.isEmpty()) {
            return Optional.empty();
        }

        for (String claim : claims.keySet()) {
            for (String candidate : candidates) {
                if (claim.equalsIgnoreCase(candidate)) {
                    Object value = claims.get(claim);
                    if (value!=null) {
                        return Optional.of(value.toString());
                    }
                }
            }
        }
        return Optional.empty();
    }

    public int getExpiresInSeconds() {
        return expiresIn * 60;
    }

}



