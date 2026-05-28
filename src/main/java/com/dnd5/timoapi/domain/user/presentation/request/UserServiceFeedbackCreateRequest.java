package com.dnd5.timoapi.domain.user.presentation.request;

import com.dnd5.timoapi.domain.user.domain.model.UserServiceFeedback;
import jakarta.validation.constraints.*;

public record UserServiceFeedbackCreateRequest(
        @Positive @NotNull
        @Min(value=0) @Max(value=5)
        Long serviceRating,
        @NotEmpty
        String serviceFeedback
) {
    public UserServiceFeedback toModel() {
        return UserServiceFeedback.create(serviceRating, serviceFeedback);
    }
}
