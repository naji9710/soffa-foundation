package io.soffa.foundation.commons.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.core.model.Authentication;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
public class DefaultJwtProcessor implements JwtEncoder, JwtDecoder {

    private static final Logger LOG = Logger.get(JwtJwksDecoder.class);
    private JwtEncoderConfig config;

    @Override
    public Jwt create(String subjet, Map<String, Object> claims) {
        return create(subjet, claims, config.getDefaultTtl());
    }

    @Override
    public Jwt create(String subjet, Map<String, Object> claims, int ttl) {
        String token = JwtUtil.create(
            config.getIssuer(),
            config.getSecret(),
            subjet,
            claims,
            ttl
        );
        return new Jwt(token, subjet, claims, ttl);
    }



    @Override
    public Optional<Authentication> decode(String token, ClaimsExtractor claimsExtractor) {
        try {

            Algorithm algorithm = Algorithm.HMAC256(config.getSecret());
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(config.getIssuer())
                .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);

            Map<String, Claim> baseClaims = jwt.getClaims();

            Map<String, Object> claims = new HashMap<>();
            for (Map.Entry<String, Claim> entry : baseClaims.entrySet()) {
                claims.put(entry.getKey(), entry.getValue().asString());
            }

            return Optional.of(claimsExtractor.extractInfo(new Jwt(token, jwt.getSubject(), claims)));

        } catch (Exception e) {
            LOG.error(e);
            return Optional.empty();
        }
    }
}
