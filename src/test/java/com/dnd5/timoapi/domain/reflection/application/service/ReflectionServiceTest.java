package com.dnd5.timoapi.domain.reflection.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnd5.timoapi.domain.reflection.application.support.TodayQuestionResolver;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.infrastructure.cache.TodayQuestionCacheService;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReflectionServiceTest {

    @InjectMocks
    private ReflectionService reflectionService;

    @Mock
    private ReflectionRepository reflectionRepository;

    @Mock
    private ReflectionQuestionRepository reflectionQuestionRepository;

    @Mock
    private ReflectionFeedbackRepository reflectionFeedbackRepository;

    @Mock
    private TodayQuestionResolver todayQuestionResolver;

    @Mock
    private TodayQuestionCacheService todayQuestionCacheService;

    @Mock
    private UserRepository userRepository;

    @Test
    void findById_다른_유저_회고면_403_예외() {
        Long reflectionId = 10L;
        Long reflectionOwnerId = 2L;
        Long currentUserId = 1L;

        ReflectionEntity reflectionEntity = mock(ReflectionEntity.class);
        when(reflectionEntity.getId()).thenReturn(reflectionId);
        when(reflectionEntity.getUserId()).thenReturn(reflectionOwnerId);
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);

            assertThatThrownBy(() -> reflectionService.findById(reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException businessException = (BusinessException) exception;
                        assertThat(businessException.getErrorCode())
                                .isEqualTo(ReflectionErrorCode.REFLECTION_NOT_OWNER);
                    });
        }

        verify(reflectionQuestionRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }
}
