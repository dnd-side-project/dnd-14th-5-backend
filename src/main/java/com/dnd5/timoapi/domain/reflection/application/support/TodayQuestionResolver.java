package com.dnd5.timoapi.domain.reflection.application.support;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.UserReflectionQuestionOrderRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.infrastructure.cache.TodayQuestionCacheService;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestResultEntity;
import com.dnd5.timoapi.domain.user.domain.model.enums.UserTestRecordStatus;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.user.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.user.domain.repository.UserTestResultRepository;
import com.dnd5.timoapi.domain.user.exception.UserTestRecordErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TodayQuestionResolver {

    private static final int QUESTION_POOL_SIZE = 5;
    private static final int MAX_POOL_SELECTION_ATTEMPTS = 50;

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

        List<Long> questionPool = createQuestionPool(userId);
        Long questionId = questionPool.getFirst();

        cacheService.setQuestionPool(userId, questionPool);
        cacheService.setQuestionId(userId, questionId);
        log.info("question_pool_resolved userId={} questionIds={}", userId, questionPool);
        return questionId;
    }

    public Long change(Long userId) {
        resolve(userId);
        Long questionId = cacheService.getNextQuestionId(userId);
        if (questionId == null) {
            cacheService.evict(userId);
            resolve(userId);
            questionId = cacheService.getNextQuestionId(userId);
        }
        cacheService.setQuestionId(userId, questionId);
        return questionId;
    }

    public void cacheQuestionId(Long userId, Long questionId) {
        cacheService.setQuestionId(userId, questionId);
    }

    public ZtpiCategory resolveTodayCategory(Long userId) {
        return selectWeightedRandom(findScoreMap(userId));
    }

    private Map<ZtpiCategory, Double> findScoreMap(Long userId) {
        UserTestRecordEntity latestRecord = userTestRecordRepository
                .findTopByUserIdAndStatusOrderByCreatedAtDesc(userId, UserTestRecordStatus.COMPLETED)
                .orElseThrow(() -> new BusinessException(
                        UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND));

        List<UserTestResultEntity> results =
                userTestResultRepository.findByUserTestRecordId(latestRecord.getId());

        Map<ZtpiCategory, Double> scoreMap = results.stream()
                .collect(Collectors.toMap(
                        UserTestResultEntity::getCategory,
                        UserTestResultEntity::getScore));

        return scoreMap;
    }

    public Long resolveTodaySequence(Long userId, ZtpiCategory category) {
        return userReflectionQuestionOrderRepository.findByUserIdAndCategory(userId, category)
                .orElseThrow(() -> new BusinessException(
                        ReflectionErrorCode.USER_REFLECTION_QUESTION_ORDER_NOT_FOUND))
                .getSequence();
    }

    private List<Long> createQuestionPool(Long userId) {
        Set<Long> questionIds = new LinkedHashSet<>();
        Map<ZtpiCategory, Long> offsets = new EnumMap<>(ZtpiCategory.class);
        Map<ZtpiCategory, Double> scoreMap = findScoreMap(userId);

        for (int attempt = 0;
                attempt < MAX_POOL_SELECTION_ATTEMPTS && questionIds.size() < QUESTION_POOL_SIZE;
                attempt++) {
            ZtpiCategory category = selectWeightedRandom(scoreMap);
            long maxSequence = reflectionQuestionRepository.findMaxSequenceByCategory(category);
            if (maxSequence == 0) {
                continue;
            }

            long offset = offsets.getOrDefault(category, 0L);
            long baseSequence = resolveTodaySequence(userId, category);
            long sequence = ((baseSequence - 1 + offset) % maxSequence) + 1;
            offsets.put(category, offset + 1);

            reflectionQuestionRepository.findBySequenceAndCategory(sequence, category)
                    .map(ReflectionQuestionEntity::getId)
                    .ifPresent(questionIds::add);
        }

        if (questionIds.size() < QUESTION_POOL_SIZE) {
            reflectionQuestionRepository.findAllByDeletedAtIsNull().stream()
                    .map(ReflectionQuestionEntity::getId)
                    .filter(questionId -> !questionIds.contains(questionId))
                    .limit(QUESTION_POOL_SIZE - questionIds.size())
                    .forEach(questionIds::add);
        }

        if (questionIds.isEmpty()) {
            throw new BusinessException(ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND);
        }
        return new ArrayList<>(questionIds);
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

}
