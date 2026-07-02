package com.dnd5.timoapi.global.infrastructure.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(
        boolean enabled,
        Discord discord,
        SwaggerDiff swaggerDiff
) {
    public record Discord(String webhookUrl) {}
    public record SwaggerDiff(String webhookUrl) {}
}
