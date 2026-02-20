package com.dnd5.timoapi.domain.reflection.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackPromptEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackPromptRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionFeedbackPromptCreateRequest;
import com.dnd5.timoapi.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReflectionFeedbackPromptServiceTest {

    @InjectMocks
    private ReflectionFeedbackPromptService reflectionFeedbackPromptService;

    @Mock
    private ReflectionFeedbackPromptRepository reflectionFeedbackPromptRepository;

    @Test
    void create_중복_version이면_예외() {
        ReflectionFeedbackPromptCreateRequest request = new ReflectionFeedbackPromptCreateRequest(1, "prompt");
        when(reflectionFeedbackPromptRepository.existsByVersion(1)).thenReturn(true);

        assertThatThrownBy(() -> reflectionFeedbackPromptService.create(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode())
                            .isEqualTo(ReflectionErrorCode.REFLECTION_FEEDBACK_PROMPT_VERSION_DUPLICATED);
                });

        verify(reflectionFeedbackPromptRepository, never()).save(any(ReflectionFeedbackPromptEntity.class));
    }
}
