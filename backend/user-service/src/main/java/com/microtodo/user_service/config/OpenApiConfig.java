package com.microtodo.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        final String schemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("User Service API")
                .version("v1")
                .description("Authentication and user management endpoints"))
            .components(new Components()
                .addSecuritySchemes(schemeName, new SecurityScheme()
                    .name(schemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            // Make JWT the default for endpoints (you can still call public ones without it)
            .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }
}