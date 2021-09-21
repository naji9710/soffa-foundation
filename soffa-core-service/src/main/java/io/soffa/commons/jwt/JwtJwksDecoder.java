package io.soffa.commons.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.soffa.commons.logging.Logger;
import io.soffa.service.core.model.Authentication;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public class JwtJwksDecoder implements JwtDecoder {

    private static final Logger logger = Logger.create(JwtJwksDecoder.class);
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    @SneakyThrows
    public JwtJwksDecoder(String url) {
        JWKSet source;
        if (url.startsWith("http")) {
            source = JWKSet.load(new URL(url));
        } else {
            source = JWKSet.load(Objects.requireNonNull(JwtJwksDecoder.class.getResourceAsStream(url)));
        }
        JWKSource<SecurityContext> keySource = new ImmutableJWKSet<>(source);
        jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource));
    }

    @Override
    public Optional<Authentication> decode(String token) {
        try {
            JWTClaimsSet claimsSet = jwtProcessor.process(token, null);
            return Optional.of(extractInfo(new Jwt(token, claimsSet.getSubject(), claimsSet.getClaims())));
        } catch (Exception e){
            logger.error(e);
            return Optional.empty();
        }
    }

    protected Authentication extractInfo(Jwt jwt) {
        return Authentication.builder().username(jwt.getSubject()).build();
    }
}
