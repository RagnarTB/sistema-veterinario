package com.veterinaria.configuraciones; // Asegúrate de que el paquete sea el correcto

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Aplica a todas tus rutas de la API
                        .allowedOrigins("http://localhost:4200") // Permite el acceso SOLO a tu Angular
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Permite estos métodos
                        .allowedHeaders("*") // Permite cualquier cabecera (incluyendo el JWT que usaremos luego)
                        .allowCredentials(true); // Necesario para enviar tokens o cookies
            }
        };
    }
}