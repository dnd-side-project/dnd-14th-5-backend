package com.dnd5.timoapi.domain.user.presentation.response;

import com.dnd5.timoapi.domain.test.presentation.response.TestResultResponse;
import com.dnd5.timoapi.domain.user.domain.model.UserServiceFeedback;
import com.dnd5.timoapi.domain.user.domain.model.UserTestRecord;

import java.time.LocalDateTime;

public record UserServiceFeedbackDetailResponse(
        Long id,
        Long userId,
        Long serviceRating,
        String serviceFeedback,
        LocalDateTime createdAt
) {
    public static UserServiceFeedbackDetailResponse of(
            UserServiceFeedback model
    ) {
        return new UserServiceFeedbackDetailResponse(
                model.id(),
                model.userId(),
                model.serviceRating(),
                model.serviceFeedback(),
                model.createdAt()
        );
    }
}
