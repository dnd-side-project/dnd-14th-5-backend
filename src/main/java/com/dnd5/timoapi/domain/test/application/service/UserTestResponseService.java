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

        validateUserTestRecordOwnership(userTestRecordEntity);
        validateTestRecordAlreadyCompleted(userTestRecordEntity);

        TestQuestionEntity testQuestionEntity = testQuestionRepository.findByIdAndDeletedAtIsNull(request.questionId())
                .orElseThrow(() -> new BusinessException(TestQuestionErrorCode.TEST_QUESTION_NOT_FOUND));

        if (!userTestRecordEntity.getTest().getId().equals(testQuestionEntity.getTest().getId())) {
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_CROSS_RESPONSE,
                    userTestRecordEntity.getId(), testQuestionEntity.getTest().getId());
        }

        Optional<UserTestResponseEntity> userTestResponseEntity =
                userTestResponseRepository.findByUserTestRecordAndTestQuestion(
                        userTestRecordEntity, testQuestionEntity
                );

        if (userTestResponseEntity.isPresent()) {
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_ALREADY_RESPONSE,
                    userTestResponseEntity.get().getId());
        }

        userTestResponseRepository.save(UserTestResponseEntity.from(userTestRecordEntity, testQuestionEntity, request.score()));
    }


    public void update(Long testRecordId, Long responseId, @Valid UserTestResponseUpdateRequest request) {
        UserTestRecordEntity userTestRecordEntity = userTestRecordRepository.findById(testRecordId)
                .orElseThrow(() -> new BusinessException(UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND));

        validateUserTestRecordOwnership(userTestRecordEntity);
        validateTestRecordAlreadyCompleted(userTestRecordEntity);

        UserTestResponseEntity userTestResponseEntity = getUserTestResponseEntity(responseId);
        validateResponseBelongsToRecord(testRecordId, userTestResponseEntity);

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

    private void validateUserTestRecordOwnership(UserTestRecordEntity record) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!record.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_NOT_OWNER,
                    record.getId(), record.getUser().getId(), currentUserId);
        }
    }

    private void validateTestRecordAlreadyCompleted(UserTestRecordEntity record) {
        if (record.getStatus() == TestRecordStatus.COMPLETED) {
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_ALREADY_COMPLETE,
                    record.getId(), record.getStatus());
        }
    }

    private void validateResponseBelongsToRecord(Long testRecordId, UserTestResponseEntity response) {
        if (!response.getUserTestRecord().getId().equals(testRecordId)) {
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_RESPONSE_NOT_BELONG,
                    testRecordId, response.getId(), response.getUserTestRecord().getId());
        }
    }


}
