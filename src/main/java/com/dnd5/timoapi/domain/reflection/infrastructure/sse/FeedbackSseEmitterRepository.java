package com.dnd5.timoapi.domain.reflection.infrastructure.sse;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class FeedbackSseEmitterRepository {

    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(Long reflectionId, SseEmitter emitter) {
        emitters.put(reflectionId, emitter);
    }

    public Optional<SseEmitter> get(Long reflectionId) {
        return Optional.ofNullable(emitters.get(reflectionId));
    }

    public void remove(Long reflectionId) {
        emitters.remove(reflectionId);
    }
}
