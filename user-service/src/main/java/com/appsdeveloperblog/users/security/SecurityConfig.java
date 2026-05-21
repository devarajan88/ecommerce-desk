package com.appsdeveloperblog.users.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration aligned with orders-service.
 *
 * JWT validation is handled entirely by the API Gateway (Resource Server).
 * This service trusts the X-User-Name / X-User-Roles headers the Gateway injects
 * after successfully validating the Keycloak token — no local JwtDecoder needed.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()   // login endpoint is public
                .requestMatchers("/actuator/**").permitAll()   // Prometheus / health
                .requestMatchers("/graphiql/**").permitAll()   // GraphiQL browser UI
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(new HeaderAuthenticationFilter(),
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
