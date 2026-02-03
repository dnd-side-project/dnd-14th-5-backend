package com.dnd5.timoapi.domain.reflection.application.service;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackPromptEntity;
import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedbackPrompt;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackPromptRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionFeedbackPromptCreateRequest;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackPromptDetailResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackPromptResponse;
import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.exception.TestErrorCode;
import com.dnd5.timoapi.domain.test.presentation.response.TestDetailResponse;
import com.dnd5.timoapi.domain.test.presentation.response.TestResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReflectionFeedbackPromptService {

    private final ReflectionFeedbackPromptRepository reflectionFeedbackPromptRepository;

    public void create(ReflectionFeedbackPromptCreateRequest request) {
        ReflectionFeedbackPrompt ReflectionFeedbackPrompt = request.toModel();
        reflectionFeedbackPromptRepository.save(ReflectionFeedbackPromptEntity.from(ReflectionFeedbackPrompt));
    }

    @Transactional(readOnly = true)
    public List<ReflectionFeedbackPromptResponse> findAll() {
        return reflectionFeedbackPromptRepository.findAll().stream()
                .map(ReflectionFeedbackPromptEntity::toModel)
                .map(ReflectionFeedbackPromptResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReflectionFeedbackPromptDetailResponse findByVersion(int version) {
        ReflectionFeedbackPromptEntity reflectionFeedbackPromptEntity = getReflectionFeedbackPromptEntity(version);
        return ReflectionFeedbackPromptDetailResponse.from(reflectionFeedbackPromptEntity.toModel());
    }

    private ReflectionFeedbackPromptEntity getReflectionFeedbackPromptEntity(int version) {
        return reflectionFeedbackPromptRepository.findByVersion(version)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_FEEDBACK_PROMPT_NOT_FOUND));
    }
}
