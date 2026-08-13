package com.dnd5.timoapi.domain.user.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.user.domain.model.User;
import com.dnd5.timoapi.domain.user.domain.model.enums.OAuthProvider;
import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String name,
        String email,
        OAuthProvider provider,
        ZtpiCategory category,
        Integer streakDays,
        Integer totalDays,
        LocalDateTime createdAt
) {
    public static AdminUserResponse from(User model) {
        return new AdminUserResponse(
                model.id(),
                model.nickname(),
                model.email(),
                model.provider(),
                model.category(),
                model.streakDays(),
                model.totalDays(),
                model.createdAt()
        );
    }
}
