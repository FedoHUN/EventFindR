package sk.eventfindr.fsa.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    private final RestSecurityExceptionHandler restSecurityExceptionHandler;
    private final String allowedOrigins;

    SecurityConfiguration(RestSecurityExceptionHandler restSecurityExceptionHandler,
                          @Value("${eventfindr.cors.allowed-origins:http://localhost:4200}") String allowedOrigins) {
        this.restSecurityExceptionHandler = restSecurityExceptionHandler;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(this::configureAuthorizationRules)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(restSecurityExceptionHandler)
                        .accessDeniedHandler(restSecurityExceptionHandler))
                .oauth2ResourceServer(oauth2 -> configureOauth2ResourceServer(oauth2, jwtDecoder))
                .build();
    }

    private void configureAuthorizationRules(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                // Pouzivatelske endpointy musia byt pred verejnymi wildcard pravidlami, inak by ich prekryl permitAll matcher.
                .requestMatchers(HttpMethod.GET, "/users/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/events/my-attendances").authenticated()
                .requestMatchers(HttpMethod.GET, "/events/my-performances").authenticated()
                .requestMatchers(HttpMethod.GET, "/events/my-drafts").authenticated()
                .requestMatchers(HttpMethod.GET, "/events/*/attend").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/events/*/attend").authenticated()

                // Verejne read-only endpointy dostupne bez prihlasenia.
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/users/organizers").permitAll()
                .requestMatchers(HttpMethod.GET, "/artists/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/users/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/*/media").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/*/media/*/file").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/*/comments").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/trending").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/*/similar").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/*/attendance-counts").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/users/*/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/posts/*/media/*/file").permitAll()

                // Administratorsku zmenu konfiguracie povolujeme iba roli ADMIN.
                .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/events/*/toggle-featured").hasRole("ADMIN")

                // JWT overi identitu; konkretne DB role a vlastnictvo kontroluje domenova vrstva.
                .requestMatchers(HttpMethod.POST, "/events").authenticated()
                .requestMatchers(HttpMethod.PUT, "/events/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/events/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/events/*/cancel").authenticated()
                .requestMatchers(HttpMethod.POST, "/events/*/restore").authenticated()
                .requestMatchers(HttpMethod.POST, "/events/*/publish").authenticated()
                .requestMatchers(HttpMethod.POST, "/events/*/media").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/events/*/media/*").authenticated()
                .requestMatchers(HttpMethod.PUT, "/events/*/media/reorder").authenticated()
                .requestMatchers(HttpMethod.POST, "/posts").authenticated()
                .requestMatchers(HttpMethod.POST, "/posts/*/media").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/posts/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/posts/*/media/*").authenticated()

                // Ostatne endpointy vyzaduju prihlaseneho pouzivatela.
                .anyRequest().authenticated();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Location", "X-Event-Id"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void configureOauth2ResourceServer(OAuth2ResourceServerConfigurer<HttpSecurity> oauth2, JwtDecoder jwtDecoder) {
        oauth2
                .jwt(jwt -> {
                    jwt.decoder(jwtDecoder);
                    jwt.jwtAuthenticationConverter(JwtConverter::new);
                });
    }
}
