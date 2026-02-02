package com.dnd5.timoapi.domain.test.domain.entity;

import com.dnd5.timoapi.domain.test.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.test.domain.model.UserTestResponse;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "user_test_record_responses")
public class UserTestResponseEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_record_id", nullable = false)
    private UserTestRecordEntity userTestRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestionEntity testQuestion;

    @Column(name = "answer_score", nullable = false)
    private int answerScore;

    public static UserTestResponseEntity from(
            UserTestRecordEntity userTestRecord,
            TestQuestionEntity testQuestion,
            UserTestResponse model
    ) {
        return new UserTestResponseEntity(
                userTestRecord,
                testQuestion,
                model.answerScore()
        );
    }

    public UserTestResponse toModel() {
        return new UserTestResponse(
                getId(),
                userTestRecord.getId(),
                testQuestion.getId(),
                getAnswerScore(),
                getCreatedAt(),
                getUpdatedAt()
        );
    }

}
