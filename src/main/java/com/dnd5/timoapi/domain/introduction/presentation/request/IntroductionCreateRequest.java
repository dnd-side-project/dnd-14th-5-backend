package com.dnd5.timoapi.domain.introduction.presentation.request;

import com.dnd5.timoapi.domain.introduction.domain.model.Introduction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IntroductionCreateRequest(
        @NotNull Integer version,
        @NotBlank String content
) {
    public Introduction toModel() {
        return Introduction.create(version, content);
    }
}
