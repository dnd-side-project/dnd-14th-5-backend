package com.dnd5.timoapi.domain.statistics.presentation;

import com.dnd5.timoapi.domain.statistics.application.service.StatisticsService;
import com.dnd5.timoapi.domain.statistics.presentation.response.StatisticsCategoryDetailResponse;
import com.dnd5.timoapi.domain.statistics.presentation.response.StatisticsResponse;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
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

    @GetMapping
    public StatisticsResponse getStatistics() {
        return statisticsService.getStatistics();
    }

    @GetMapping("/{category}")
    public StatisticsCategoryDetailResponse getCategoryStatistics(@PathVariable ZtpiCategory category) {
        return statisticsService.getCategoryStatistics(category);
    }
}
