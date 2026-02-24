package com.dnd5.timoapi.domain.reflection.presentation;

import com.dnd5.timoapi.domain.reflection.application.service.ReflectionFeedbackService;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackRepository;
import com.dnd5.timoapi.domain.reflection.infrastructure.sse.FeedbackSseEmitterRepository;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackDetailResponse;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/reflections/{reflectionId}/feedback")
@RequiredArgsConstructor
@Validated
public class ReflectionFeedbackController {

    private final ReflectionFeedbackService reflectionFeedbackService;
    private final ReflectionFeedbackRepository reflectionFeedbackRepository;
    private final FeedbackSseEmitterRepository sseEmitterRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ReflectionFeedbackDetailResponse create(@Positive @PathVariable Long reflectionId) {
        return reflectionFeedbackService.create(reflectionId);
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@Positive @PathVariable Long reflectionId) {
        Optional<ReflectionFeedbackEntity> existing = reflectionFeedbackRepository.findByReflectionId(reflectionId);

        if (existing.isPresent()) {
            FeedbackStatus status = existing.get().getStatus();
            if (status == FeedbackStatus.COMPLETED || status == FeedbackStatus.FAILED) {
                SseEmitter emitter = new SseEmitter(0L);
                try {
                    emitter.send(SseEmitter.event()
                            .name("feedback")
                            .data(ReflectionFeedbackDetailResponse.from(existing.get().toModel())));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("Failed to send immediate SSE event for reflectionId={}", reflectionId, e);
                    emitter.completeWithError(e);
                }
                return emitter;
            }
        }

        SseEmitter emitter = new SseEmitter(60_000L);
        sseEmitterRepository.save(reflectionId, emitter);
        emitter.onCompletion(() -> sseEmitterRepository.remove(reflectionId));
        emitter.onTimeout(() -> sseEmitterRepository.remove(reflectionId));
        emitter.onError(e -> sseEmitterRepository.remove(reflectionId));
        return emitter;
    }
}
