package com.example.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "User API", version = "1.0",
                 description = "OpenAPI docs for User service"),
    servers = { @io.swagger.v3.oas.annotations.servers.Server(url = "/") }
)
public class SwaggerConfig {}
