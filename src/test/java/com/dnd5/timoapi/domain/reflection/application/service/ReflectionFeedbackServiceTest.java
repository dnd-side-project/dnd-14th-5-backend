package com.dnd5.timoapi.domain.reflection.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnd5.timoapi.domain.reflection.application.support.ReflectionFeedbackAsyncProcessor;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionFeedbackRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackDetailResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class ReflectionFeedbackServiceTest {

    @InjectMocks
    private ReflectionFeedbackService reflectionFeedbackService;

    @Mock
    private ReflectionFeedbackRepository reflectionFeedbackRepository;

    @Mock
    private ReflectionRepository reflectionRepository;

    @Mock
    private ReflectionFeedbackAsyncProcessor reflectionFeedbackAsyncProcessor;

    @Test
    void create_다른_유저_회고면_403_예외() {
        Long reflectionId = 10L;
        ReflectionEntity reflectionEntity = new ReflectionEntity(2L, 3L, LocalDate.now(), "text");
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            assertThatThrownBy(() -> reflectionFeedbackService.create(reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException businessException = (BusinessException) exception;
                        assertThat(businessException.getErrorCode())
                                .isEqualTo(ReflectionErrorCode.REFLECTION_NOT_OWNER);
                    });
        }

        verify(reflectionFeedbackRepository, never()).findByReflectionId(any());
    }

    @Test
    void create_신규_피드백은_PROCESSING_상태로_즉시_반환() {
        Long reflectionId = 10L;
        ReflectionEntity reflectionEntity = new ReflectionEntity(1L, 3L, LocalDate.now(), "회고 내용");
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));
        when(reflectionFeedbackRepository.findByReflectionId(reflectionId)).thenReturn(Optional.empty());

        ReflectionFeedbackEntity processingFeedback = new ReflectionFeedbackEntity(reflectionId, 0, null, FeedbackStatus.PROCESSING);
        when(reflectionFeedbackRepository.save(any())).thenReturn(processingFeedback);

        try (MockedStatic<SecurityUtil> securityMocked = Mockito.mockStatic(SecurityUtil.class);
             MockedStatic<TransactionSynchronizationManager> txMocked = Mockito.mockStatic(TransactionSynchronizationManager.class)) {

            securityMocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
            txMocked.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> null);

            ReflectionFeedbackDetailResponse response = reflectionFeedbackService.create(reflectionId);

            assertThat(response.status()).isEqualTo(FeedbackStatus.PROCESSING);
        }
    }

    @Test
    void create_FAILED_기존_피드백은_재생성_허용() {
        Long reflectionId = 10L;
        ReflectionEntity reflectionEntity = new ReflectionEntity(1L, 3L, LocalDate.now(), "회고 내용");
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        ReflectionFeedbackEntity failedFeedback = spy(
                new ReflectionFeedbackEntity(reflectionId, 0, null, FeedbackStatus.FAILED)
        );
        when(reflectionFeedbackRepository.findByReflectionId(reflectionId))
                .thenReturn(Optional.of(failedFeedback));

        try (MockedStatic<SecurityUtil> securityMocked = Mockito.mockStatic(SecurityUtil.class);
             MockedStatic<TransactionSynchronizationManager> txMocked = Mockito.mockStatic(TransactionSynchronizationManager.class)) {

            securityMocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
            txMocked.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> null);

            ReflectionFeedbackDetailResponse response = reflectionFeedbackService.create(reflectionId);

            assertThat(response.status()).isEqualTo(FeedbackStatus.PROCESSING);
        }

        verify(failedFeedback).restartProcessing();
        verify(reflectionFeedbackRepository, never()).save(any());
    }

    @Test
    void create_PROCESSING_기존_피드백도_재시도_허용() {
        Long reflectionId = 10L;
        ReflectionEntity reflectionEntity = new ReflectionEntity(1L, 3L, LocalDate.now(), "회고 내용");
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        ReflectionFeedbackEntity processingFeedback = spy(
                new ReflectionFeedbackEntity(reflectionId, 0, null, FeedbackStatus.PROCESSING)
        );
        when(reflectionFeedbackRepository.findByReflectionId(reflectionId))
                .thenReturn(Optional.of(processingFeedback));

        try (MockedStatic<SecurityUtil> securityMocked = Mockito.mockStatic(SecurityUtil.class);
             MockedStatic<TransactionSynchronizationManager> txMocked = Mockito.mockStatic(TransactionSynchronizationManager.class)) {

            securityMocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
            txMocked.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> null);

            ReflectionFeedbackDetailResponse response = reflectionFeedbackService.create(reflectionId);

            assertThat(response.status()).isEqualTo(FeedbackStatus.PROCESSING);
        }

        verify(processingFeedback).restartProcessing();
    }

    @Test
    void create_COMPLETED_기존_피드백은_재생성_불가() {
        Long reflectionId = 10L;
        ReflectionEntity reflectionEntity = new ReflectionEntity(1L, 3L, LocalDate.now(), "회고 내용");
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflectionEntity));

        ReflectionFeedbackEntity completedFeedback = new ReflectionFeedbackEntity(reflectionId, 80, "피드백", FeedbackStatus.COMPLETED);
        when(reflectionFeedbackRepository.findByReflectionId(reflectionId))
                .thenReturn(Optional.of(completedFeedback));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            assertThatThrownBy(() -> reflectionFeedbackService.create(reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> {
                        BusinessException businessException = (BusinessException) exception;
                        assertThat(businessException.getErrorCode())
                                .isEqualTo(ReflectionErrorCode.REFLECTION_FEEDBACK_ALREADY_EXISTS);
                    });
        }
    }
}
