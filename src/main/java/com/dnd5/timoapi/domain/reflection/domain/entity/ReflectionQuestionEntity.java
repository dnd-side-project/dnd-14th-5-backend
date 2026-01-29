package com.dnd5.timoapi.domain.reflection.domain.entity;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionQuestion;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
        name = "reflection_questions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sequence", "category"})
)
public class ReflectionQuestionEntity extends BaseEntity {

    @Column(nullable = false)
    private Long sequence;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ZtpiCategory category;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    public static ReflectionQuestionEntity from(ReflectionQuestion model) {
        return new ReflectionQuestionEntity(
                model.sequence(),
                model.category(),
                model.content(),
                model.createdBy()
        );
    }

    public ReflectionQuestion toModel() {
        return new ReflectionQuestion(
                getId(),
                getSequence(),
                getCategory(),
                getContent(),
                getCreatedBy(),
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public void update(String content, String createdBy) {
        if (content != null) {
            this.content = content;
        }
        if (createdBy != null) {
            this.createdBy = createdBy;
        }
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public void decreaseSequence() {
        this.sequence--;
    }
}
