package com.dnd5.timoapi.domain.user.presentation.response;

import com.dnd5.timoapi.domain.user.domain.model.User;
import com.dnd5.timoapi.domain.user.domain.model.enums.OAuthProvider;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        OAuthProvider provider,
        Boolean isOnboarded,
        Integer streakDays,
        LocalDateTime createdAt
) {
    public static UserResponse from(User model) {
        return new UserResponse(
                model.id(),
                model.nickname(),
                model.email(),
                model.provider(),
                model.isOnboarded(),
                model.streakDays(),
                model.createdAt()
        );
    }
}
