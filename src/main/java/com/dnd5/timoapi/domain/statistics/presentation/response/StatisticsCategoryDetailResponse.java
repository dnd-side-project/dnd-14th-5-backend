package com.dnd5.timoapi.domain.statistics.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.util.List;

public record StatisticsCategoryDetailResponse(
        ZtpiCategory category,
        String character,
        String personality,
        double idealScore,
        List<StatisticsScoreDetailResponse> dataPoints
) {

}
