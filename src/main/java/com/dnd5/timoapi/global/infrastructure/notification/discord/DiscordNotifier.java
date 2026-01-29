package com.dnd5.timoapi.global.infrastructure.notification.discord;

import com.dnd5.timoapi.global.infrastructure.notification.ErrorNotificationData;
import com.dnd5.timoapi.global.infrastructure.notification.NotificationProperties;
import com.dnd5.timoapi.global.infrastructure.notification.NotificationType;
import com.dnd5.timoapi.global.infrastructure.notification.Notifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordNotifier implements Notifier {

    private static final int COLOR_ERROR = 16711680;
    private static final int COLOR_WARNING = 16776960;
    private static final int COLOR_INFO = 65280;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WebClient notificationWebClient;
    private final NotificationProperties properties;

    @Async
    @Override
    public void notify(NotificationType type, ErrorNotificationData data) {
        if (!properties.enabled() || properties.discord() == null || properties.discord().webhookUrl().isBlank()) {
            log.debug("Discord notification disabled or webhook URL not configured");
            return;
        }

        Map<String, Object> payload = buildPayload(type, data);

        notificationWebClient.post()
                .uri(properties.discord().webhookUrl())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Failed to send Discord notification: {}", e.getMessage()))
                .subscribe();
    }

    private Map<String, Object> buildPayload(NotificationType type, ErrorNotificationData data) {
        return Map.of(
                "embeds", List.of(Map.of(
                        "title", getTitle(type),
                        "color", getColor(type),
                        "fields", List.of(
                                Map.of("name", "Server", "value", data.serverName(), "inline", true),
                                Map.of("name", "Time", "value", data.timestamp().format(FORMATTER), "inline", true),
                                Map.of("name", "Error Type", "value", data.errorType(), "inline", false),
                                Map.of("name", "Message", "value", truncate(data.message(), 1024), "inline", false),
                                Map.of("name", "Stack Trace", "value", "```" + truncate(data.stackTrace(), 1000) + "```", "inline", false)
                        )
                ))
        );
    }

    private String getTitle(NotificationType type) {
        return switch (type) {
            case ERROR -> "\uD83D\uDEA8 Error Occurred";
            case WARNING -> "\u26A0\uFE0F Warning";
            case INFO -> "\u2139\uFE0F Info";
        };
    }

    private int getColor(NotificationType type) {
        return switch (type) {
            case ERROR -> COLOR_ERROR;
            case WARNING -> COLOR_WARNING;
            case INFO -> COLOR_INFO;
        };
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }
}
