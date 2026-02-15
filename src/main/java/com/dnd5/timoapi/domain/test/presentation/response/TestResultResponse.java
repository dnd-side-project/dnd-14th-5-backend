package com.dnd5.timoapi.domain.test.presentation.response;

import java.util.List;

public record TestResultResponse(
        TestResultCategoryResponse closestCategory,
        List<TestResultScoreResponse> scores
) {

}
