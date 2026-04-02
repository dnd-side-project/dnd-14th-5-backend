package com.dnd5.timoapi.domain.reflection.application.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnd5.timoapi.domain.user.application.service.UserTestRecordService;
import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.user.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import com.dnd5.timoapi.domain.test.domain.repository.TestRepository;
import com.dnd5.timoapi.domain.user.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.user.presentation.request.UserTestRecordCreateRequest;
import com.dnd5.timoapi.domain.user.presentation.response.UserTestRecordCreateResponse;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
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
    void create_이미_진행중인_테스트_기록이_존재하면_기존_기록_반환() {
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

            UserTestRecord existingModel = new UserTestRecord(1L, USER_ID, TEST_ID, TestRecordStatus.IN_PROGRESS, null, null);
            UserTestRecordEntity mockRecord = mock(UserTestRecordEntity.class);
            when(mockRecord.toModel()).thenReturn(existingModel);
            when(userTestRecordRepository
                    .findByUserIdAndTestIdAndStatus(
                            USER_ID,
                            TEST_ID,
                            TestRecordStatus.IN_PROGRESS))
                    .thenReturn(Optional.of(mockRecord));

            UserTestRecordCreateRequest request =
                    new UserTestRecordCreateRequest(TEST_ID);

            UserTestRecordCreateResponse response = service.create(request);

            assertThat(response.isExisting()).isTrue();
            assertThat(response.id()).isEqualTo(1L);
            verify(userTestRecordRepository, never()).save(any());
        }
    }

}
