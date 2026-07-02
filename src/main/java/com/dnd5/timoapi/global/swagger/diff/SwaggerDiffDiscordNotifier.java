package com.dnd5.timoapi.global.swagger.diff;

import com.dnd5.timoapi.global.infrastructure.notification.NotificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SwaggerDiffDiscordNotifier {

    private static final int COLOR_BLUE = 3447003;
    private static final int MAX_DETAIL_PER_SECTION = 3;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WebClient notificationWebClient;
    private final NotificationProperties properties;

    public void notify(SwaggerDiffResult result) {
        if (!properties.enabled() || properties.swaggerDiff() == null || properties.swaggerDiff().webhookUrl().isBlank()) {
            log.warn("Swagger diff Discord 알림 비활성화 또는 웹훅 URL 미설정 (enabled={}, swaggerDiff={})",
                    properties.enabled(), properties.swaggerDiff());
            return;
        }

        log.info("Swagger diff Discord 알림 전송 시작 (추가={}, 삭제={}, 변경={})",
                result.added().size(), result.removed().size(), result.changed().size());

        if (!result.added().isEmpty()) {
            sendPayload(buildPayload("✅", "추가", result.added()));
        }
        if (!result.removed().isEmpty()) {
            sendPayload(buildPayload("❌", "삭제", result.removed()));
        }
        if (!result.changed().isEmpty()) {
            sendPayload(buildPayload("✏️", "변경", result.changed()));
        }
    }

    private void sendPayload(Map<String, Object> payload) {
        notificationWebClient.post()
                .uri(properties.swaggerDiff().webhookUrl())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Swagger diff Discord 알림 전송 실패: {}", e.getMessage()))
                .subscribe();
    }

    private Map<String, Object> buildPayload(String emoji, String label,
                                              List<SwaggerDiffResult.EndpointDetail> endpoints) {
        List<Map<String, Object>> fields = new ArrayList<>();
        addSectionFields(fields, emoji, label, endpoints);
        fields.add(Map.of("name", "​", "value", "​", "inline", false));

        return Map.of(
                "embeds", List.of(Map.of(
                        "title", "🔔 Timo API 변경 알림 🔔",
                        "color", COLOR_BLUE,
                        "fields", fields,
                        "footer", Map.of("text", "TIMO API · " + LocalDateTime.now().format(FORMATTER))
                ))
        );
    }

    private void addSectionFields(List<Map<String, Object>> fields, String emoji, String label,
                                   List<SwaggerDiffResult.EndpointDetail> endpoints) {
        if (endpoints.isEmpty()) return;

        int total = endpoints.size();
        List<SwaggerDiffResult.EndpointDetail> toShow = endpoints.stream()
                .limit(MAX_DETAIL_PER_SECTION)
                .toList();

        fields.add(Map.of(
                "name", emoji + " 엔드포인트 " + total + "개가 " + label + "되었습니다!",
                "value", "​",
                "inline", false
        ));

        boolean showExamples = !"삭제".equals(label);
        for (int i = 0; i < toShow.size(); i++) {
            if (i > 0) {
                fields.add(Map.of("name", "​", "value", "​", "inline", false));
            }
            fields.add(detailField(toShow.get(i), i + 1, showExamples));
        }

        if (total > MAX_DETAIL_PER_SECTION) {
            fields.add(Map.of(
                    "name", "외 " + (total - MAX_DETAIL_PER_SECTION) + "개",
                    "value", "​",
                    "inline", false
            ));
        }
    }

    private Map<String, Object> detailField(SwaggerDiffResult.EndpointDetail detail, int index, boolean showExamples) {
        StringBuilder value = new StringBuilder();
        if (detail.summary() != null) {
            value.append(detail.summary());
        }
        if (!showExamples) {
            return Map.of(
                    "name", index + ". " + detail.key(),
                    "value", value.isEmpty() ? "​" : value.toString(),
                    "inline", false
            );
        }
        if (!value.isEmpty()) value.append("\n\n");

        boolean requestChanged = detail.previousRequestExample() != null;
        boolean responseChanged = detail.previousResponseExample() != null;

        value.append("**Request**").append(requestChanged ? " ✏️" : "").append("\n");
        if (requestChanged) {
            value.append("변경 전\n```json\n").append(truncate(detail.previousRequestExample(), 150)).append("\n```\n");
            if (detail.requestExample() != null) {
                value.append("변경 후\n```json\n").append(truncate(detail.requestExample(), 150)).append("\n```\n");
            } else {
                value.append("변경 후\n_해당 사항 없음_\n");
            }
        } else if (detail.requestExample() != null) {
            value.append("```json\n").append(truncate(detail.requestExample(), 400)).append("\n```\n");
        } else {
            value.append("_해당 사항 없음_\n");
        }

        value.append("**Response**").append(responseChanged ? " ✏️" : "").append("\n");
        if (responseChanged) {
            value.append("변경 전\n```json\n").append(truncate(detail.previousResponseExample(), 150)).append("\n```\n");
            if (detail.responseExample() != null) {
                value.append("변경 후\n```json\n").append(truncate(detail.responseExample(), 150)).append("\n```");
            } else {
                value.append("변경 후\n_해당 사항 없음_");
            }
        } else if (detail.responseExample() != null) {
            value.append("```json\n").append(truncate(detail.responseExample(), 400)).append("\n```");
        } else {
            value.append("_해당 사항 없음_");
        }

        return Map.of(
                "name", index + ". " + detail.key(),
                "value", value.toString().trim(),
                "inline", false
        );
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }
}
