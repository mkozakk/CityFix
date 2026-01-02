package org.example.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/**").permitAll()
                .anyExchange().permitAll()
            .and()
            .csrf().disable()
            .httpBasic().disable()
            .formLogin().disable();

        return http.build();
    }
}

