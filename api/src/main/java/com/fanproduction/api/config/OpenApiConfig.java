package com.fanproduction.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI / Swagger UI.
 * <p>
 * Минимальная версия:
 * <ul>
 *   <li>Общая информация о проекте (название, версия, описание).</li>
 *   <li>JWT Bearer security scheme — чтобы в Swagger UI появилась
 *       кнопка «Authorize» и можно было тестировать защищённые эндпоинты.</li>
 * </ul>
 * <p>
 * Аннотации {@code @Operation} / {@code @Tag} на контроллерах намеренно
 * не используются — для одного разработчика достаточно автогенерируемого
 * списка эндпоинтов. Полная документизация — US13.3.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI fanProductionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fan Production Manager API")
                        .version("0.0.1-SNAPSHOT")
                        .description("""
                                REST API для системы управления производством вентиляторов.
                                
                                **Авторизация:** JWT Bearer. Получите токен через
                                `POST /api/auth/login`, нажмите кнопку **Authorize**
                                и введите его (без префикса `Bearer `).
                                """)
                        .contact(new Contact()
                                .name("Fan Production Manager")
                                .url("https://github.com/"))
                        .license(new License()
                                .name("Internal Use Only")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT-токен из POST /api/auth/login")));
    }
}
