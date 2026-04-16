package com.dnd5.timoapi.global.analytics.event;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

public record ReflectionCreatedEvent(Long userId, Long reflectionId, Long questionId, ZtpiCategory category, int answerLength) {
}
