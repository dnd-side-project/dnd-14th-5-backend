package com.dnd5.timoapi.domain.test.application.service;

import static com.dnd5.timoapi.global.security.context.SecurityUtil.getCurrentUserId;

import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestResponseEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestResultEntity;
import com.dnd5.timoapi.domain.test.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.test.domain.repository.TestQuestionRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.test.domain.repository.TestRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestResponseRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestResultRepository;
import com.dnd5.timoapi.domain.test.exception.TestErrorCode;
import com.dnd5.timoapi.domain.test.exception.TestQuestionErrorCode;
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
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTestRecordService {

    private final UserRepository userRepository;
    private final TestRepository testRepository;

    private final TestQuestionRepository testQuestionRepository;

    private final UserTestResponseRepository userTestResponseRepository;
    private final UserTestResultRepository userTestResultRepository;

    private final UserTestRecordRepository userTestRecordRepository;

    public UserTestRecordCreateResponse create(UserTestRecordCreateRequest request) {
        Long userId = getCurrentUserId();

        if (userId == null) {
             throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        UserEntity userEntity = userRepository.findById(userId)
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

        if (userTestRecordEntity.isCompleted()) {
            throw new BusinessException(UserTestRecordErrorCode.ALREADY_COMPLETED);
        }

        List<TestQuestionEntity> testQuestionEntityList = testQuestionRepository.findByTestId(userTestRecordEntity.getTest().getId())
                .orElseThrow(() -> new BusinessException(TestQuestionErrorCode.TEST_QUESTION_NOT_FOUND));

        List<UserTestResponseEntity> userTestResponseEntityList = userTestResponseRepository.findByUserTestRecordId(testRecordId)
                .orElseThrow(() -> new BusinessException(UserTestResponseErrorCode.USER_TEST_RESPONSE_NOT_FOUND));

        validateAllQuestionsAnswered(testQuestionEntityList, userTestResponseEntityList);
        createUserTestResults(userTestRecordEntity, userTestResponseEntityList);

        userTestRecordEntity.complete();
    }

    @Transactional(readOnly = true)
    public List<UserTestRecordResponse> findAll() {
        Long userId = getCurrentUserId();

        if (userId == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        return userTestRecordRepository.findByUserId(userId).stream()
                .map(UserTestRecordEntity::toModel)
                .map(UserTestRecordResponse::from)
                .toList();

    }

    @Transactional(readOnly = true)
    public UserTestRecordDetailResponse findById(Long testRecordId) {
        UserTestRecordEntity userTestRecordEntity = getUserTestRecordEntity(testRecordId);
        int progress =
                userTestResponseRepository
                        .countByUserTestRecordId(testRecordId);

        return UserTestRecordDetailResponse.of(
                userTestRecordEntity.toModel(),
                progress
        );
    }

    private UserTestRecordEntity getUserTestRecordEntity(Long testRecordId) {
        return userTestRecordRepository.findById(testRecordId)
                .orElseThrow(() -> new BusinessException(UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND));
    }

    private void validateAllQuestionsAnswered(List<TestQuestionEntity> testQuestionEntityList, List<UserTestResponseEntity> userTestResponseEntityList) {
        if (testQuestionEntityList.size() > userTestResponseEntityList.size()) {
            throw new BusinessException(UserTestRecordErrorCode.NOT_ALL_QUESTIONS_ANSWERED);
        }
    }

    private Map<ZtpiCategory, Double> calculateCategoryAverages(List<UserTestResponseEntity> userTestResponseEntityList) {
        return userTestResponseEntityList.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTestQuestion().getCategory(),
                        Collectors.averagingDouble(UserTestResponseEntity::getScore)
                ));
    }

    @Transactional
    public void createUserTestResults(UserTestRecordEntity userTestRecordEntity, List<UserTestResponseEntity> userTestResponseEntityList) {
        Map<ZtpiCategory, Double> userTestCategoryAverages = calculateCategoryAverages(userTestResponseEntityList);

        userTestCategoryAverages.forEach((category, score) -> {
            UserTestResultEntity userTestResultEntity = UserTestResultEntity.from(userTestRecordEntity, category, score);
            userTestResultRepository.save(userTestResultEntity);
        });
    }

}
