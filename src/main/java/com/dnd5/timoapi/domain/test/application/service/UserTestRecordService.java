package com.dnd5.timoapi.domain.test.application.service;

import static com.dnd5.timoapi.global.security.context.SecurityUtil.getCurrentUserId;

import com.dnd5.timoapi.domain.reflection.domain.entity.UserReflectionQuestionOrderEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.UserReflectionQuestionOrderRepository;
import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestResponseEntity;
import com.dnd5.timoapi.domain.test.domain.entity.UserTestResultEntity;
import com.dnd5.timoapi.domain.test.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.test.domain.repository.TestQuestionRepository;
import com.dnd5.timoapi.domain.test.domain.repository.TestRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestResponseRepository;
import com.dnd5.timoapi.domain.test.domain.repository.UserTestResultRepository;
import com.dnd5.timoapi.domain.test.exception.TestErrorCode;
import com.dnd5.timoapi.domain.test.exception.TestQuestionErrorCode;
import com.dnd5.timoapi.domain.test.exception.UserTestRecordErrorCode;
import com.dnd5.timoapi.domain.test.exception.UserTestResponseErrorCode;
import com.dnd5.timoapi.domain.test.presentation.request.UserTestRecordCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.TestResultCategoryResponse;
import com.dnd5.timoapi.domain.test.presentation.response.TestResultResponse;
import com.dnd5.timoapi.domain.test.presentation.response.TestResultScoreResponse;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordCreateResponse;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordDetailResponse;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordResponse;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.domain.user.exception.UserErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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
    private final UserReflectionQuestionOrderRepository userReflectionQuestionOrderRepository;

    public UserTestRecordCreateResponse create(UserTestRecordCreateRequest request) {
        Long userId = getCurrentUserId();

        if (userId == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        TestEntity testEntity = testRepository.findById(request.testId())
                .orElseThrow(() -> new BusinessException(TestErrorCode.TEST_NOT_FOUND));

        Optional<UserTestRecordEntity> userTestRecordEntity =
                userTestRecordRepository
                        .findByUserIdAndTestIdAndStatus(
                                userId,
                                testEntity.getId(),
                                TestRecordStatus.IN_PROGRESS);

        if (userTestRecordEntity.isPresent()) {
            Map<String, Object> additional = Map.of(
                    "testRecordId", userTestRecordEntity.get().getId()
            );

            throw new BusinessException(
                    UserTestRecordErrorCode.ALREADY_IN_PROGRESS,
                    additional
            );
        }

        UserTestRecord model = request.toModel();

        UserTestRecordEntity savedEntity =
                userTestRecordRepository.save(
                        UserTestRecordEntity.from(userEntity, testEntity, model));

        return UserTestRecordCreateResponse.from(savedEntity.toModel());
    }

    public UserTestRecordDetailResponse complete(Long testRecordId) {
        UserTestRecordEntity userTestRecordEntity = getUserTestRecordEntity(testRecordId);

        if (userTestRecordEntity.isCompleted()) {
            throw new BusinessException(UserTestRecordErrorCode.ALREADY_COMPLETED);
        }

        List<TestQuestionEntity> testQuestionEntityList = testQuestionRepository.findByTestId(
                        userTestRecordEntity.getTest().getId())
                .orElseThrow(
                        () -> new BusinessException(TestQuestionErrorCode.TEST_QUESTION_NOT_FOUND));

        List<UserTestResponseEntity> userTestResponseEntityList = userTestResponseRepository.findByUserTestRecordId(
                        testRecordId)
                .orElseThrow(() -> new BusinessException(
                        UserTestResponseErrorCode.USER_TEST_RESPONSE_NOT_FOUND));

        validateAllQuestionsAnswered(testQuestionEntityList, userTestResponseEntityList);
        Map<ZtpiCategory, Double> userTestResults = createUserTestResults(userTestRecordEntity,
                userTestResponseEntityList);

        userTestRecordEntity.complete();
        createUserReflectionQuestionOrders(userTestRecordEntity.getUser().getId());

        UserEntity userEntity = userTestRecordEntity.getUser();
        if (!userEntity.getIsOnboarded()) {
            userEntity.completeOnboarding();
        }

        TestResultResponse resultResponse = createTestResultResponse(userTestResults);

        return UserTestRecordDetailResponse.of(
                userTestRecordEntity.toModel(),
                null,
                resultResponse
        );
    }

    private ZtpiCategory calculateClosestCategory(
            Map<ZtpiCategory, Double> userTestResults) {
        return userTestResults.entrySet().stream()
                // 이상 점수보다 큰 것만
                .filter(e -> e.getValue() > e.getKey().getIdealScore())
                // 초과폭 큰 순
                .max(Comparator.comparingDouble(e ->
                        e.getValue() - e.getKey().getIdealScore()
                ))
                .map(Map.Entry::getKey)
                // 전부 이상 이하일 경우 fallback
                .orElseGet(() ->
                        userTestResults.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElseThrow()
                );
    }

    private List<TestResultScoreResponse> createScoreResponse(
            Map<ZtpiCategory, Double> userTestResults) {
        return userTestResults.entrySet().stream()
                .map(entry -> {

                    ZtpiCategory category = entry.getKey();
                    double score = entry.getValue();

                    return new TestResultScoreResponse(
                            category,
                            score,
                            category.getIdealScore()
                    );
                })
                .toList();
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
                userTestResponseRepository.countByUserTestRecordId(testRecordId);

        if (!userTestRecordEntity.isCompleted()) {
            return UserTestRecordDetailResponse.of(
                    userTestRecordEntity.toModel(),
                    progress,
                    null
            );
        }

        List<UserTestResultEntity> resultEntities =
                userTestResultRepository.findAllByUserTestRecordId(testRecordId);

        if (resultEntities.isEmpty()) {
            throw new BusinessException(UserTestRecordErrorCode.USER_TEST_RESULT_NOT_FOUND);
        }

        Map<ZtpiCategory, Double> userTestResults = resultEntities.stream()
                .collect(Collectors.toMap(
                        UserTestResultEntity::getCategory,
                        UserTestResultEntity::getScore
                ));

        TestResultResponse resultResponse = createTestResultResponse(userTestResults);

        return UserTestRecordDetailResponse.of(
                userTestRecordEntity.toModel(),
                null,
                resultResponse
        );
    }

    private TestResultResponse createTestResultResponse(
            Map<ZtpiCategory, Double> userTestResults) {
        List<TestResultScoreResponse> scoreResponses = createScoreResponse(userTestResults);
        ZtpiCategory closestCategory = calculateClosestCategory(userTestResults);

        return new TestResultResponse(
                new TestResultCategoryResponse(
                        closestCategory.name(),
                        closestCategory.getCharacter(),
                        closestCategory.getPersonality(),
                        closestCategory.getDescription()
                ),
                scoreResponses
        );
    }

    private UserTestRecordEntity getUserTestRecordEntity(Long testRecordId) {
        return userTestRecordRepository.findById(testRecordId)
                .orElseThrow(() -> new BusinessException(
                        UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND));
    }

    private void createUserReflectionQuestionOrders(Long userId) {
        for (ZtpiCategory category : ZtpiCategory.values()) {
            if (!userReflectionQuestionOrderRepository.existsByUserIdAndCategory(userId,
                    category)) {
                userReflectionQuestionOrderRepository.save(
                        new UserReflectionQuestionOrderEntity(userId, category, 1L)
                );
            }
        }
    }

    private void validateAllQuestionsAnswered(List<TestQuestionEntity> testQuestionEntityList,
            List<UserTestResponseEntity> userTestResponseEntityList) {
        if (testQuestionEntityList.size() > userTestResponseEntityList.size()) {
            throw new BusinessException(UserTestRecordErrorCode.NOT_ALL_QUESTIONS_ANSWERED);
        }
    }

    private Map<ZtpiCategory, Double> calculateCategoryAverages(
            List<UserTestResponseEntity> userTestResponseEntityList) {
        return userTestResponseEntityList.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTestQuestion().getCategory(),
                        Collectors.averagingDouble(UserTestResponseEntity::getScore)
                ));
    }

    @Transactional
    public Map<ZtpiCategory, Double> createUserTestResults(
            UserTestRecordEntity userTestRecordEntity,
            List<UserTestResponseEntity> userTestResponseEntityList) {
        Map<ZtpiCategory, Double> userTestCategoryAverages = calculateCategoryAverages(
                userTestResponseEntityList);

        List<UserTestResultEntity> userTestResultEntityList = userTestCategoryAverages.entrySet()
                .stream()
                .map(entry -> UserTestResultEntity.from(userTestRecordEntity, entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
        userTestResultRepository.saveAll(userTestResultEntityList);

        return userTestCategoryAverages;
    }
}
