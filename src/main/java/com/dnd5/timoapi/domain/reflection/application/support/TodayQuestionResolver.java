package com.dnd5.timoapi.domain.reflection.application.support;

import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.UserReflectionQuestionOrderRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.infrastructure.cache.TodayQuestionCacheService;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestResultEntity;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestResultRepository;
import com.dnd5.timoapi.domain.test.exception.UserTestRecordErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TodayQuestionResolver {

    private final TodayQuestionCacheService cacheService;
    private final UserReflectionQuestionOrderRepository userReflectionQuestionOrderRepository;
    private final ReflectionQuestionRepository reflectionQuestionRepository;
    private final UserTestRecordRepository userTestRecordRepository;
    private final UserTestResultRepository userTestResultRepository;

    public Long resolve(Long userId) {
        Long cached = cacheService.getQuestionId(userId);
        if (cached != null) {
            return cached;
        }

        ZtpiCategory category = resolveTodayCategory(userId);
        Long sequence = resolveTodaySequence(userId, category);
        Long questionId = findQuestionId(sequence, category);

        cacheService.setQuestionId(userId, questionId);
        return questionId;
    }

    public void cacheQuestionId(Long userId, Long questionId) {
        cacheService.setQuestionId(userId, questionId);
    }

    public ZtpiCategory resolveTodayCategory(Long userId) {
        UserTestRecordEntity latestRecord = userTestRecordRepository
                .findTopByUserIdAndStatusOrderByCreatedAtDesc(userId, TestRecordStatus.COMPLETED)
                .orElseThrow(() -> new BusinessException(
                        UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND));

        List<UserTestResultEntity> results =
                userTestResultRepository.findByUserTestRecordId(latestRecord.getId());

        Map<ZtpiCategory, Double> scoreMap = results.stream()
                .collect(Collectors.toMap(
                        UserTestResultEntity::getCategory,
                        UserTestResultEntity::getScore));

        return selectWeightedRandom(scoreMap);
    }

    public Long resolveTodaySequence(Long userId, ZtpiCategory category) {
        return userReflectionQuestionOrderRepository.findByUserIdAndCategory(userId, category)
                .orElseThrow(() -> new BusinessException(
                        ReflectionErrorCode.USER_REFLECTION_QUESTION_ORDER_NOT_FOUND))
                .getSequence();
    }

    private ZtpiCategory selectWeightedRandom(Map<ZtpiCategory, Double> scoreMap) {
        ZtpiCategory[] categories = ZtpiCategory.values();
        double[] weights = new double[categories.length];
        double totalWeight = 0.0;

        for (int i = 0; i < categories.length; i++) {
            double userScore = scoreMap.getOrDefault(categories[i], categories[i].getIdealScore());
            weights[i] = Math.abs(categories[i].getIdealScore() - userScore);
            totalWeight += weights[i];
        }

        if (totalWeight == 0.0) {
            return categories[ThreadLocalRandom.current().nextInt(categories.length)];
        }

        double random = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cumulative = 0.0;
        for (int i = 0; i < categories.length; i++) {
            cumulative += weights[i];
            if (random < cumulative) {
                return categories[i];
            }
        }

        return categories[categories.length - 1];
    }

    private Long findQuestionId(Long sequence, ZtpiCategory category) {
        return reflectionQuestionRepository.findBySequenceAndCategory(sequence, category)
                .orElseThrow(() -> new BusinessException(
                        ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND))
                .getId();
    }
}
