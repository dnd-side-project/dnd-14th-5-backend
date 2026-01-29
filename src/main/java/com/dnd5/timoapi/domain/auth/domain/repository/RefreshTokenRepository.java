package com.dnd5.timoapi.domain.auth.domain.repository;

import com.dnd5.timoapi.global.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String PREFIX = "refresh:";

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    public void save(Long userId, String token) {
        redisTemplate.opsForValue().set(
                PREFIX + userId,
                token,
                jwtProperties.getRefreshExpiration(),
                TimeUnit.MILLISECONDS
        );
    }

    public Optional<String> findByUserId(Long userId) {
        String token = redisTemplate.opsForValue().get(PREFIX + userId);
        return Optional.ofNullable(token);
    }

    public void deleteByUserId(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }
}
