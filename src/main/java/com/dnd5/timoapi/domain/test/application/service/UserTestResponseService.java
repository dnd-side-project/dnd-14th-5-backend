package com.dnd5.timoapi.domain.test.application.service;

import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestResponseEntity;
import com.dnd5.timoapi.domain.test.domain.model.UserTestResponse;
import com.dnd5.timoapi.domain.test.domain.repository.TestQuestionRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestResponseRepository;
import com.dnd5.timoapi.domain.test.exception.TestQuestionErrorCode;
import com.dnd5.timoapi.domain.test.exception.UserTestRecordErrorCode;
import com.dnd5.timoapi.domain.test.exception.UserTestResponseErrorCode;
import com.dnd5.timoapi.domain.test.presentation.request.UserTestResponseCreateRequest;
import com.dnd5.timoapi.global.exception.BusinessException;
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

        TestQuestionEntity testQuestionEntity = testQuestionRepository.findById(request.questionId())
                .orElseThrow(() -> new BusinessException(TestQuestionErrorCode.TEST_QUESTION_NOT_FOUND));

        UserTestResponse model = request.toModel(testRecordId);

        userTestResponseRepository.save(UserTestResponseEntity.from(userTestRecordEntity, testQuestionEntity, model));

    }

    private UserTestResponseEntity getUserTestResponseEntity(Long testResponseId) {
        return userTestResponseRepository.findById(testResponseId)
                .orElseThrow(() -> new BusinessException(UserTestResponseErrorCode.USER_TEST_RESPONSE_NOT_FOUND));
    }

}
