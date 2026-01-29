package com.dnd5.timoapi.domain.test.domain.entity;

import com.dnd5.timoapi.domain.test.domain.model.TestQuestion;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "test_questions")
public class TestQuestionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private TestEntity test;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ZtpiCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int sequence;

    @Column(name = "is_reversed", nullable = false)
    private boolean isReversed;

    public static TestQuestionEntity of(
            TestEntity test,
            ZtpiCategory category,
            String content,
            int sequence,
            boolean isReversed
    ) {
        return new TestQuestionEntity(test, category, content, sequence, isReversed);
    }

    public TestQuestion toModel() {
        return new TestQuestion(
                getId(),
                test.getId(),
                category,
                content,
                sequence,
                isReversed,
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public void update(ZtpiCategory category, String content, int sequence, boolean isReversed) {
        if (category != null) this.category = category;
        if (content != null) this.content = content;
        this.sequence = sequence;
        this.isReversed = isReversed;
    }
}
