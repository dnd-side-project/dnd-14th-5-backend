package com.dnd5.timoapi.domain.statistics.application.service;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackRepository;
import com.dnd5.timoapi.domain.statistics.presentation.response.StatisticsCategoryDetailResponse;
import com.dnd5.timoapi.domain.statistics.presentation.response.StatisticsCategoryResponse;
import com.dnd5.timoapi.domain.statistics.presentation.response.StatisticsResponse;
import com.dnd5.timoapi.domain.statistics.presentation.response.StatisticsScoreDetailResponse;
import com.dnd5.timoapi.domain.statistics.presentation.response.StatisticsScoreResponse;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestResultEntity;
import com.dnd5.timoapi.domain.user.domain.model.enums.UserTestRecordStatus;
import com.dnd5.timoapi.domain.statistics.exception.StatisticsErrorCode;
import com.dnd5.timoapi.domain.user.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.user.domain.repository.UserTestResultRepository;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final UserTestRecordRepository userTestRecordRepository;
    private final UserTestResultRepository userTestResultRepository;
    private final ReflectionFeedbackRepository reflectionFeedbackRepository;

    public StatisticsResponse getStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();

        Map<ZtpiCategory, UserTestResultEntity> testResultByCategory = getLatestTestResultByCategory(userId);
        List<ReflectionFeedbackEntity> feedbacks = reflectionFeedbackRepository
                .findCompletedByUserIdOrderByCreatedAt(userId);
        Map<ZtpiCategory, List<ReflectionFeedbackEntity>> feedbacksByCategory = feedbacks.stream()
                .collect(Collectors.groupingBy(ReflectionFeedbackEntity::getCategory));

        List<StatisticsCategoryResponse> categories = Arrays.stream(ZtpiCategory.values())
                .map(category -> {
                    List<StatisticsScoreResponse> dataPoints = new ArrayList<>();

                    UserTestResultEntity testResult = testResultByCategory.get(category);
                    if (testResult != null) {
                        dataPoints.add(new StatisticsScoreResponse(
                                testResult.getScore(),
                                testResult.getCreatedAt(),
                                "TEST"
                        ));
                    }

                    feedbacksByCategory.getOrDefault(category, List.of()).forEach(fb ->
                            dataPoints.add(new StatisticsScoreResponse(
                                    fb.getAfterScore(),
                                    fb.getCreatedAt(),
                                    "REFLECTION"
                            ))
                    );

                    return new StatisticsCategoryResponse(
                            category,
                            category.getCharacter(),
                            category.getPersonality(),
                            category.getIdealScore(),
                            dataPoints
                    );
                })
                .toList();

        return new StatisticsResponse(categories);
    }

    public StatisticsCategoryDetailResponse getCategoryStatistics(ZtpiCategory category) {
        Long userId = SecurityUtil.getCurrentUserId();

        List<StatisticsScoreDetailResponse> dataPoints = new ArrayList<>();

        getLatestTestResultByCategory(userId).entrySet().stream()
                .filter(entry -> entry.getKey() == category)
                .map(Map.Entry::getValue)
                .findFirst()
                .ifPresent(testResult -> dataPoints.add(new StatisticsScoreDetailResponse(
                        testResult.getScore(),
                        testResult.getCreatedAt(),
                        "TEST",
                        null,
                        null
                )));

        reflectionFeedbackRepository
                .findCompletedByUserIdAndCategoryOrderByCreatedAt(userId, category)
                .forEach(fb -> dataPoints.add(new StatisticsScoreDetailResponse(
                        fb.getAfterScore(),
                        fb.getCreatedAt(),
                        "REFLECTION",
                        fb.getChangedScore(),
                        fb.getIsIncreased()
                )));

        return new StatisticsCategoryDetailResponse(
                category,
                category.getCharacter(),
                category.getPersonality(),
                category.getIdealScore(),
                dataPoints
        );
    }

    private Map<ZtpiCategory, UserTestResultEntity> getLatestTestResultByCategory(Long userId) {
        UserTestRecordEntity latestRecord = userTestRecordRepository
                .findTopByUserIdAndStatusOrderByCreatedAtDesc(userId, UserTestRecordStatus.COMPLETED)
                .orElseThrow(() -> new BusinessException(StatisticsErrorCode.STATISTICS_TEST_NOT_COMPLETED));

        List<UserTestResultEntity> results = userTestResultRepository
                .findAllByUserTestRecordIdAndDeletedAtIsNull(latestRecord.getId());

        if (results.isEmpty()) {
            throw new BusinessException(StatisticsErrorCode.STATISTICS_TEST_RESULT_NOT_FOUND);
        }

        return results.stream()
                .collect(Collectors.toMap(UserTestResultEntity::getCategory, r -> r));
    }
}
