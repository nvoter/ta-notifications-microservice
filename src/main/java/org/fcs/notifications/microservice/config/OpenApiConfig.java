package org.fcs.notifications.microservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "Notifications Microservice API",
                version = "v1",
                description = "API для получения уведомлений пользователя и управления их статусом прочтения"
        )
)
@SecurityScheme(
        name = "userIdHeader",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "userId",
        description = "Идентификатор пользователя, для которого выполняется операция"
)
public class OpenApiConfig {
}
