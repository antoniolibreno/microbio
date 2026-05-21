package com.arthurberwanger.microbio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI microbioOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Microbio API")
                .version("v1")
                .description("API REST do sistema Microbio — solicitações de orçamento e análises microbiológicas.")
                .contact(new Contact().name("Equipe Microbio")));
    }
}
