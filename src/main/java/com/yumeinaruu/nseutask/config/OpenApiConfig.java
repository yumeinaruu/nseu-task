package com.yumeinaruu.nseutask.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    private static final String API_TITLE = "Task Manager API";
    private static final String API_VERSION = "1.0";
    private static final String BEARER_FORMAT = "JWT";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title(API_TITLE).version(API_VERSION))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat(BEARER_FORMAT)));
    }
}
