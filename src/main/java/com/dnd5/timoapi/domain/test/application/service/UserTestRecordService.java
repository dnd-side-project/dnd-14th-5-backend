package com.dnd5.timoapi.domain.test.application.service;

import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.test.domain.repository.TestRepository;
import com.dnd5.timoapi.domain.test.exception.TestErrorCode;
import com.dnd5.timoapi.domain.test.exception.UserTestRecordErrorCode;
import com.dnd5.timoapi.domain.test.presentation.request.UserTestRecordCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordCreateResponse;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordResponse;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.domain.user.exception.UserErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTestRecordService {

    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final UserTestRecordRepository userTestRecordRepository;

    public UserTestRecordCreateResponse create(UserTestRecordCreateRequest request) {
        UserEntity userEntity = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        TestEntity testEntity = testRepository.findById(request.testId())
                .orElseThrow(() -> new BusinessException(TestErrorCode.TEST_NOT_FOUND));

        UserTestRecord model = request.toModel();

        UserTestRecordEntity savedEntity =
                userTestRecordRepository.save(UserTestRecordEntity.from(userEntity, testEntity, model));

        return UserTestRecordCreateResponse.from(savedEntity.toModel());
    }

    public void complete(Long testRecordId) {
        UserTestRecordEntity userTestRecordEntity = getUserTestRecordEntity(testRecordId);
        userTestRecordEntity.complete();
    }

    @Transactional(readOnly = true)
    public List<UserTestRecordResponse> findAll(Long userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        return userTestRecordRepository.findByUserId(userId).stream()
                .map(UserTestRecordEntity::toModel)
                .map(UserTestRecordResponse::from)
                .toList();

    }

    private UserTestRecordEntity getUserTestRecordEntity(Long testRecordId) {
        return userTestRecordRepository.findById(testRecordId)
                .orElseThrow(() -> new BusinessException(UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND));
    }

}
