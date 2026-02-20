package com.dnd5.timoapi.domain.reflection.application.scheduler;

import com.dnd5.timoapi.domain.reflection.application.support.TodayQuestionResolver;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.UserReflectionQuestionOrderRepository;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReflectionScheduler {

    private final ReflectionRepository reflectionRepository;
    private final ReflectionQuestionRepository reflectionQuestionRepository;
    private final UserReflectionQuestionOrderRepository userReflectionQuestionOrderRepository;
    private final TodayQuestionResolver todayQuestionResolver;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processYesterdayReflections() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<ReflectionEntity> yesterdayReflections =
                reflectionRepository.findAllByDateAndUserDeletedAtIsNull(yesterday);

        log.info("Processing {} reflections from {}", yesterdayReflections.size(), yesterday);

        for (ReflectionEntity reflection : yesterdayReflections) {
            Long userId = reflection.getUserId();
            Long questionId = reflection.getQuestionId();

            ReflectionQuestionEntity question = reflectionQuestionRepository.findById(questionId)
                    .orElse(null);

            if (question == null) {
                log.warn("Question not found for questionId: {}", questionId);
                continue;
            }

            ZtpiCategory answeredCategory = question.getCategory();
            userReflectionQuestionOrderRepository.findByUserIdAndCategory(userId, answeredCategory)
                    .ifPresent(order -> {
                        order.incrementSequence();
                        log.info("Incremented sequence for userId: {}, category: {}", userId, answeredCategory);
                    });

            cacheTodayQuestion(userId);
        }

        resetStreakForInactiveUsers(yesterdayReflections);

        log.info("Reflection scheduler completed");
    }

    private void resetStreakForInactiveUsers(List<ReflectionEntity> yesterdayReflections) {
        Set<Long> activeUserIds = yesterdayReflections.stream()
                .map(ReflectionEntity::getUserId)
                .collect(Collectors.toSet());

        List<UserEntity> streakUsers = userRepository.findAllByStreakDaysGreaterThanAndDeletedAtIsNull(0);
        for (UserEntity user : streakUsers) {
            if (!activeUserIds.contains(user.getId())) {
                user.resetStreakDays();
                log.info("Reset streak for userId: {}", user.getId());
            }
        }
    }

    private void cacheTodayQuestion(Long userId) {
        ZtpiCategory todayCategory = todayQuestionResolver.resolveTodayCategory(userId);
        Long sequence = todayQuestionResolver.resolveTodaySequence(userId, todayCategory);

        reflectionQuestionRepository.findBySequenceAndCategory(sequence, todayCategory)
                .ifPresent(todayQuestion -> {
                    todayQuestionResolver.cacheQuestionId(userId, todayQuestion.getId());
                    log.info("Cached today's question for userId: {}, questionId: {}",
                            userId, todayQuestion.getId());
                });
    }
}
