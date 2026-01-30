package com.dnd5.timoapi.domain.introduction.domain.entity;

import com.dnd5.timoapi.domain.introduction.domain.model.Introduction;
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
@Table(name = "service_introductions")
public class IntroductionEntity extends BaseEntity {

    @Column(nullable = false)
    private int version;

    @Column(columnDefinition = "TEXT")
    private String content;

    public static IntroductionEntity from(Introduction model) {
        return new IntroductionEntity(model.version(), model.content());
    }

    public Introduction toModel() {
        return new Introduction(getId(), version, content, getCreatedAt());
    }

    public void update(Integer version, String content) {
        if (version != null) this.version = version;
        if (content != null) this.content = content;
    }
}
