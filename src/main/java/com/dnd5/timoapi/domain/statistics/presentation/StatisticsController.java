package com.dnd5.timoapi.domain.statistics.presentation;

import com.dnd5.timoapi.domain.statistics.application.service.StatisticsService;
import com.dnd5.timoapi.domain.statistics.presentation.response.StatisticsCategoryDetailResponse;
import com.dnd5.timoapi.domain.statistics.presentation.response.StatisticsResponse;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "전체 통계 조회")
    @GetMapping
    public StatisticsResponse getStatistics() {
        return statisticsService.getStatistics();
    }

    @Operation(summary = "카테고리별 통계 조회")
    @GetMapping("/{category}")
    public StatisticsCategoryDetailResponse getCategoryStatistics(@PathVariable ZtpiCategory category) {
        return statisticsService.getCategoryStatistics(category);
    }
}
