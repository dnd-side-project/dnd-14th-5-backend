package com.dnd5.timoapi.domain.reflection.application.service;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionQuestion;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionQuestionCreateRequest;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionQuestionUpdateRequest;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionQuestionDetailResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionQuestionResponse;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.common.response.PageResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReflectionQuestionService {

    private final ReflectionQuestionRepository reflectionQuestionRepository;

    public void create(ReflectionQuestionCreateRequest request) {
        Long nextSequence = reflectionQuestionRepository.findMaxSequenceByCategory(request.category()) + 1;

        ReflectionQuestion questionModel = ReflectionQuestion.create(
                nextSequence, request.category(), request.content(), request.createdBy());
        reflectionQuestionRepository.save(ReflectionQuestionEntity.from(questionModel));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReflectionQuestionResponse> findAll(
            Pageable pageable,
            String keyword,
            ZtpiCategory category
    ) {
        Page<ReflectionQuestionEntity> questionPage
                = reflectionQuestionRepository.searchByConditions(keyword, category, pageable);

        return new PageResponse<>(
                questionPage.stream()
                        .map(ReflectionQuestionEntity::toModel)
                        .map(ReflectionQuestionResponse::from)
                        .toList(),
                questionPage.getTotalElements(),
                questionPage.getNumber(),
                questionPage.getSize()
        );
    }

    @Transactional(readOnly = true)
    public ReflectionQuestionDetailResponse findById(Long questionId) {
        ReflectionQuestionEntity questionEntity = getQuestionEntity(questionId);
        return ReflectionQuestionDetailResponse.from(questionEntity.toModel());
    }

    public void update(Long questionId, ReflectionQuestionUpdateRequest request) {
        ReflectionQuestionEntity questionEntity = getQuestionEntity(questionId);
        questionEntity.update(request.content(), request.createdBy());
    }

    public void delete(Long questionId) {
        ReflectionQuestionEntity questionEntity = getQuestionEntity(questionId);
        ZtpiCategory category = questionEntity.getCategory();
        Long deletedSequence = questionEntity.getSequence();

        questionEntity.setSequence(-questionEntity.getId());
        questionEntity.setDeletedAt(LocalDateTime.now());

        reflectionQuestionRepository.findAllByCategoryAndSequenceGreaterThan(category, deletedSequence)
                .forEach(ReflectionQuestionEntity::decreaseSequence);
    }

    private ReflectionQuestionEntity getQuestionEntity(Long questionId) {
        return reflectionQuestionRepository.findByIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new BusinessException(
                        ReflectionErrorCode.REFLECTION_QUESTION_NOT_FOUND));
    }
}
