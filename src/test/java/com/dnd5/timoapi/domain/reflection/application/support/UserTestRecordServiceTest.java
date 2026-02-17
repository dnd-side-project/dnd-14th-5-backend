package com.dnd5.timoapi.domain.reflection.application.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.UserReflectionQuestionOrderRepository;
import com.dnd5.timoapi.domain.reflection.infrastructure.cache.TodayQuestionCacheService;
import com.dnd5.timoapi.domain.test.application.service.UserTestRecordService;
import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestResultEntity;
import com.dnd5.timoapi.domain.test.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.test.domain.repository.TestRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestResultRepository;
import com.dnd5.timoapi.domain.test.presentation.request.UserTestRecordCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordCreateResponse;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserTestRecordServiceTest {

    @InjectMocks
    private UserTestRecordService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private UserTestRecordRepository userTestRecordRepository;

    private static final Long USER_ID = 1L;
    private static final Long TEST_ID = 100L;

    @Test
    void create_이미_진행중인_테스트_기록이_존재하면_예외() {
        try (MockedStatic<SecurityUtil> mocked =
                Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserId)
                    .thenReturn(USER_ID);

            UserEntity user = mock(UserEntity.class);
            TestEntity test = mock(TestEntity.class);

            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(user));
            when(testRepository.findById(TEST_ID))
                    .thenReturn(Optional.of(test));

            when(test.getId()).thenReturn(TEST_ID);

            UserTestRecordEntity mockRecord = mock(UserTestRecordEntity.class);
            when(mockRecord.getId()).thenReturn(1L);
            when(userTestRecordRepository
                    .findByUserIdAndTestIdAndStatus(
                            USER_ID,
                            TEST_ID,
                            TestRecordStatus.IN_PROGRESS))
                    .thenReturn(Optional.of(mockRecord));

            UserTestRecordCreateRequest request =
                    new UserTestRecordCreateRequest(TEST_ID);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BusinessException.class);

            verify(userTestRecordRepository, never()).save(any());
        }
    }

    @Test
    void userTestRecordServiceCreate_테스트_기록이_정상적으로_저장되는_경우() {
        try (MockedStatic<SecurityUtil> mocked =
                Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserId)
                    .thenReturn(USER_ID);

            UserEntity user = mock(UserEntity.class);
            TestEntity test = mock(TestEntity.class);
            UserTestRecordEntity savedEntity = mock(UserTestRecordEntity.class);
            UserTestRecord model = mock(UserTestRecord.class);

            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(user));
            when(testRepository.findById(TEST_ID))
                    .thenReturn(Optional.of(test));

            UserTestRecordEntity mockRecord = mock(UserTestRecordEntity.class);
            when(mockRecord.getId()).thenReturn(1L);
            when(userTestRecordRepository
                    .findByUserIdAndTestIdAndStatus(
                            USER_ID,
                            TEST_ID,
                            TestRecordStatus.IN_PROGRESS))
                    .thenReturn(Optional.of(mockRecord));

            when(savedEntity.toModel()).thenReturn(model);

            when(userTestRecordRepository.save(any()))
                    .thenReturn(savedEntity);

            UserTestRecordCreateRequest request =
                    new UserTestRecordCreateRequest(TEST_ID);

            UserTestRecordCreateResponse response =
                    service.create(request);

            assertThat(response).isNotNull();
            verify(userTestRecordRepository)
                    .save(any(UserTestRecordEntity.class));
        }
    }

}
