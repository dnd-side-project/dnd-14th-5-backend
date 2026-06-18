package com.dnd5.timoapi.domain.statistics.presentation.response;

import java.util.List;

public record StatisticsResponse(
        List<StatisticsCategoryResponse> categories
) {

}
