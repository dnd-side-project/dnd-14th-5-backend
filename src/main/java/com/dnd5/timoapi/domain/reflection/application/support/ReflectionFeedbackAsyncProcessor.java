package com.dnd5.timoapi.domain.reflection.application.support;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.infrastructure.ai.FeedbackGenerator;
import com.dnd5.timoapi.domain.reflection.infrastructure.ai.FeedbackResult;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestResultEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserTestResultRepository;
import com.dnd5.timoapi.domain.user.exception.UserTestRecordErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import com.dnd5.timoapi.global.analytics.event.FeedbackReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReflectionFeedbackAsyncProcessor {

    private static final int MIN_FEEDBACK_SCORE = 0;
    private static final int MAX_FEEDBACK_SCORE = 5;

    private final ApplicationEventPublisher eventPublisher;
    private final ReflectionRepository reflectionRepository;
    private final ReflectionFeedbackRepository reflectionFeedbackRepository;
    private final ReflectionQuestionRepository reflectionQuestionRepository;
    private final UserTestResultRepository userTestResultRepository;
    private final FeedbackGenerator feedbackGenerator;

    @Async("feedbackTaskExecutor")
    @Transactional
    public void process(Long reflectionId) {
        log.info("feedback_async_start reflectionId={}", reflectionId);
        ReflectionFeedbackEntity feedbackEntity = reflectionFeedbackRepository
                .findByReflectionId(reflectionId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_FEEDBACK_NOT_FOUND));

        try {
            ReflectionEntity reflectionEntity = reflectionRepository.findById(reflectionId)
                    .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_NOT_FOUND));

            ReflectionQuestionEntity questionEntity = reflectionQuestionRepository
                    .findById(reflectionEntity.getQuestionId())
                    .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND));

            FeedbackResult feedbackResult = feedbackGenerator.execute(
                    questionEntity.getCategory(),
                    questionEntity.getContent(),
                    reflectionEntity.getAnswerText()
            );
            validateScore(feedbackResult.score());

            ZtpiCategory category = questionEntity.getCategory();

            Long userId = reflectionEntity.getUserId();
            UserTestResultEntity latestTestResult = userTestResultRepository
                    .findFirstByUserTestRecord_User_IdAndCategoryAndDeletedAtIsNullOrderByCreatedAtDesc(userId, category)
                    .orElseThrow(() -> new BusinessException(UserTestRecordErrorCode.USER_TEST_RESULT_NOT_FOUND));

            double testScore = latestTestResult.getScore();
            LocalDateTime latestTestDate = latestTestResult.getCreatedAt();

            List<Integer> previousReflectionFeedbackScores = reflectionFeedbackRepository
                    .findScoresByUserAndCategoryAfter(userId, category, FeedbackStatus.COMPLETED, latestTestDate, reflectionId);

            List<Integer> nonZeroPreviousScores = previousReflectionFeedbackScores.stream()
                    .filter(s -> s != 0)
                    .toList();

            double scoreSum = testScore + nonZeroPreviousScores.stream().mapToInt(Integer::intValue).sum();
            int count = 1 + nonZeroPreviousScores.size();

            double beforeScore = scoreSum / count;

            int currentScore = feedbackResult.score();
            double afterScore = currentScore == 0
                    ? beforeScore
                    : (scoreSum + currentScore) / (count + 1);

            boolean isIncreased = beforeScore < afterScore;
            double changedScore = Math.abs(afterScore - beforeScore);

            feedbackEntity.complete(feedbackResult.score(), feedbackResult.content(), category, isIncreased, changedScore, beforeScore, afterScore);

            eventPublisher.publishEvent(new FeedbackReceivedEvent(userId, reflectionId, feedbackResult.score()));
        } catch (Exception e) {
            log.error("Feedback generation failed for reflectionId={}", reflectionId, e);
            feedbackEntity.fail();
        }
    }

    @Transactional
    public void markAsFailed(Long reflectionId) {
        reflectionFeedbackRepository.findByReflectionId(reflectionId)
                .ifPresent(ReflectionFeedbackEntity::fail);
    }

    private void validateScore(int score) {
        if (score < MIN_FEEDBACK_SCORE || score > MAX_FEEDBACK_SCORE) {
            throw new BusinessException(
                    ReflectionErrorCode.REFLECTION_FEEDBACK_SCORE_OUT_OF_RANGE,
                    score,
                    MIN_FEEDBACK_SCORE,
                    MAX_FEEDBACK_SCORE
            );
        }
    }
}
