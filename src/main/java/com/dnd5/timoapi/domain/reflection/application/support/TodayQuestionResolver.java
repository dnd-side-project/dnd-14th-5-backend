package com.dnd5.timoapi.domain.reflection.application.support;

import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.UserReflectionQuestionOrderRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.infrastructure.cache.TodayQuestionCacheService;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TodayQuestionResolver {

    private final TodayQuestionCacheService cacheService;
    private final UserReflectionQuestionOrderRepository userReflectionQuestionOrderRepository;
    private final ReflectionQuestionRepository reflectionQuestionRepository;

    public Long resolve(Long userId) {
        Long cached = cacheService.getQuestionId(userId);
        if (cached != null) {
            return cached;
        }

        ZtpiCategory category = resolveFarthestCategory(userId);
        Long sequence = resolveTodaySequence(userId, category);
        Long questionId = findQuestionId(sequence, category);

        cacheService.setQuestionId(userId, questionId);
        return questionId;
    }

    public void cacheQuestionId(Long userId, Long questionId) {
        cacheService.setQuestionId(userId, questionId);
    }

    public ZtpiCategory resolveFarthestCategory(Long userId) {
        // FIXME: @didfodms 가장 부족한 시간관을 계산하는 로직
        return ZtpiCategory.FUTURE;
    }

    public Long resolveTodaySequence(Long userId, ZtpiCategory category) {
        return userReflectionQuestionOrderRepository.findByUserIdAndCategory(userId, category)
                .orElseThrow(() -> new BusinessException(
                        ReflectionErrorCode.USER_REFLECTION_QUESTION_ORDER_NOT_FOUND))
                .getSequence();
    }

    private Long findQuestionId(Long sequence, ZtpiCategory category) {
        return reflectionQuestionRepository.findBySequenceAndCategory(sequence, category)
                .orElseThrow(() -> new BusinessException(
                        ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND))
                .getId();
    }
}
