package com.dnd5.timoapi.global.analytics.listener;

import com.dnd5.timoapi.global.analytics.domain.entity.AnalyticsEventEntity;
import com.dnd5.timoapi.global.analytics.domain.repository.AnalyticsEventRepository;
import com.dnd5.timoapi.global.analytics.event.FeedbackReceivedEvent;
import com.dnd5.timoapi.global.analytics.event.NotificationSentEvent;
import com.dnd5.timoapi.global.analytics.event.ReflectionCreatedEvent;
import com.dnd5.timoapi.global.analytics.event.TestCompletedEvent;
import com.dnd5.timoapi.global.analytics.event.TestStartedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventListener {

    private final AnalyticsEventRepository analyticsEventRepository;
    private final ObjectMapper objectMapper;

    @Async("analyticsTaskExecutor")
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TestStartedEvent e) {
        save(e.userId(), "test_started", Map.of(
                "testRecordId", e.testRecordId()
        ));
    }

    @Async("analyticsTaskExecutor")
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TestCompletedEvent e) {
        save(e.userId(), "test_completed", Map.of(
                "testRecordId", e.testRecordId(),
                "scores", e.scores()
        ));
    }

    @Async("analyticsTaskExecutor")
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(ReflectionCreatedEvent e) {
        save(e.userId(), "reflection_created", Map.of(
                "reflectionId", e.reflectionId(),
                "questionId", e.questionId(),
                "category", e.category().name(),
                "answerLength", e.answerLength()
        ));
    }

    @Async("analyticsTaskExecutor")
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(FeedbackReceivedEvent e) {
        save(e.userId(), "feedback_received", Map.of(
                "reflectionId", e.reflectionId(),
                "score", e.score()
        ));
    }

    @Async("analyticsTaskExecutor")
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(NotificationSentEvent e) {
        save(e.userId(), "notification_sent", Map.of(
                "tokenCount", e.tokenCount()
        ));
    }

    private void save(Long userId, String eventName, Map<String, Object> properties) {
        try {
            String json = objectMapper.writeValueAsString(properties);
            analyticsEventRepository.save(new AnalyticsEventEntity(userId, eventName, json));
        } catch (JsonProcessingException ex) {
            log.error("analytics_save_failed eventName={}", eventName, ex);
        }
    }
}
