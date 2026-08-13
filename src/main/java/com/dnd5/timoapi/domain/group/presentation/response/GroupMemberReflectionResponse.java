package com.dnd5.timoapi.domain.group.presentation.response;

import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionQuestionResponse;
import java.time.LocalDate;

public record GroupMemberReflectionResponse(
        Long id,
        ReflectionQuestionResponse question,
        String content,
        boolean isPublic,
        LocalDate reflectedAt
) {
}
