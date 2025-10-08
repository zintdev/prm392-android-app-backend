package com.example.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "PRM Backend API", version = "1.0",
                 description = "OpenAPI documentation for PRM Backend service"),
    servers = { @io.swagger.v3.oas.annotations.servers.Server(url = "/") },
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecuritySchemes({
    @SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\""
    )
})
public class SwaggerConfig {}
