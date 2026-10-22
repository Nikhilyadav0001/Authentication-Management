package com.auth.authbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        info = @Info(
                title = "Auth application built by Nikhil",
                description = "A good auth application developed by me",
                contact = @Contact(
                        name = "Nikhil",
                        url = "https://authapp.com",
                        email = "support@suggestion.com"
                ),
                version = "1.0",
                summary = "This app is very useful and use it where auth is needed"
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
public class APIDocConfig {
}
