package com.dnd5.timoapi.domain.user.application.service;

import static com.dnd5.timoapi.global.security.context.SecurityUtil.getCurrentUserId;

import com.dnd5.timoapi.domain.reflection.domain.entity.UserReflectionQuestionOrderEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.UserReflectionQuestionOrderRepository;
import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestResponseEntity;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestResultEntity;
import com.dnd5.timoapi.domain.user.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.user.domain.model.enums.UserTestRecordStatus;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.test.domain.repository.TestQuestionRepository;
import com.dnd5.timoapi.domain.test.domain.repository.TestRepository;
import com.dnd5.timoapi.domain.user.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.user.domain.repository.UserTestResponseRepository;
import com.dnd5.timoapi.domain.user.domain.repository.UserTestResultRepository;
import com.dnd5.timoapi.domain.test.exception.TestErrorCode;
import com.dnd5.timoapi.domain.test.exception.TestQuestionErrorCode;
import com.dnd5.timoapi.domain.user.exception.UserTestRecordErrorCode;
import com.dnd5.timoapi.domain.user.exception.UserTestResponseErrorCode;
import com.dnd5.timoapi.domain.user.presentation.request.UserTestRecordCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.TestResultCategoryResponse;
import com.dnd5.timoapi.domain.test.presentation.response.TestResultResponse;
import com.dnd5.timoapi.domain.test.presentation.response.TestResultScoreResponse;
import com.dnd5.timoapi.domain.user.presentation.response.UserTestRecordCreateResponse;
import com.dnd5.timoapi.domain.user.presentation.response.UserTestRecordDetailResponse;
import com.dnd5.timoapi.domain.user.presentation.response.UserTestRecordResponse;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.domain.user.exception.UserErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        UserEntity userEntity = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        TestEntity testEntity = testRepository.findByIdAndDeletedAtIsNull(request.testId())
                .orElseThrow(() -> new BusinessException(TestErrorCode.TEST_NOT_FOUND));

        Optional<UserTestRecordEntity> userTestRecordEntity =
                userTestRecordRepository
                        .findByUserIdAndTestIdAndStatusAndDeletedAtIsNull(
                                userId,
                                testEntity.getId(),
                                UserTestRecordStatus.IN_PROGRESS);

        if (userTestRecordEntity.isPresent()) {
            return UserTestRecordCreateResponse.from(userTestRecordEntity.get().toModel(), true);
        }

        UserTestRecord model = request.toModel();

        UserTestRecordEntity savedEntity =
                userTestRecordRepository.save(
                        UserTestRecordEntity.from(userEntity, testEntity, model));

        return UserTestRecordCreateResponse.from(savedEntity.toModel(), false);
    }

    public UserTestRecordDetailResponse complete(Long testRecordId) {
        UserTestRecordEntity userTestRecordEntity = getUserTestRecordEntity(testRecordId);
        Long userId = userTestRecordEntity.getUser().getId();
        log.info("test_complete_start testRecordId={} userId={}", testRecordId, userId);

        if (userTestRecordEntity.isCompleted()) {
            throw new BusinessException(UserTestRecordErrorCode.ALREADY_COMPLETED);
        }

        validateUserTestRecordOwnership(userTestRecordEntity);

        List<TestQuestionEntity> testQuestionEntityList = testQuestionRepository.findByTestIdAndDeletedAtIsNull(
                        userTestRecordEntity.getTest().getId());
        if (testQuestionEntityList.isEmpty()) {
            throw new BusinessException(TestQuestionErrorCode.TEST_QUESTION_NOT_FOUND);
        }

        List<UserTestResponseEntity> userTestResponseEntityList = userTestResponseRepository.findByUserTestRecordIdAndDeletedAtIsNull(
                        testRecordId);
        if (userTestResponseEntityList.isEmpty()) {
            throw new BusinessException(UserTestResponseErrorCode.USER_TEST_RESPONSE_NOT_FOUND);
        }

        validateNoDuplicateQuestionsAnswered(userTestResponseEntityList);

        validateAllQuestionsAnswered(testQuestionEntityList, userTestResponseEntityList);
        Map<ZtpiCategory, Double> userTestResults = createUserTestResults(userTestRecordEntity,
                userTestResponseEntityList);
        log.info("test_complete_done testRecordId={} userId={} scores={}", testRecordId, userId, userTestResults);

        userTestRecordEntity.complete();

        UserEntity userEntity = userTestRecordEntity.getUser();

        ZtpiCategory userMaxCategory = calculateClosestCategory(userTestResults);
        userEntity.updateZtpiCategory(userMaxCategory);

        createUserReflectionQuestionOrders(userTestRecordEntity.getUser().getId());

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

    @Transactional
    public void delete(Long testRecordId) {
        UserTestRecordEntity userTestRecordEntity = userTestRecordRepository.findByIdAndDeletedAtIsNull(testRecordId)
                .orElseThrow(() -> new BusinessException(UserTestRecordErrorCode.USER_TEST_RECORD_NOT_FOUND));

        List<UserTestResponseEntity> userTestResponseEntityList =
                userTestResponseRepository.findAllByUserTestRecordIdAndDeletedAtIsNull(testRecordId);

        userTestResponseEntityList.forEach(UserTestResponseEntity::softDelete);

        List<UserTestResultEntity> userTestResultEntityList =
                userTestResultRepository.findAllByUserTestRecordIdAndDeletedAtIsNull(testRecordId);

        userTestResultEntityList.forEach(UserTestResultEntity::softDelete);

        userTestRecordEntity.softDelete();
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

        return userTestRecordRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
                .map(UserTestRecordEntity::toModel)
                .map(UserTestRecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserTestRecordDetailResponse findById(Long testRecordId) {
        UserTestRecordEntity userTestRecordEntity = getUserTestRecordEntity(testRecordId);
        int progress =
                userTestResponseRepository.countByUserTestRecordIdAndDeletedAtIsNull(testRecordId);

        if (!userTestRecordEntity.isCompleted()) {
            return UserTestRecordDetailResponse.of(
                    userTestRecordEntity.toModel(),
                    progress,
                    null
            );
        }

        List<UserTestResultEntity> resultEntities =
                userTestResultRepository.findAllByUserTestRecordIdAndDeletedAtIsNull(testRecordId);

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
        return userTestRecordRepository.findByIdAndDeletedAtIsNull(testRecordId)
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

    private void validateNoDuplicateQuestionsAnswered(List<UserTestResponseEntity> responses) {
        long distinctCount = responses.stream()
                .map(response -> response.getTestQuestion().getId())
                .distinct()
                .count();

        if (distinctCount != responses.size()) {
            throw new BusinessException(UserTestRecordErrorCode.DUPLICATE_QUESTION_RESPONSE);
        }
    }

    private void validateUserTestRecordOwnership(UserTestRecordEntity record) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!record.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(UserTestRecordErrorCode.USER_TEST_NOT_OWNER,
                    record.getId(), record.getUser().getId(), currentUserId);
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

    public ZtpiCategory getUserTestRecordMaxCategory(Long testRecordId) {

        List<UserTestResultEntity> results =
                userTestResultRepository.findAllByUserTestRecordIdAndDeletedAtIsNull(testRecordId);

        if (results.isEmpty()) {
            throw new BusinessException(UserTestRecordErrorCode.USER_TEST_RESULT_NOT_FOUND);
        }

        return results.stream()
                .max(Comparator.comparing(UserTestResultEntity::getScore))
                .orElseThrow(() -> new BusinessException(
                        UserTestRecordErrorCode.USER_TEST_RESULT_NOT_FOUND))
                .getCategory();
    }
}
