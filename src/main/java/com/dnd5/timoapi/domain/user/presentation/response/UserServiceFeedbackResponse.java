package com.dnd5.timoapi.domain.user.presentation.response;

import com.dnd5.timoapi.domain.user.domain.model.UserServiceFeedback;

public record UserServiceFeedbackResponse(
        Long id,
        Long userId,
        Long serviceRating,
        String serviceFeedback
) {
    public static UserServiceFeedbackResponse from(UserServiceFeedback model) {
        return new UserServiceFeedbackResponse(
                model.id(),
                model.userId(),
                model.serviceRating(),
                model.serviceFeedback()
        );
    }
}
