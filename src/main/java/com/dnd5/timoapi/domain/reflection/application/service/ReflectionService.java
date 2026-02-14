package com.dnd5.timoapi.domain.reflection.application.service;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.model.Reflection;
import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedback;
import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionQuestion;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.application.support.TodayQuestionResolver;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionCreateRequest;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionCreateResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionDetailResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionQuestionDetailResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionQuestionResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionResponse;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.domain.user.exception.UserErrorCode;
import com.dnd5.timoapi.global.common.response.PageResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReflectionService {

    private final ReflectionRepository reflectionRepository;
    private final ReflectionQuestionRepository reflectionQuestionRepository;
    private final ReflectionFeedbackRepository reflectionFeedbackRepository;
    private final TodayQuestionResolver todayQuestionResolver;
    private final UserRepository userRepository;

    public ReflectionCreateResponse create(ReflectionCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        if (reflectionRepository.findByDateAndUserId(LocalDate.now(), userId).isPresent()) {
            throw new BusinessException(ReflectionErrorCode.TODAY_REFLECTION_ALREADY_EXISTS);
        }

        ReflectionQuestionEntity questionEntity = findTodayQuestionEntity(userId);

        Reflection reflectionModel = new Reflection(
                null,
                userId,
                questionEntity.getId(),
                LocalDate.now(),
                request.content(),
                null
        );
        ReflectionEntity saved = reflectionRepository.save(ReflectionEntity.from(reflectionModel));

        UserEntity userEntity = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        userEntity.incrementStreakDays();

        return new ReflectionCreateResponse(saved.getId());
    }

    @Transactional(readOnly = true)
    public ReflectionQuestionDetailResponse findQuestionToday() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ReflectionQuestionDetailResponse.from(findTodayQuestionEntity(userId).toModel());
    }

    @Transactional(readOnly = true)
    public ReflectionDetailResponse findReflectionToday() {
        Long userId = SecurityUtil.getCurrentUserId();
        ReflectionEntity reflectionEntity =
                reflectionRepository.findByDateAndUserId(LocalDate.now(), userId)
                        .orElseThrow(() -> new BusinessException(
                                ReflectionErrorCode.TODAY_REFLECTION_NOT_FOUND));

        ReflectionQuestionEntity questionEntity = findTodayQuestionEntity(userId);

        ReflectionFeedback feedback = reflectionFeedbackRepository.findByReflectionId(reflectionEntity.getId())
                .map(ReflectionFeedbackEntity::toModel)
                .orElse(null);

        return toResponse(reflectionEntity.toModel(), questionEntity.toModel(), feedback);
    }

    @Transactional(readOnly = true)
    public List<ReflectionResponse> findAllMy(YearMonth month) {
        Long userId = SecurityUtil.getCurrentUserId();

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<ReflectionEntity> reflections =
                reflectionRepository.findAllByUserIdAndDateBetween(userId, start, end);

        return reflections.stream()
                .map(entity -> {
                    ReflectionQuestionEntity questionEntity =
                            reflectionQuestionRepository.findById(entity.getQuestionId())
                                    .orElseThrow(() -> new BusinessException(
                                            ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND
                                    ));
                    return toResponse(entity.toModel(), questionEntity.toModel());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ReflectionDetailResponse findById(Long reflectionId) {
        ReflectionEntity reflectionEntity =
                reflectionRepository.findById(reflectionId)
                        .orElseThrow(() -> new BusinessException(
                                ReflectionErrorCode.REFLECTION_NOT_FOUND
                        ));

        ReflectionQuestionEntity questionEntity =
                reflectionQuestionRepository.findById(reflectionEntity.getQuestionId())
                        .orElseThrow(() -> new BusinessException(
                                ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND
                        ));

        ReflectionFeedback feedback = reflectionFeedbackRepository.findByReflectionId(reflectionEntity.getId())
                .map(ReflectionFeedbackEntity::toModel)
                .orElse(null);

        return toResponse(reflectionEntity.toModel(), questionEntity.toModel(), feedback);
    }

    private ReflectionQuestionEntity findTodayQuestionEntity(Long userId) {
        Long questionId = todayQuestionResolver.resolve(userId);
        return reflectionQuestionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(
                        ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND));
    }

    private ReflectionDetailResponse toResponse(
            Reflection reflection,
            ReflectionQuestion question,
            ReflectionFeedback feedback
    ) {
        return new ReflectionDetailResponse(
                reflection.id(),
                ReflectionQuestionResponse.from(question),
                reflection.answerText(),
                feedback != null ? ReflectionFeedbackResponse.from(feedback) : null,
                reflection.date()
        );
    }

    private ReflectionResponse toResponse(
            Reflection reflection,
            ReflectionQuestion question
    ) {
        return new ReflectionResponse(
                reflection.id(),
                ReflectionQuestionResponse.from(question),
                reflection.answerText(),
                reflection.date()
        );
    }
}
