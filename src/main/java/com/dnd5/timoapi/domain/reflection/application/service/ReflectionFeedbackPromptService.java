package com.dnd5.timoapi.domain.reflection.application.service;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackPromptEntity;
import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedbackPrompt;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackPromptRepository;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionFeedbackPromptCreateRequest;
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
}
