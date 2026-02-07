package com.dnd5.timoapi.domain.test.application.service;

import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.domain.model.Test;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestType;
import com.dnd5.timoapi.domain.test.domain.repository.TestRepository;
import com.dnd5.timoapi.domain.test.exception.TestErrorCode;
import com.dnd5.timoapi.domain.test.presentation.request.TestCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.request.TestUpdateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.TestDetailResponse;
import com.dnd5.timoapi.domain.test.presentation.response.TestResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TestService {

    private final TestRepository testRepository;

    public void create(TestCreateRequest request) {
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
        testEntity.update(request.type(), request.name(), request.description());
    }

    public void delete(Long testId) {
        TestEntity testEntity = getTestEntity(testId);
        testEntity.setDeletedAt(LocalDateTime.now());
    }

    private TestEntity getTestEntity(Long testId) {
        return testRepository.findByIdAndDeletedAtIsNull(testId)
                .orElseThrow(() -> new BusinessException(TestErrorCode.TEST_NOT_FOUND));
    }

}
