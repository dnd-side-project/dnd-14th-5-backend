package com.dnd5.timoapi.domain.user.domain.model;

import java.time.LocalDateTime;

import static com.dnd5.timoapi.global.security.context.SecurityUtil.getCurrentUserId;

public record UserServiceFeedback(
        Long id,
        Long userId,
        Long serviceRating,
        String serviceFeedback,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserServiceFeedback create(
            Long userId,
            Long serviceRating,
            String serviceFeedback
    ) {
        return new UserServiceFeedback(
                null,
                userId,
                serviceRating,
                serviceFeedback,
                null,
                null
        );
    }

}
