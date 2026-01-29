package com.dnd5.timoapi.domain.reflection.presentation.response;

import java.time.LocalDate;

public record ReflectionResponse(
        Long id,
        ReflectionQuestionResponse question,
        String content,
        LocalDate reflectedAt
) {

}
