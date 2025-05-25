package ru.m0vt.musick.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI musickOpenAPI() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("Musick API")
                    .description("API для музыкального сервиса Musick")
                    .version("v1.0")
                    .contact(
                        new Contact()
                            .name("Mikhail")
                            .url("https://github.com/mdd375")
                            .email("m0vt@example.com")
                    )
            )
            .servers(
                List.of(
                    new Server()
                        .url("http://localhost:8080")
                        .description("Local server"),
                    new Server()
                        .url("https://api.musick.m0vt.ru")
                        .description("Production server")
                )
            )
            .addSecurityItem(new SecurityRequirement().addList("JWT", Arrays.asList("read", "write")))
            .components(
                new Components()
                    .addSecuritySchemes(
                        "JWT",
                        new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .in(SecurityScheme.In.HEADER)
                            .name("Authorization")
                    )
            );
    }
}
