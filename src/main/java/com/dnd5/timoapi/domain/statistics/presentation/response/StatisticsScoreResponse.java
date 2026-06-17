package com.dnd5.timoapi.domain.statistics.presentation.response;

import java.time.LocalDateTime;

public record StatisticsScoreResponse(
        double score,
        LocalDateTime createdAt,
        String type
) {

}
