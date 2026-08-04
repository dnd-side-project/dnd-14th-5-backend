package com.dnd5.timoapi.domain.reflection.infrastructure.cache;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodayQuestionCacheService {

    private static final String KEY_PREFIX = "reflection:question:today:";
    private static final String POOL_KEY_PREFIX = "reflection:question:pool:";

    private final RedisTemplate<String, String> redisTemplate;

    public Long getQuestionId(Long userId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        return value != null ? Long.parseLong(value) : null;
    }

    public void setQuestionId(Long userId, Long questionId) {
        Duration ttl = untilMidnight();
        redisTemplate.opsForValue().set(KEY_PREFIX + userId, String.valueOf(questionId), ttl);
    }

    public void setQuestionPool(Long userId, List<Long> questionIds) {
        String key = POOL_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        if (questionIds.isEmpty()) {
            return;
        }
        redisTemplate.opsForList().rightPushAll(
                key,
                questionIds.stream().map(String::valueOf).toList()
        );
        redisTemplate.expire(key, untilMidnight());
    }

    public Long getNextQuestionId(Long userId) {
        String key = POOL_KEY_PREFIX + userId;
        String current = redisTemplate.opsForList().leftPop(key);
        if (current == null) {
            return null;
        }
        redisTemplate.opsForList().rightPush(key, current);
        String next = redisTemplate.opsForList().index(key, 0);
        return next != null ? Long.parseLong(next) : Long.parseLong(current);
    }

    public void evict(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
        redisTemplate.delete(POOL_KEY_PREFIX + userId);
    }

    public void evictByQuestionId(Long questionId) {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                String cachedQuestionId = redisTemplate.opsForValue().get(key);
                if (cachedQuestionId != null && cachedQuestionId.equals(String.valueOf(questionId))) {
                    redisTemplate.delete(key);
                }
            }
        }

        Set<String> poolKeys = redisTemplate.keys(POOL_KEY_PREFIX + "*");
        if (poolKeys != null) {
            for (String poolKey : poolKeys) {
                Long removed = redisTemplate.opsForList().remove(
                        poolKey, 0, String.valueOf(questionId));
                if (removed != null && removed > 0) {
                    redisTemplate.delete(poolKey);
                }
            }
        }
    }

    private Duration untilMidnight() {
        return Duration.between(
                LocalDateTime.now(),
                LocalDate.now().plusDays(1).atTime(LocalTime.MIDNIGHT)
        );
    }
}
