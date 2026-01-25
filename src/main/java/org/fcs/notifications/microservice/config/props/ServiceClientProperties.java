package org.fcs.notifications.microservice.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clients")
public record ServiceClientProperties(
        ClientProperties users,
        ClientProperties disciplines
) {
    public record ClientProperties(String baseUrl) {
    }
}
