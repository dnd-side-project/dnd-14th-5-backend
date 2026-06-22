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

                    List<ReflectionFeedbackEntity> categoryFeedbacks = feedbacksByCategory.getOrDefault(category, List.of());
                    categoryFeedbacks.forEach(fb ->
                            dataPoints.add(new StatisticsScoreResponse(
                                    fb.getAfterScore(),
                                    fb.getCreatedAt(),
                                    "REFLECTION"
                            ))
                    );

                    ReflectionFeedbackEntity latestFeedback = categoryFeedbacks.isEmpty()
                            ? null : categoryFeedbacks.get(categoryFeedbacks.size() - 1);
                    ProximityInfo proximity = calculateProximity(latestFeedback, category.getIdealScore());

                    return new StatisticsCategoryResponse(
                            category,
                            category.getCharacter(),
                            category.getPersonality(),
                            category.getIdealScore(),
                            proximity.changedScore(),
                            proximity.proximityRate(),
                            proximity.isCloserToIdeal(),
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

        List<ReflectionFeedbackEntity> feedbacks = reflectionFeedbackRepository
                .findCompletedByUserIdAndCategoryOrderByCreatedAt(userId, category);

        feedbacks.forEach(fb -> dataPoints.add(new StatisticsScoreDetailResponse(
                fb.getAfterScore(),
                fb.getCreatedAt(),
                "REFLECTION",
                fb.getChangedScore(),
                fb.getIsIncreased()
        )));

        ReflectionFeedbackEntity latestFeedback = feedbacks.isEmpty() ? null : feedbacks.get(feedbacks.size() - 1);
        ProximityInfo proximity = calculateProximity(latestFeedback, category.getIdealScore());

        return new StatisticsCategoryDetailResponse(
                category,
                category.getCharacter(),
                category.getPersonality(),
                category.getIdealScore(),
                proximity.changedScore(),
                proximity.proximityRate(),
                proximity.isCloserToIdeal(),
                dataPoints
        );
    }

    private record ProximityInfo(Double changedScore, Double proximityRate, Boolean isCloserToIdeal) {}

    private ProximityInfo calculateProximity(ReflectionFeedbackEntity feedback, double idealScore) {
        if (feedback == null || feedback.getChangedScore() == null || feedback.getBeforeScore() == null || feedback.getAfterScore() == null) {
            return new ProximityInfo(null, null, null);
        }

        double previousDistance = Math.abs(idealScore - feedback.getBeforeScore());
        double currentDistance = Math.abs(idealScore - feedback.getAfterScore());

        if (previousDistance == 0) {
            return new ProximityInfo(feedback.getChangedScore(), null, null);
        }

        double proximityRate = Math.abs(previousDistance - currentDistance) / previousDistance * 100;
        boolean isCloserToIdeal = currentDistance < previousDistance;

        return new ProximityInfo(feedback.getChangedScore(), proximityRate, isCloserToIdeal);
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
