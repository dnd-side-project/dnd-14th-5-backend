package com.dnd5.timoapi.global.swagger.diff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Profile("prod")
@Component
@RequiredArgsConstructor
public class SwaggerDiffEventListener {

    @Value("${server.port:8080}")
    private int serverPort;

    private final SwaggerDiffService swaggerDiffService;
    private final SwaggerDiffDiscordNotifier discordNotifier;
    private final SwaggerDiffGithubIssueCreator githubIssueCreator;

    @Async
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        try {
            String spec = WebClient.create()
                    .get()
                    .uri("http://localhost:" + serverPort + "/v3/api-docs")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (spec == null) return;

            swaggerDiffService.compareAndSave(spec)
                    .filter(SwaggerDiffResult::hasChanges)
                    .ifPresent(result -> {
                        discordNotifier.notify(result);
                        githubIssueCreator.createIssue(result);
                    });

        } catch (Exception e) {
            log.warn("Swagger diff 감지 실패: {}", e.getMessage());
        }
    }
}
