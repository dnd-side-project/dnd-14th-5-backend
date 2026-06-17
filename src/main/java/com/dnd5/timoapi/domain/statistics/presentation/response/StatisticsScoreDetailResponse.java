package com.dnd5.timoapi.domain.statistics.presentation.response;

import java.time.LocalDateTime;

public record StatisticsScoreDetailResponse(
        double score,
        LocalDateTime createdAt,
        String type,
        Double changedScore,
        Boolean isIncreased
) {

}
