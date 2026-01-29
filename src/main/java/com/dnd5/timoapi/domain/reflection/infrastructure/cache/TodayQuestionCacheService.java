package com.dnd5.timoapi.domain.reflection.infrastructure.cache;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodayQuestionCacheService {

    private static final String KEY_PREFIX = "reflection:question:today:";

    private final RedisTemplate<String, String> redisTemplate;

    public Long getQuestionId(Long userId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        return value != null ? Long.parseLong(value) : null;
    }

    public void setQuestionId(Long userId, Long questionId) {
        Duration ttl = Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atTime(LocalTime.MIDNIGHT));
        redisTemplate.opsForValue().set(KEY_PREFIX + userId, String.valueOf(questionId), ttl);
    }

    public void evict(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}
