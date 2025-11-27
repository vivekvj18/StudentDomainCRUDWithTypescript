package com.domaincrud.domaincrud.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Student Domain CRUD API",
                version = "1.0",
                description = "IIITB ESD Mini Project - Domains & Students"
        ),
        security = {
                @SecurityRequirement(name = "basicAuth")
        }
)
@SecurityScheme(
        name = "basicAuth",          // is naam se Swagger ko pata chalega
        type = SecuritySchemeType.HTTP,
        scheme = "basic"             // HTTP Basic Auth
)
public class OpenApiConfig {
    // yaha kuch bhi logic nahi chahiye, sirf annotations kaam karenge
}
