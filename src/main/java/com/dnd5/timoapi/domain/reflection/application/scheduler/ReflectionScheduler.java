package com.dnd5.timoapi.domain.reflection.application.scheduler;

import com.dnd5.timoapi.domain.reflection.application.support.TodayQuestionResolver;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.UserReflectionQuestionOrderRepository;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.time.LocalDate;
import java.util.List;
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

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processYesterdayReflections() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<ReflectionEntity> yesterdayReflections = reflectionRepository.findAllByDate(yesterday);

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

        log.info("Reflection scheduler completed");
    }

    private void cacheTodayQuestion(Long userId) {
        ZtpiCategory todayCategory = todayQuestionResolver.resolveFarthestCategory(userId);
        Long sequence = todayQuestionResolver.resolveTodaySequence(userId, todayCategory);

        reflectionQuestionRepository.findBySequenceAndCategory(sequence, todayCategory)
                .ifPresent(todayQuestion -> {
                    todayQuestionResolver.cacheQuestionId(userId, todayQuestion.getId());
                    log.info("Cached today's question for userId: {}, questionId: {}",
                            userId, todayQuestion.getId());
                });
    }
}
