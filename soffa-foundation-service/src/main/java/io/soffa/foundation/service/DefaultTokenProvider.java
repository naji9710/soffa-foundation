package io.soffa.foundation.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
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
import io.soffa.foundation.commons.IOUtil;
import io.soffa.foundation.commons.Logger;
import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.exceptions.InvalidTokenException;
import io.soffa.foundation.exceptions.NotImplementedException;
import io.soffa.foundation.exceptions.UnauthorizedException;
import io.soffa.foundation.model.Authentication;
import io.soffa.foundation.model.TenantId;
import io.soffa.foundation.model.UserProfile;
import io.soffa.foundation.tokens.*;
import lombok.Data;
import lombok.SneakyThrows;

import java.net.URL;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;

@Data
public class DefaultTokenProvider implements TokenProvider, ClaimsExtractor {

    private static final Logger LOG = Logger.get(DefaultTokenProvider.class);
    private TokenConfig config;
    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private String privateJwks;

    public DefaultTokenProvider(TokenConfig config) {
        this.config = config;
        configureJwksProcessor();
    }

    @Override
    public String getServiceToken() {
        return config.getServiceToken();
    }

    @Override
    public Token create(TokenType type, String subjet, Map<String, Object> claims) {
        return create(type, subjet, claims, config.getDefaultTtl());
    }

    @Override
    public Token create(TokenType type, String subjet, Map<String, Object> claims, int ttl) {
        String token;
        if (type == TokenType.JWT) {
            if (privateJwks != null) {
                token = TokenUtil.fromJwks(
                    privateJwks,
                    config.getIssuer(),
                    subjet,
                    claims,
                    Duration.ofSeconds(ttl)
                );
            } else {
                token = TokenUtil.createJwt(
                    config.getIssuer(),
                    config.getSecret(),
                    subjet,
                    claims,
                    ttl
                );
            }
        } else {
            throw new NotImplementedException("Token type not supported yet: %s", type.name());
        }
        return new Token(token, subjet, claims, ttl);
    }


    @Override
    public Authentication extractInfo(Token token) {
        TenantId tenant = token.lookupClaim("tenant", "tenantId").map(TenantId::new).orElse(null);

        UserProfile profile = new UserProfile();

        profile.setCity(token.lookupClaim("city", "location").orElse(null));
        profile.setCountry(token.lookupClaim("country", "countryId").orElse(null));
        profile.setGender(token.lookupClaim("gender", "sex", "sexe").orElse(null));
        profile.setEmail(token.lookupClaim("email", "mail").orElse(null));
        profile.setPhoneNumber(token.lookupClaim("mobile", "mobileNumber", "phoneNumber", "phone").orElse(null));
        profile.setGivenName(token.lookupClaim("givenname", "given_name", "firstname", "first_name", "prenom").orElse(null));
        profile.setFamilyName(token.lookupClaim("familyname", "family_name", "lastName", "last_name").orElse(null));
        profile.setNickname(token.lookupClaim("nickname", "nick_name", "pseudo", "alias").orElse(null));

        Set<String> permissions = new HashSet<>();
        Set<String> roles = new HashSet<>();

        token.lookupClaim("permissions", "grants").ifPresent(s -> {
            for (String item : s.split(",")) {
                if (TextUtil.isNotEmpty(item)) {
                    permissions.add(item.trim().toLowerCase());
                }
            }
        });

        token.lookupClaim("roles").ifPresent(s -> {
            for (String item : s.split(",")) {
                if (TextUtil.isNotEmpty(item)) {
                    roles.add(item.trim().toLowerCase());
                }
            }
        });

        return Authentication.builder().
            claims(token.getClaims()).
            liveMode(Objects.equals(token.lookupClaim("liveMode").orElse(null), "true")).
            username(token.getSubject()).
            tenantId(tenant).
            application(token.lookupClaim("applicationName", "application", "applicationId", "app").orElse(null)).
            profile(profile).
            roles(roles).
            permissions(permissions).
            build();
    }

    @SneakyThrows
    private void configureJwksProcessor() {
        if (config.getPrivateJwks() != null) {
            privateJwks = IOUtil.getResourceAsString(config.getPrivateJwks());
        }
        if (config.getPublicJwks() != null) {
            JWKSet source;
            if (config.getPublicJwks().startsWith("http")) {
                source = JWKSet.load(new URL(config.getPublicJwks()));
            } else {
                source = JWKSet.load(Objects.requireNonNull(DefaultTokenProvider.class.getResourceAsStream(config.getPublicJwks())));
            }
            JWKSource<SecurityContext> keySource = new ImmutableJWKSet<>(source);
            jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource));
        }
    }

    @Override
    public Authentication decode(String token) {
        return decode(token, this);
    }

    @Override
    public Authentication decode(String token, ClaimsExtractor extractor) {
        if (jwtProcessor != null) {
            return decodejwtWithJwks(token, extractor);
        } else {
            return decodeJwtWithSecret(token, extractor);
        }
    }

    public Authentication decodejwtWithJwks(String token, ClaimsExtractor extractor) {
        try {
            JWTClaimsSet claimsSet = jwtProcessor.process(token, null);
            return extractor.extractInfo(new Token(token, claimsSet.getSubject(), claimsSet.getClaims()));
        } catch (ParseException | JOSEException | BadJOSEException e) {
            throw new InvalidTokenException(e.getMessage(), e);
        }
    }

    public Authentication decodeJwtWithSecret(String token, ClaimsExtractor claimsExtractor) {
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

            return claimsExtractor.extractInfo(new Token(token, jwt.getSubject(), claims));

        } catch (Exception e) {
            throw new UnauthorizedException(e.getMessage(), e);
        }
    }
}
