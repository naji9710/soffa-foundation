package io.soffa.foundation.commons.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.core.model.Authentication;
import lombok.SneakyThrows;

import java.net.URL;
import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;

public class JwtJwksDecoder implements JwtDecoder {

    private static final Logger LOG = Logger.get(JwtJwksDecoder.class);
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
    public Optional<Authentication> decode(String token, ClaimsExtractor extractor) {
        try {
            JWTClaimsSet claimsSet = jwtProcessor.process(token, null);
            return Optional.of(extractor.extractInfo(new Jwt(token, claimsSet.getSubject(), claimsSet.getClaims())));
        } catch (ParseException | JOSEException | BadJOSEException e) {
            LOG.error(e.getMessage());
            return Optional.empty();
        }
    }


}
