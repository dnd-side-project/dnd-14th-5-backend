package com.dnd5.timoapi.global.infrastructure.notification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

public record ErrorNotificationData(
        String errorType,
        String message,
        String stackTrace,
        String serverName,
        LocalDateTime timestamp
) {
    private static final int MAX_STACK_TRACE_LINES = 10;

    public static ErrorNotificationData from(Exception e) {
        return new ErrorNotificationData(
                e.getClass().getSimpleName(),
                e.getMessage() != null ? e.getMessage() : "No message",
                extractStackTrace(e),
                System.getenv().getOrDefault("SERVER_NAME", "local"),
                LocalDateTime.now()
        );
    }

    private static String extractStackTrace(Exception e) {
        return Arrays.stream(e.getStackTrace())
                .limit(MAX_STACK_TRACE_LINES)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }
}
