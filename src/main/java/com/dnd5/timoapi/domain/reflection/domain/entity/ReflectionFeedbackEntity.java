package com.dnd5.timoapi.domain.reflection.domain.entity;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedback;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
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

    public static ReflectionFeedbackEntity from(ReflectionFeedback model) {
        return new ReflectionFeedbackEntity(
                model.reflectionId(),
                model.score(),
                model.content(),
                model.status()
        );
    }

    public ReflectionFeedback toModel() {
        return new ReflectionFeedback(
                getId(),
                getReflectionId(),
                getScore(),
                getContent(),
                getStatus(),
                getCreatedAt()
        );
    }

    public void complete(int score, String content) {
        this.score = score;
        this.content = content;
        this.status = FeedbackStatus.COMPLETED;
    }

    public void fail() {
        this.status = FeedbackStatus.FAILED;
    }
}
