package com.dnd5.timoapi.domain.group.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

public record GroupTodayReflectionItem(
        Long userId,
        Long reflectionId,
        String nickname,
        ZtpiCategory userCategory,
        String questionContent,
        ZtpiCategory questionCategory,
        String answerText,
        boolean isPublic,
        Integer streakDays,
        Integer totalDays
) {
}
