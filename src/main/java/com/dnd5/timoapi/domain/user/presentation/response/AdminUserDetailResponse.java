package com.dnd5.timoapi.domain.user.presentation.response;

import com.dnd5.timoapi.domain.customization.presentation.response.EquippedCustomizationResponse;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.user.domain.model.User;
import com.dnd5.timoapi.domain.user.domain.model.enums.OAuthProvider;
import com.dnd5.timoapi.domain.user.domain.model.enums.UserRole;
import java.time.LocalDateTime;
import java.util.List;

public record AdminUserDetailResponse(
        Long id,
        String name,
        String email,
        OAuthProvider provider,
        UserRole role,
        ZtpiCategory category,
        Boolean isOnboarded,
        Integer streakDays,
        Integer totalDays,
        LocalDateTime createdAt,
        List<EquippedCustomizationResponse> equippedCustomizations
) {
    public static AdminUserDetailResponse from(User model, List<EquippedCustomizationResponse> equippedCustomizations) {
        return new AdminUserDetailResponse(
                model.id(),
                model.nickname(),
                model.email(),
                model.provider(),
                model.role(),
                model.category(),
                model.isOnboarded(),
                model.streakDays(),
                model.totalDays(),
                model.createdAt(),
                equippedCustomizations
        );
    }
}
