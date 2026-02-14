package com.dnd5.timoapi.domain.test.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

public record TestResultScoreResponse(
        ZtpiCategory category,
        double score,
        double idealScore
) {

}
