package com.dnd5.timoapi.domain.user.domain.model;

import com.dnd5.timoapi.domain.user.domain.model.enums.OAuthProvider;
import com.dnd5.timoapi.domain.user.domain.model.enums.UserRole;

import java.time.LocalDateTime;

public record User(
        Long id,
        String email,
        String nickname,
        String timezone,
        OAuthProvider provider,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static User create(String email, String nickname, String timezone, OAuthProvider provider) {
        return new User(null, email, nickname, timezone, provider, UserRole.USER, null, null);
    }
}
