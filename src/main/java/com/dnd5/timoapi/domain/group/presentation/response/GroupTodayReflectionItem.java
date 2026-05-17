package com.dnd5.timoapi.domain.group.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

public record GroupTodayReflectionItem(
        Long userId,
        String nickname,
        String questionContent,
        ZtpiCategory questionCategory,
        String answerText,
        Integer streakDays,
        Integer totalDays
) {
}
