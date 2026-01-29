package com.dnd5.timoapi.domain.reflection.presentation.response;

import java.time.LocalDate;

public record ReflectionDetailResponse(
        Long id,
        ReflectionQuestionResponse question,
        String content,
        ReflectionFeedbackResponse feedback,
        LocalDate reflectedAt
) {

}
