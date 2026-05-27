package com.dnd5.timoapi.domain.reflection.domain.entity;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedback;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "reflection_feedbacks")
public class ReflectionFeedbackEntity extends BaseEntity {

    @Column(name = "reflection_id", nullable = false, unique = true)
    private Long reflectionId;

    private int score;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedbackStatus status;

    @Enumerated(EnumType.STRING)
    private ZtpiCategory category;

    private Boolean isIncreased;

    private Double changedScore;

    private Double beforeScore;

    private Double afterScore;

    public static ReflectionFeedbackEntity from(ReflectionFeedback model) {
        return new ReflectionFeedbackEntity(
                model.reflectionId(),
                model.score(),
                model.content(),
                model.status(),
                model.category(),
                model.isIncreased(),
                model.changedScore(),
                model.beforeScore(),
                model.afterScore()
        );
    }

    public ReflectionFeedback toModel() {
        return new ReflectionFeedback(
                getId(),
                getReflectionId(),
                getScore(),
                getContent(),
                getStatus(),
                getCategory(),
                getIsIncreased(),
                getChangedScore(),
                getBeforeScore(),
                getAfterScore(),
                getCreatedAt()
        );
    }

    public void complete(int score, String content, ZtpiCategory category, Boolean isIncreased, Double changedScore, Double beforeScore, Double afterScore) {
        this.score = score;
        this.content = content;
        this.status = FeedbackStatus.COMPLETED;
        this.category = category;
        this.isIncreased = isIncreased;
        this.changedScore = changedScore;
        this.beforeScore = beforeScore;
        this.afterScore = afterScore;
    }

    public void fail() {
        this.status = FeedbackStatus.FAILED;
    }

    public void restartProcessing() {
        this.score = 0;
        this.content = null;
        this.status = FeedbackStatus.PROCESSING;
        this.category = null;
        this.isIncreased = null;
        this.changedScore = null;
        this.beforeScore = null;
        this.afterScore = null;
    }
}
