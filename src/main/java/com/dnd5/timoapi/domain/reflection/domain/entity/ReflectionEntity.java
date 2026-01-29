package com.dnd5.timoapi.domain.reflection.domain.entity;

import com.dnd5.timoapi.domain.reflection.domain.model.Reflection;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
        name = "reflections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"})
)
public class ReflectionEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "answer_text", nullable = false, columnDefinition = "TEXT")
    private String answerText;

    public static ReflectionEntity from(Reflection model) {
        return new ReflectionEntity(
                model.userId(),
                model.questionId(),
                model.date(),
                model.answerText()
        );
    }

    public Reflection toModel() {
        return new Reflection(
                getId(),
                getUserId(),
                getQuestionId(),
                getDate(),
                getAnswerText(),
                getCreatedAt()
        );
    }
}
