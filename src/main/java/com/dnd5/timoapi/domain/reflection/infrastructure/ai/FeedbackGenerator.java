package com.dnd5.timoapi.domain.reflection.infrastructure.ai;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

public interface FeedbackGenerator {

    FeedbackResult execute(ZtpiCategory category, String userReflection);
}
