package io.soffa.foundation.commons.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.soffa.foundation.core.model.Authentication;
import io.soffa.foundation.core.model.TenantId;
import io.soffa.foundation.core.model.UserProfile;
import io.soffa.foundation.lang.TextUtil;
import io.soffa.foundation.logging.Logger;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class JwtJwksDecoder implements JwtDecoder {

    private static final Logger LOG = Logger.create(JwtJwksDecoder.class);
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
        } catch (Exception e) {
            LOG.error(e);
            return Optional.empty();
        }
    }

    protected Authentication extractInfo(Jwt jwt) {
        TenantId tenant = jwt.lookupClaim("tenant").map(TenantId::new).orElse(null);

        UserProfile profile = new UserProfile();

        profile.setCity(jwt.lookupClaim("city", "location").orElse(null));
        profile.setCountry(jwt.lookupClaim("country", "countryId").orElse(null));
        profile.setGender(jwt.lookupClaim("gender", "sex", "sexe").orElse(null));
        profile.setEmail(jwt.lookupClaim("email", "mail").orElse(null));
        profile.setPhoneNumber(jwt.lookupClaim("mobile", "mobileNumber", "phoneNumber", "phone").orElse(null));
        profile.setGivenName(jwt.lookupClaim("givenname", "given_name", "firstname", "first_name", "prenom").orElse(null));
        profile.setFamilyName(jwt.lookupClaim("familyname", "family_name", "lastName", "last_name").orElse(null));
        profile.setNickname(jwt.lookupClaim("nickname", "nick_name", "pseudo", "alias").orElse(null));

        Set<String> permissions = new HashSet<>();

        jwt.lookupClaim("permissions", "grants").ifPresent(s -> {
            for(String item : s.split(",")) {
                if (TextUtil.isNotEmpty(item)) {
                    permissions.add(item.toLowerCase());
                }
            }
        });

        return Authentication.builder().
            username(jwt.getSubject()).
            tenantId(tenant).
            application(jwt.lookupClaim("applicationName", "application", "applicationId", "app").orElse(null)).
            profile(profile).
            permissions(permissions).
            build();
    }


}
