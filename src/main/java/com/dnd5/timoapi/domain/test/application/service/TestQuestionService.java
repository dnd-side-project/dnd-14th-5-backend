package com.dnd5.timoapi.domain.test.application.service;

import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import com.dnd5.timoapi.domain.test.domain.repository.TestQuestionRepository;
import com.dnd5.timoapi.domain.test.domain.repository.TestRepository;
import com.dnd5.timoapi.domain.test.exception.TestErrorCode;
import com.dnd5.timoapi.domain.test.exception.TestQuestionErrorCode;
import com.dnd5.timoapi.domain.test.presentation.request.TestQuestionUpdateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.TestQuestionDetailResponse;
import com.dnd5.timoapi.domain.test.presentation.response.TestQuestionResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.domain.test.presentation.request.TestQuestionCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TestQuestionService {

    private final TestRepository testRepository;
    private final TestQuestionRepository testQuestionRepository;

    public void create(Long testId, TestQuestionCreateRequest request) {
        TestEntity entity = testRepository.findById(testId)
                .orElseThrow(() -> new BusinessException(TestErrorCode.TEST_NOT_FOUND));

        TestQuestionEntity testEntity = TestQuestionEntity.of(
                entity,
                request.category(),
                request.content(),
                request.sequence(),
                request.isReversed()
        );

        testQuestionRepository.save(testEntity);
    }

    @Transactional(readOnly = true)
    public List<TestQuestionResponse> findAll(Long testId) {
        return testQuestionRepository.findByTestIdAndDeletedAtIsNullOrderBySequenceAsc(testId).stream()
                .map(TestQuestionEntity::toModel)
                .map(TestQuestionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TestQuestionDetailResponse findById(Long testId, Long questionId) {
        TestQuestionEntity testQuestionEntity = getTestQuestionEntity(testId, questionId);
        return TestQuestionDetailResponse.from(testQuestionEntity.toModel());
    }

    public void update(Long questionId, Long testId, @Valid TestQuestionUpdateRequest request) {
        TestQuestionEntity testQuestionEntity = testQuestionRepository.findByIdAndTestIdAndDeletedAtIsNull(questionId, testId)
                .orElseThrow(() -> new BusinessException(TestQuestionErrorCode.TEST_QUESTION_NOT_FOUND));
        testQuestionEntity.update(
                request.category(),
                request.content(),
                request.sequence(),
                request.isReversed()
        );
    }

    public void delete(Long questionId, Long testId) {
        TestEntity testEntity = testRepository.findById(testId)
                .orElseThrow(() -> new BusinessException(TestErrorCode.TEST_NOT_FOUND));
        TestQuestionEntity testQuestionEntity = testQuestionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(TestQuestionErrorCode.TEST_QUESTION_NOT_FOUND));

        if (!testEntity.getId().equals(testQuestionEntity.getId())) {
            throw new BusinessException(TestQuestionErrorCode.TEST_QUESTION_NOT_FOUND);
        }
        testQuestionEntity.setDeletedAt(LocalDateTime.now());
    }

    private TestQuestionEntity getTestQuestionEntity(Long testId, Long questionId) {
        return testQuestionRepository.findByIdAndTestIdAndDeletedAtIsNull(questionId, testId)
                .orElseThrow(() -> new BusinessException(TestQuestionErrorCode.TEST_QUESTION_NOT_FOUND));
    }
}
