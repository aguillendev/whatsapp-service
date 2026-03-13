package com.example.whatsapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI whatsappOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WhatsApp Cloud API Service")
                        .description("Servicio de integración con la API de WhatsApp Business de Meta. " +
                                "Permite enviar mensajes de texto y templates, y recibir mensajes vía webhook.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("dev@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Servidor local"),
                        new Server().url("https://tu-dominio.ngrok-free.app").description("Servidor público (ngrok)")
                ));
    }
}
