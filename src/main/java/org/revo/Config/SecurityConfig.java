package org.revo.Config;

import net.minidev.json.JSONArray;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Collection;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

/**
 * Created by ashraf on 18/04/17.
 */
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                .matchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(new JwtAuthenticationConverter() {
                    protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
                        return createAuthorityList(((JSONArray) jwt.getClaims().get("authorities")).stream().toArray(String[]::new));
                    }
                }))
                .and().and().build();
    }
}
