package com.dnd5.timoapi.domain.test.domain.entity;

import com.dnd5.timoapi.domain.test.domain.model.Test;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestType;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "tests")
public class TestEntity extends BaseEntity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TestType type;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(
            mappedBy = "test",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<TestQuestionEntity> testQuestions;

    public static TestEntity from(Test model) {
        return new TestEntity(model.type(), model.name(), model.description(), null);
    }

    public Test toModel() {
        return new Test(getId(), getType(), getName(), getDescription(), getCreatedAt());
    }

    public void update(TestType type, String name, String description) {
        if (type != null) this.type = type;
        if (name != null) this.name = name;
        if (description != null) this.description = description;
    }
}
