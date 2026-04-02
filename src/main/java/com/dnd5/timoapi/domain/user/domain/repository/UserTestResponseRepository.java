package com.dnd5.timoapi.domain.user.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestResponseEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTestResponseRepository extends JpaRepository<UserTestResponseEntity, Long> {

    int countByUserTestRecordId(Long testRecordId);
    Optional<List<UserTestResponseEntity>> findByUserTestRecordId(Long testRecordId);
    Optional<UserTestResponseEntity> findByUserTestRecordIdAndId(Long testRecordId, Long id);
    Optional<UserTestResponseEntity> findByUserTestRecordAndTestQuestion(
            UserTestRecordEntity record,
            TestQuestionEntity question
    );
    Optional<UserTestResponseEntity> findByUserTestRecordIdAndIdAndDeletedAtIsNull(Long testRecordId, Long responseId);
    List<UserTestResponseEntity> findAllByUserTestRecordIdAndDeletedAtIsNull(Long testRecordId);
    List<UserTestResponseEntity> findByUserTestRecordIdAndDeletedAtIsNull(Long testRecordId);

    Optional<UserTestResponseEntity> findByIdAndDeletedAtIsNull(Long testResponseId);

    int countByUserTestRecordIdAndDeletedAtIsNull(Long testRecordId);
}
