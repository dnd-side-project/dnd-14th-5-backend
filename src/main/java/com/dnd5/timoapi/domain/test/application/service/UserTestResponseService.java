package com.dnd5.timoapi.domain.test.application.service;

import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestResponseEntity;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import com.dnd5.timoapi.domain.test.domain.repository.TestQuestionRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestResponseRepository;
import com.dnd5.timoapi.domain.test.exception.TestQuestionErrorCode;
import com.dnd5.timoapi.domain.test.exception.UserTestRecordErrorCode;
import com.dnd5.timoapi.domain.test.exception.UserTestResponseErrorCode;
import com.dnd5.timoapi.domain.test.presentation.request.UserTestResponseCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.request.UserTestResponseUpdateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestResponseResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTestResponseService {

    private final UserTestRecordRepository userTestRecordRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final UserTestResponseRepository userTestResponseRepository;

    public void create(Long testRecordId, UserTestResponseCreateRequest request) {

        UserTestRecordEntity userTestRecordEntity = userTestRecordRepository.findById(testRecordId)
                .orElseThrow(() -> new BusinessException(UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND));

        Long userId = SecurityUtil.getCurrentUserId();
        if (!userTestRecordEntity.getUser().getId().equals(userId)) {
            Map<String, Object> additional = Map.of(
                    "userTestRecordId", userTestRecordEntity.getId(),
                    "testRecordUserId", userTestRecordEntity.getUser().getId(),
                    "currentUserId", userId
            );
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_NOT_OWNER, additional);
        }

        TestQuestionEntity testQuestionEntity = testQuestionRepository.findById(request.questionId())
                .orElseThrow(() -> new BusinessException(TestQuestionErrorCode.TEST_QUESTION_NOT_FOUND));

        if (!userTestRecordEntity.getTest().getId().equals(testQuestionEntity.getTest().getId())) {
            Map<String, Object> additional = Map.of(
                    "userTestId", userTestRecordEntity.getId(),
                    "questionTestId", testQuestionEntity.getTest().getId()
            );
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_CROSS_RESPONSE, additional);
        }

        Optional<UserTestResponseEntity> userTestResponseEntity =
                userTestResponseRepository.findByUserTestRecordAndTestQuestion(
                        userTestRecordEntity, testQuestionEntity
                );

        if (userTestResponseEntity.isPresent()) {
            Map<String, Object> additional = Map.of(
                    "testResponseId", userTestResponseEntity.get().getId()
            );

            throw new BusinessException(
                    UserTestResponseErrorCode.USER_TEST_ALREADY_RESPONSE,
                    additional
            );
        }

        if (userTestRecordEntity.getStatus() == TestRecordStatus.COMPLETED) {
            Map<String, Object> additional = Map.of(
                    "userTestRecordId", userTestRecordEntity.getId(),
                    "status", userTestRecordEntity.getStatus()
            );
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_ALREADY_COMPLETE, additional);
        }

        userTestResponseRepository.save(UserTestResponseEntity.from(userTestRecordEntity, testQuestionEntity, request.score()));

    }

    public void update(Long testRecordId, Long responseId, @Valid UserTestResponseUpdateRequest request) {
        UserTestRecordEntity userTestRecordEntity = userTestRecordRepository.findById(testRecordId)
                .orElseThrow(() -> new BusinessException(UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND));

        Long userId = SecurityUtil.getCurrentUserId();
        if (!userTestRecordEntity.getUser().getId().equals(userId)) {
            Map<String, Object> additional = Map.of(
                    "userTestRecordId", userTestRecordEntity.getId(),
                    "testRecordUserId", userTestRecordEntity.getUser().getId(),
                    "currentUserId", userId
            );
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_NOT_OWNER, additional);
        }

        UserTestResponseEntity userTestResponseEntity = getUserTestResponseEntity(responseId);

        if (!userTestResponseEntity.getUserTestRecord().getId().equals(testRecordId)) {
            Map<String, Object> additional = Map.of(
                    "testRecordIdInUrl", testRecordId,
                    "responseId", responseId,
                    "responseTestRecordId", userTestResponseEntity.getUserTestRecord().getId()
            );
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_RESPONSE_NOT_BELONG, additional);
        }

        if (userTestRecordEntity.getStatus() == TestRecordStatus.COMPLETED) {
            Map<String, Object> additional = Map.of(
                    "userTestRecordId", userTestRecordEntity.getId(),
                    "status", userTestRecordEntity.getStatus()
            );
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_ALREADY_COMPLETE, additional);
        }

        userTestResponseEntity.update(request.score());
    }

    @Transactional(readOnly = true)
    public List<UserTestResponseResponse> findAll(Long testRecordId) {
        userTestRecordRepository.findById(testRecordId)
                .orElseThrow(() -> new BusinessException(UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND));

        return userTestResponseRepository.findByUserTestRecordId(testRecordId)
                .orElse(List.of())
                .stream()
                .map(UserTestResponseEntity::toModel)
                .map(UserTestResponseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserTestResponseResponse findById(Long testRecordId, Long responseId) {
        userTestRecordRepository.findById(testRecordId)
                .orElseThrow(() -> new BusinessException(UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND));

        return userTestResponseRepository.findByUserTestRecordIdAndId(testRecordId, responseId)
                .map(UserTestResponseEntity::toModel)
                .map(UserTestResponseResponse::from)
                .orElseThrow(() -> new BusinessException(UserTestResponseErrorCode.USER_TEST_RESPONSE_NOT_FOUND));
    }

    private UserTestResponseEntity getUserTestResponseEntity(Long testResponseId) {
        return userTestResponseRepository.findById(testResponseId)
                .orElseThrow(() -> new BusinessException(UserTestResponseErrorCode.USER_TEST_RESPONSE_NOT_FOUND));
    }

}
