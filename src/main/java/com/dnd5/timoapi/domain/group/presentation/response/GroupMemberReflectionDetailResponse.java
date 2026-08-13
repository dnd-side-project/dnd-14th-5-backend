package com.dnd5.timoapi.domain.group.presentation.response;

import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionQuestionResponse;
import java.time.LocalDate;

public record GroupMemberReflectionDetailResponse(
        Long id,
        ReflectionQuestionResponse question,
        String content,
        LocalDate reflectedAt,
        long likes,
        long comments
) {
}
