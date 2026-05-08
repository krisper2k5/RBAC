package com.taxi.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable) // Отключаем CORS для curl-тестов
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            log.warn("🔐 AuthEntryPoint: {} {} | Error: {}",
                                    req.getMethod(), req.getRequestURI(), e.getMessage());
                            res.setContentType("application/json");
                            res.setStatus(401);
                            res.getWriter().write("{\"error\": \"Unauthorized or invalid/expired token\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            log.warn("🔐 AccessDenied: {} {} | Error: {}",
                                    req.getMethod(), req.getRequestURI(), e.getMessage());
                            res.setContentType("application/json");
                            res.setStatus(403);
                            res.getWriter().write("{\"error\": \"Forbidden\"}");
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("✅ SecurityConfig applied");
        return http.build();
    }
}