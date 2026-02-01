package com.dnd5.timoapi.domain.test.application.service;

import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.test.domain.repository.TestRepository;
import com.dnd5.timoapi.domain.test.presentation.response.TestQuestionResponse;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordResponse;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
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

    @Transactional(readOnly = true)
    public List<UserTestRecordResponse> findAll(Long userId, Long testId) {


        return testQuestionRepository.findByTestIdOrderBySequenceAsc(testId).stream()
                .map(TestQuestionEntity::toModel)
                .map(TestQuestionResponse::from)
                .toList();
    }

}
