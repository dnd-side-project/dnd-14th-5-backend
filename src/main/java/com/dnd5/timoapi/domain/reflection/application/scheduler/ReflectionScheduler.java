package com.dnd5.timoapi.domain.reflection.application.scheduler;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.UserReflectionQuestionOrderEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.UserReflectionQuestionOrderRepository;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processYesterdayReflections() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<ReflectionEntity> yesterdayReflections =
                reflectionRepository.findAllByDateAndUserDeletedAtIsNull(yesterday);

        log.info("Processing {} reflections from {}", yesterdayReflections.size(), yesterday);

        if (yesterdayReflections.isEmpty()) {
            log.info("Reflection scheduler completed - no reflections");
            return;
        }

        incrementQuestionSequences(yesterdayReflections);

        Set<Long> activeUserIds = yesterdayReflections.stream()
                .map(ReflectionEntity::getUserId)
                .collect(Collectors.toSet());
        userRepository.resetStreakForInactiveUsers(activeUserIds);

        log.info("Reflection scheduler completed");
    }

    private void incrementQuestionSequences(List<ReflectionEntity> reflections) {
        Set<Long> questionIds = reflections.stream()
                .map(ReflectionEntity::getQuestionId)
                .collect(Collectors.toSet());

        Map<Long, ReflectionQuestionEntity> questionMap =
                reflectionQuestionRepository.findAllById(questionIds)
                        .stream()
                        .collect(Collectors.toMap(ReflectionQuestionEntity::getId, q -> q));

        Set<Long> userIds = reflections.stream()
                .map(ReflectionEntity::getUserId)
                .collect(Collectors.toSet());

        Map<String, UserReflectionQuestionOrderEntity> orderMap =
                userReflectionQuestionOrderRepository.findAllByUserIdIn(userIds)
                        .stream()
                        .collect(Collectors.toMap(
                                o -> orderKey(o.getUserId(), o.getCategory()),
                                o -> o
                        ));

        for (ReflectionEntity reflection : reflections) {
            ReflectionQuestionEntity question = questionMap.get(reflection.getQuestionId());
            if (question == null) {
                log.warn("Question not found for questionId: {}", reflection.getQuestionId());
                continue;
            }

            ZtpiCategory category = question.getCategory();
            UserReflectionQuestionOrderEntity order = orderMap.get(orderKey(reflection.getUserId(), category));
            if (order == null) {
                log.warn("Order not found for userId: {}, category: {}", reflection.getUserId(), category);
                continue;
            }

            order.incrementSequence();
            log.info("Incremented sequence for userId: {}, category: {}", reflection.getUserId(), category);
        }
    }

    private String orderKey(Long userId, ZtpiCategory category) {
        return userId + ":" + category.name();
    }
}
