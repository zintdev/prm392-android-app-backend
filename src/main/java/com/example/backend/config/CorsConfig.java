package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.*;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOriginsProp;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethodsProp;

    @Value("${app.cors.allowed-headers}")
    private String allowedHeadersProp;

    @Value("${app.cors.exposed-headers:}")
    private String exposedHeadersProp;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOriginsProp.split("\\s*,\\s*"));
        List<String> methods = Arrays.asList(allowedMethodsProp.split("\\s*,\\s*"));
        List<String> headers = Arrays.asList(allowedHeadersProp.split("\\s*,\\s*"));
        List<String> exposed = exposedHeadersProp.isBlank()
                ? List.of()
                : Arrays.asList(exposedHeadersProp.split("\\s*,\\s*"));

        cfg.setAllowedOrigins(origins);
        cfg.setAllowedMethods(methods);
        cfg.setAllowedHeaders(headers);
        cfg.setExposedHeaders(exposed);
        cfg.setAllowCredentials(true); // nếu dùng cookie/JSESSIONID
        cfg.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng cho toàn bộ API
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
