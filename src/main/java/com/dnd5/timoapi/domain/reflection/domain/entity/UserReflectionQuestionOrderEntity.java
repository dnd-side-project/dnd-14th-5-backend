package com.dnd5.timoapi.domain.reflection.domain.entity;

import com.dnd5.timoapi.domain.reflection.domain.model.UserReflectionQuestionOrder;
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
        name = "user_reflection_question_orders",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category"})
)
public class UserReflectionQuestionOrderEntity extends BaseEntity { // TODO 로그인시 전부 1로 생성, 스케쥴링으로 오늘의 질문 카테고리 1 증가

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ZtpiCategory category;

    @Column(nullable = false)
    private Long sequence;

    public static UserReflectionQuestionOrderEntity from(UserReflectionQuestionOrder model) {
        return new UserReflectionQuestionOrderEntity(
                model.userId(),
                model.category(),
                model.sequence()
        );
    }

    public UserReflectionQuestionOrder toModel() {
        return new UserReflectionQuestionOrder(
                getId(),
                getUserId(),
                getCategory(),
                getSequence()
        );
    }

    public void incrementSequence() {
        this.sequence++;
    }
}
