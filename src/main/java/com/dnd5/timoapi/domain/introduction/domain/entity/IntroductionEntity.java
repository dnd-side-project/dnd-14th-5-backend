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

    @Column(nullable = false)
    Long sequence;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    String imageUrl;

    public static IntroductionEntity from(Introduction model) {
        return new IntroductionEntity(
                model.version(),
                model.sequence(),
                model.title(),
                model.description(),
                model.imageUrl()
        );
    }

    public Introduction toModel() {
        return new Introduction(
                getId(),
                version,
                sequence,
                title,
                description,
                imageUrl,
                getCreatedAt()
        );
    }

    public void update(
            Integer version,
            Long sequence,
            String title,
            String description,
            String imageUrl
    ) {
        if (version != null) this.version = version;
        if (sequence != null) this.sequence = sequence;
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }
}
