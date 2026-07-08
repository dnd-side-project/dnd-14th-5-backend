package com.dnd5.timoapi.global.swagger.diff;

import com.dnd5.timoapi.global.infrastructure.gemini.GeminiClient;
import com.dnd5.timoapi.global.infrastructure.notification.NotificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SwaggerDiffGithubIssueCreator {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String SUMMARY_SYSTEM_PROMPT = """
            당신은 백엔드 API 변경 사항을 프론트엔드 개발자에게 전달하는 역할입니다.
            변경된 엔드포인트 목록을 보고 핵심 내용을 개괄식으로 1~4줄로 요약하세요.
            반드시 아래 형식을 지켜 plain text로만 응답하세요. JSON, 마크다운 코드블록, 따옴표 등 절대 사용 금지.

            형식:
            - 첫 번째 변경 요약
            - 두 번째 변경 요약

            마침표 없이 간결하게, 코드나 기술 용어(HTTP 메서드, 경로 등)는 그대로 사용하세요.
            """;

    private final WebClient notificationWebClient;
    private final GeminiClient geminiClient;
    private final NotificationProperties properties;

    public void createIssue(SwaggerDiffResult result) {
        NotificationProperties.GithubIssue config = properties.githubIssue();
        if (config == null || config.token() == null || config.token().isBlank()) {
            log.warn("GitHub 이슈 생성 비활성화: token 미설정");
            return;
        }

        String title = buildTitle(result);
        String body = buildBody(result);

        log.info("GitHub 이슈 미리보기\n제목: {}\n내용:\n{}", title, body);
        notificationWebClient
                .post()
                .uri(GITHUB_API_BASE + "/repos/{owner}/{repo}/issues", config.owner(), config.repo())
                .header("Authorization", "Bearer " + config.token())
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .bodyValue(Map.of("title", title, "body", body))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("GitHub 이슈 생성 완료 ({})", title))
                .doOnError(e -> log.error("GitHub 이슈 생성 실패: {}", e.getMessage()))
                .subscribe();
    }

    private String buildTitle(SwaggerDiffResult result) {
        return "[API 변경] %s - 추가 %d / 삭제 %d / 변경 %d".formatted(
                LocalDate.now(),
                result.added().size(),
                result.removed().size(),
                result.changed().size()
        );
    }

    private String buildBody(SwaggerDiffResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Timo API 변경 사항\n\n");
        sb.append("> 서버 배포 시 Swagger 스펙 자동 감지\n\n");

        String summary = generateSummary(result);
        if (summary != null && !summary.isBlank()) {
            sb.append(summary.strip()).append("\n\n");
        }

        sb.append("\n");

        if (!result.added().isEmpty()) {
            sb.append("### ✅ 추가된 엔드포인트 (").append(result.added().size()).append("개)\n\n");
            appendEndpointDetails(sb, result.added(), true);
        }

        if (!result.removed().isEmpty()) {
            sb.append("### ❌ 삭제된 엔드포인트 (").append(result.removed().size()).append("개)\n\n");
            appendEndpointDetails(sb, result.removed(), false);
        }

        if (!result.changed().isEmpty()) {
            sb.append("### ✏️ 변경된 엔드포인트 (").append(result.changed().size()).append("개)\n\n");
            appendChangedDetails(sb, result.changed());
        }

        return sb.toString();
    }

    private String generateSummary(SwaggerDiffResult result) {
        try {
            String userPrompt = buildSummaryPrompt(result);
            return geminiClient.generatePlainText(SUMMARY_SYSTEM_PROMPT, userPrompt);
        } catch (Exception e) {
            log.warn("Swagger diff 요약 생성 실패, 요약 없이 진행: {}", e.getMessage());
            return null;
        }
    }

    private String buildSummaryPrompt(SwaggerDiffResult result) {
        StringBuilder sb = new StringBuilder();

        if (!result.added().isEmpty()) {
            sb.append("[추가]\n");
            result.added().forEach(d -> sb.append("- ").append(d.key())
                    .append(d.summary() != null ? " (" + d.summary() + ")" : "").append("\n"));
            sb.append("\n");
        }
        if (!result.removed().isEmpty()) {
            sb.append("[삭제]\n");
            result.removed().forEach(d -> sb.append("- ").append(d.key())
                    .append(d.summary() != null ? " (" + d.summary() + ")" : "").append("\n"));
            sb.append("\n");
        }
        if (!result.changed().isEmpty()) {
            sb.append("[변경]\n");
            result.changed().forEach(d -> sb.append("- ").append(d.key())
                    .append(d.summary() != null ? " (" + d.summary() + ")" : "").append("\n"));
        }

        return sb.toString();
    }

    private void appendEndpointDetails(StringBuilder sb, List<SwaggerDiffResult.EndpointDetail> endpoints, boolean showExamples) {
        for (SwaggerDiffResult.EndpointDetail detail : endpoints) {
            sb.append("- **").append(detail.key()).append("**");
            if (detail.summary() != null) {
                sb.append(" — ").append(detail.summary());
            }
            sb.append("\n");

            if (showExamples) {
                if (detail.requestExample() != null) {
                    sb.append("\n<details><summary>Request</summary>\n\n```json\n")
                            .append(detail.requestExample())
                            .append("\n```\n\n</details>\n");
                }
                if (detail.responseExample() != null) {
                    sb.append("\n<details><summary>Response</summary>\n\n```json\n")
                            .append(detail.responseExample())
                            .append("\n```\n\n</details>\n");
                }
            }
            sb.append("\n");
        }
    }

    private void appendChangedDetails(StringBuilder sb, List<SwaggerDiffResult.EndpointDetail> endpoints) {
        for (SwaggerDiffResult.EndpointDetail detail : endpoints) {
            sb.append("- **").append(detail.key()).append("**");
            if (detail.summary() != null) {
                sb.append(" — ").append(detail.summary());
            }
            sb.append("\n");

            boolean requestChanged = detail.previousRequestExample() != null;
            boolean responseChanged = detail.previousResponseExample() != null;

            if (requestChanged) {
                sb.append("\n<details><summary>Request 변경</summary>\n\n");
                sb.append("**변경 전**\n```json\n").append(detail.previousRequestExample()).append("\n```\n\n");
                if (detail.requestExample() != null) {
                    sb.append("**변경 후**\n```json\n").append(detail.requestExample()).append("\n```\n\n");
                }
                sb.append("</details>\n");
            } else if (detail.requestExample() != null) {
                sb.append("\n<details><summary>Request</summary>\n\n```json\n")
                        .append(detail.requestExample())
                        .append("\n```\n\n</details>\n");
            }

            if (responseChanged) {
                sb.append("\n<details><summary>Response 변경</summary>\n\n");
                sb.append("**변경 전**\n```json\n").append(detail.previousResponseExample()).append("\n```\n\n");
                if (detail.responseExample() != null) {
                    sb.append("**변경 후**\n```json\n").append(detail.responseExample()).append("\n```\n\n");
                }
                sb.append("</details>\n");
            } else if (detail.responseExample() != null) {
                sb.append("\n<details><summary>Response</summary>\n\n```json\n")
                        .append(detail.responseExample())
                        .append("\n```\n\n</details>\n");
            }

            sb.append("\n");
        }
    }
}
