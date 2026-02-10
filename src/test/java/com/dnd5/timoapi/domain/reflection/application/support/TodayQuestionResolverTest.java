package com.dnd5.timoapi.domain.reflection.application.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.UserReflectionQuestionOrderRepository;
import com.dnd5.timoapi.domain.reflection.infrastructure.cache.TodayQuestionCacheService;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestResultEntity;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestResultRepository;
import com.dnd5.timoapi.global.exception.BusinessException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodayQuestionResolverTest {

    @InjectMocks
    private TodayQuestionResolver todayQuestionResolver;

    @Mock
    private TodayQuestionCacheService cacheService;

    @Mock
    private UserReflectionQuestionOrderRepository userReflectionQuestionOrderRepository;

    @Mock
    private ReflectionQuestionRepository reflectionQuestionRepository;

    @Mock
    private UserTestRecordRepository userTestRecordRepository;

    @Mock
    private UserTestResultRepository userTestResultRepository;

    private static final Long USER_ID = 1L;
    private static final Long RECORD_ID = 10L;

    @Test
    void resolveTodayCategory_완료된_테스트_기록_없으면_예외() {
        when(userTestRecordRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(
                eq(USER_ID), eq(TestRecordStatus.COMPLETED)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> todayQuestionResolver.resolveTodayCategory(USER_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void resolveTodayCategory_가중치_비율대로_카테고리_선정() {
        setupMockRecord();
        setupMockResults(Map.of(
                ZtpiCategory.PAST_NEGATIVE, 2.10,
                ZtpiCategory.PAST_POSITIVE, 3.67,
                ZtpiCategory.PRESENT_HEDONISTIC, 4.33,
                ZtpiCategory.PRESENT_FATALISTIC, 1.67,
                ZtpiCategory.FUTURE, 1.69
        ));
        // FUTURE: |3.69 - 1.69| = 2.0, 나머지 전부 0
        // totalWeight = 2.0, FUTURE만 가중치 보유

        try (MockedStatic<ThreadLocalRandom> mockedRandom = Mockito.mockStatic(ThreadLocalRandom.class)) {
            ThreadLocalRandom mockRng = mock(ThreadLocalRandom.class);
            mockedRandom.when(ThreadLocalRandom::current).thenReturn(mockRng);
            when(mockRng.nextDouble(2.0)).thenReturn(1.5);

            ZtpiCategory result = todayQuestionResolver.resolveTodayCategory(USER_ID);
            assertThat(result).isEqualTo(ZtpiCategory.FUTURE);
        }
    }

    @Test
    void resolveTodayCategory_여러_카테고리에_가중치_있을때_올바른_선정() {
        setupMockRecord();
        // PAST_NEGATIVE: |2.10 - 1.10| = 1.0
        // PAST_POSITIVE: |3.67 - 3.67| = 0.0
        // PRESENT_HEDONISTIC: |4.33 - 4.33| = 0.0
        // PRESENT_FATALISTIC: |1.67 - 1.67| = 0.0
        // FUTURE: |3.69 - 2.69| = 1.0
        // totalWeight = 2.0
        setupMockResults(Map.of(
                ZtpiCategory.PAST_NEGATIVE, 1.10,
                ZtpiCategory.PAST_POSITIVE, 3.67,
                ZtpiCategory.PRESENT_HEDONISTIC, 4.33,
                ZtpiCategory.PRESENT_FATALISTIC, 1.67,
                ZtpiCategory.FUTURE, 2.69
        ));

        try (MockedStatic<ThreadLocalRandom> mockedRandom = Mockito.mockStatic(ThreadLocalRandom.class)) {
            ThreadLocalRandom mockRng = mock(ThreadLocalRandom.class);
            mockedRandom.when(ThreadLocalRandom::current).thenReturn(mockRng);

            // random=0.5 → cumulative: PAST_NEGATIVE(1.0) > 0.5 → PAST_NEGATIVE
            when(mockRng.nextDouble(2.0)).thenReturn(0.5);
            assertThat(todayQuestionResolver.resolveTodayCategory(USER_ID))
                    .isEqualTo(ZtpiCategory.PAST_NEGATIVE);

            // random=1.5 → cumulative: PAST_NEGATIVE(1.0) < 1.5, FUTURE(2.0) > 1.5 → FUTURE
            when(mockRng.nextDouble(2.0)).thenReturn(1.5);
            assertThat(todayQuestionResolver.resolveTodayCategory(USER_ID))
                    .isEqualTo(ZtpiCategory.FUTURE);
        }
    }

    @Test
    void resolveTodayCategory_모든_점수가_이상적이면_균등_랜덤() {
        setupMockRecord();
        setupMockResults(Map.of(
                ZtpiCategory.PAST_NEGATIVE, 2.10,
                ZtpiCategory.PAST_POSITIVE, 3.67,
                ZtpiCategory.PRESENT_HEDONISTIC, 4.33,
                ZtpiCategory.PRESENT_FATALISTIC, 1.67,
                ZtpiCategory.FUTURE, 3.69
        ));

        try (MockedStatic<ThreadLocalRandom> mockedRandom = Mockito.mockStatic(ThreadLocalRandom.class)) {
            ThreadLocalRandom mockRng = mock(ThreadLocalRandom.class);
            mockedRandom.when(ThreadLocalRandom::current).thenReturn(mockRng);
            when(mockRng.nextInt(5)).thenReturn(2);

            ZtpiCategory result = todayQuestionResolver.resolveTodayCategory(USER_ID);
            assertThat(result).isEqualTo(ZtpiCategory.values()[2]);
        }
    }

    @Test
    void resolveTodayCategory_통계적_분포_검증() {
        setupMockRecord();
        // PAST_NEGATIVE: |2.10 - 2.00| = 0.10
        // PAST_POSITIVE: |3.67 - 3.47| = 0.20
        // PRESENT_HEDONISTIC: |4.33 - 4.03| = 0.30
        // PRESENT_FATALISTIC: |1.67 - 1.27| = 0.40
        // FUTURE: |3.69 - 2.69| = 1.00
        // totalWeight = 2.0, 비율: 5%, 10%, 15%, 20%, 50%
        setupMockResults(Map.of(
                ZtpiCategory.PAST_NEGATIVE, 2.00,
                ZtpiCategory.PAST_POSITIVE, 3.47,
                ZtpiCategory.PRESENT_HEDONISTIC, 4.03,
                ZtpiCategory.PRESENT_FATALISTIC, 1.27,
                ZtpiCategory.FUTURE, 2.69
        ));

        Map<ZtpiCategory, Integer> counts = new EnumMap<>(ZtpiCategory.class);
        for (ZtpiCategory c : ZtpiCategory.values()) {
            counts.put(c, 0);
        }

        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            ZtpiCategory result = todayQuestionResolver.resolveTodayCategory(USER_ID);
            counts.merge(result, 1, Integer::sum);
        }

        assertThat(counts.get(ZtpiCategory.FUTURE))
                .isGreaterThan(counts.get(ZtpiCategory.PRESENT_FATALISTIC));
        assertThat(counts.get(ZtpiCategory.PRESENT_FATALISTIC))
                .isGreaterThan(counts.get(ZtpiCategory.PRESENT_HEDONISTIC));
        assertThat(counts.get(ZtpiCategory.PRESENT_HEDONISTIC))
                .isGreaterThan(counts.get(ZtpiCategory.PAST_POSITIVE));
        assertThat(counts.get(ZtpiCategory.PAST_POSITIVE))
                .isGreaterThan(counts.get(ZtpiCategory.PAST_NEGATIVE));
    }

    private void setupMockRecord() {
        UserTestRecordEntity mockRecord = mock(UserTestRecordEntity.class);
        when(mockRecord.getId()).thenReturn(RECORD_ID);
        when(userTestRecordRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(
                eq(USER_ID), eq(TestRecordStatus.COMPLETED)))
                .thenReturn(Optional.of(mockRecord));
    }

    private void setupMockResults(Map<ZtpiCategory, Double> scores) {
        List<UserTestResultEntity> results = scores.entrySet().stream()
                .map(entry -> {
                    UserTestResultEntity entity = mock(UserTestResultEntity.class);
                    when(entity.getCategory()).thenReturn(entry.getKey());
                    when(entity.getScore()).thenReturn(entry.getValue());
                    return entity;
                })
                .toList();
        when(userTestResultRepository.findByUserTestRecordId(RECORD_ID)).thenReturn(results);
    }
}
