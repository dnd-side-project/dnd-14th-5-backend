package com.dnd5.timoapi.domain.test.application.service;

import static com.dnd5.timoapi.global.security.context.SecurityUtil.getCurrentUserId;

import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestResponseEntity;
import com.dnd5.timoapi.domain.test.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.test.domain.repository.TestQuestionRepository;
import com.dnd5.timoapi.domain.test.domain.repository.TestRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestResponseRepository;
import com.dnd5.timoapi.domain.test.exception.TestErrorCode;
import com.dnd5.timoapi.domain.test.exception.UserTestRecordErrorCode;
import com.dnd5.timoapi.domain.test.exception.UserTestResponseErrorCode;
import com.dnd5.timoapi.domain.test.presentation.request.UserTestRecordCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordCreateResponse;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordDetailResponse;
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
public class UserTestResponseService {

    private final UserTestRecordRepository userTestRecordRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final UserTestResponseRepository userTestResponseRepository;

    private UserTestResponseEntity getUserTestResponseEntity(Long testResponseId) {
        return userTestResponseRepository.findById(testResponseId)
                .orElseThrow(() -> new BusinessException(UserTestResponseErrorCode.USER_TEST_RESPONSE_NOT_FOUND));
    }

}
