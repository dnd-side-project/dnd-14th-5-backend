package com.dnd5.timoapi.domain.test.domain.entity;

import com.dnd5.timoapi.domain.test.domain.model.UserTestResponse;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
        name = "user_test_record_responses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_test_record_question",
                        columnNames = {"test_record_id", "question_id"}
                )
        }
)
public class UserTestResponseEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_record_id", nullable = false)
    private UserTestRecordEntity userTestRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestionEntity testQuestion;

    @Column(name = "answer_score", nullable = false)
    @Min(value = 1, message = "테스트 답변 점수는 최소 1점이어야 합니다.")
    @Max(value = 5, message = "테스트 답변 점수는 최대 5점이어야 합니다.")
    private int answerScore;

    public static UserTestResponseEntity from(
            UserTestRecordEntity userTestRecord,
            TestQuestionEntity testQuestion,
            int answerScore
    ) {
        return new UserTestResponseEntity(
                userTestRecord,
                testQuestion,
                answerScore
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

    public void update(int score) {
        this.answerScore = score;
    }

    public double getScore() { return (double) this.answerScore; }
}
