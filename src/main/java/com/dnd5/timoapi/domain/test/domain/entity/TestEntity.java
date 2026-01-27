package com.dnd5.timoapi.domain.test.domain.entity;

import com.dnd5.timoapi.domain.test.domain.model.Test;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "tests")
public class TestEntity extends BaseEntity {

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    public static TestEntity from(Test model) {
        return new TestEntity(model.type(), model.name(), model.description());
    }

    public Test toModel() {
        return new Test(getId(), getType(), getName(), getDescription(), getCreatedAt());
    }

    public void update(String type, String name, String description) {
        if (type != null) this.type = type;
        if (name != null) this.name = name;
        if (description != null) this.description = description;
    }
}
