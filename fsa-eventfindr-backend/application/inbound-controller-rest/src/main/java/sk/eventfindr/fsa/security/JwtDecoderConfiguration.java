package sk.eventfindr.fsa.security;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.Map;

@Configuration
class JwtDecoderConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwtDecoderConfiguration.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Bean
    @Profile("!dev & !test")
    JwtDecoder jwtDecoderFromConfiguredIssuer() {
        if (!StringUtils.hasText(issuerUri) || !StringUtils.hasText(jwkSetUri)) {
            throw new IllegalStateException("JWT issuer and JWK set URI must be configured for secure profiles.");
        }
        OAuth2TokenValidator<Jwt> jwtValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        jwtDecoder.setJwtValidator(jwtValidator);
        log.info("JWT signature verification enabled for issuer {}", issuerUri);
        return jwtDecoder;
    }

    @Bean
    @Profile({"dev", "test"})
    JwtDecoder jwtDecoder() {
        log.warn("JWT signature verification is DISABLED. This profile must NOT be used in production.");
        return token -> {
            try {
                JWT nimbusJwt = JWTParser.parse(token);
                String tokenValue = nimbusJwt.serialize();
                Map<String, Object> headers = nimbusJwt.getHeader().toJSONObject();
                Map<String, Object> claims = nimbusJwt.getJWTClaimsSet().getClaims();
                return new Jwt(tokenValue, nimbusJwt.getJWTClaimsSet().getIssueTime().toInstant(), nimbusJwt.getJWTClaimsSet().getExpirationTime().toInstant(), headers, claims);
            } catch (ParseException e) {
                throw new BadJwtException("Invalid JWT token format", e);
            }
        };
    }
}
