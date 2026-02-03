package com.dnd5.timoapi.domain.reflection.domain.entity;

import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionFeedbackPrompt;
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
@Table(name = "reflection_feedback_prompts")
public class ReflectionFeedbackPromptEntity extends BaseEntity {

    private int version;

    @Column(columnDefinition = "TEXT")
    private String content;

    public static ReflectionFeedbackPromptEntity from(ReflectionFeedbackPrompt model) {
        return new ReflectionFeedbackPromptEntity(
                model.version(),
                model.content()
        );
    }

    public ReflectionFeedbackPrompt toModel() {
        return new ReflectionFeedbackPrompt(
                getId(),
                getVersion(),
                getContent(),
                getCreatedAt()
        );
    }

    public void update(String content) {
        this.content = content;
    }
}
