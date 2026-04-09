package com.veterinaria.configuraciones;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private final String SCHEME_NAME = "Bearer Authentication";
    private final String SCHEME = "bearer";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API del Sistema Veterinario")
                        .version("1.0")
                        .description("Documentación de los Endpoints de la API, requiere autenticación JWT para rutas protegidas."))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(SCHEME)
                                        .bearerFormat("JWT")
                                        .description("Ingresa tu token (sin la palabra Bearer)")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
    }
}
