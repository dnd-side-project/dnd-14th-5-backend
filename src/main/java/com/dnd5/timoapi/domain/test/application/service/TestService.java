package com.dnd5.timoapi.domain.test.application.service;

import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.model.Test;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestType;
import com.dnd5.timoapi.domain.test.domain.repository.TestQuestionRepository;
import com.dnd5.timoapi.domain.test.domain.repository.TestRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.test.exception.TestErrorCode;
import com.dnd5.timoapi.domain.test.exception.TestQuestionErrorCode;
import com.dnd5.timoapi.domain.test.exception.UserTestRecordErrorCode;
import com.dnd5.timoapi.domain.test.presentation.request.TestCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.request.TestUpdateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.TestDetailResponse;
import com.dnd5.timoapi.domain.test.presentation.response.TestResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TestService {

    private final TestRepository testRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final UserTestRecordRepository userTestRecordRepository;

    private final UserTestRecordService userTestRecordService;

    public void create(TestCreateRequest request) {
        if (testRepository.existsByTypeAndDeletedAtIsNull(request.type())) {
            throw new BusinessException(TestErrorCode.TEST_TYPE_ALREADY_EXISTS);
        }
        Test testModel = request.toModel();
        testRepository.save(TestEntity.from(testModel));
    }

    @Transactional(readOnly = true)
    public List<TestResponse> findAll() {
        return testRepository.findAllByDeletedAtIsNull().stream()
                .map(TestEntity::toModel)
                .map(TestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TestDetailResponse findById(Long testId) {
        TestEntity testEntity = getTestEntity(testId);
        return TestDetailResponse.from(testEntity.toModel());
    }

    @Transactional(readOnly = true)
    public TestDetailResponse findByType(TestType testType) {
        TestEntity testEntity = testRepository.findByTypeAndDeletedAtIsNull(testType)
                .orElseThrow(() -> new BusinessException(TestErrorCode.TEST_NOT_FOUND));
        return TestDetailResponse.from(testEntity.toModel());
    }

    public void update(Long testId, TestUpdateRequest request) {
        TestEntity testEntity = getTestEntity(testId);
        if (request.type() != null && request.type() != testEntity.getType()
                && testRepository.existsByTypeAndDeletedAtIsNull(request.type())) {
            throw new BusinessException(TestErrorCode.TEST_TYPE_ALREADY_EXISTS);
        }
        testEntity.update(request.type(), request.name(), request.description());
    }

    @Transactional
    public void delete(Long testId) {
        TestEntity testEntity = getTestEntity(testId);

        List<TestQuestionEntity> testQuestionEntityList = testQuestionRepository.findByTestIdAndDeletedAtIsNull(testId);
        if(testQuestionEntityList.isEmpty()) {
            throw new BusinessException(TestQuestionErrorCode.TEST_QUESTION_NOT_FOUND);
        }

        testQuestionEntityList.forEach(TestQuestionEntity::softDelete);

        List<UserTestRecordEntity> userTestRecordEntityList = userTestRecordRepository.findByTestIdAndDeletedAtIsNull(testId);
        if(userTestRecordEntityList.isEmpty()) {
            throw new BusinessException(UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND);
        }

        userTestRecordEntityList.forEach(
                userTestRecordEntity -> userTestRecordService.delete(userTestRecordEntity.getId())
        );

        testEntity.softDelete();
    }

    private TestEntity getTestEntity(Long testId) {
        return testRepository.findByIdAndDeletedAtIsNull(testId)
                .orElseThrow(() -> new BusinessException(TestErrorCode.TEST_NOT_FOUND));
    }

}
