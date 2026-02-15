package com.dnd5.timoapi.domain.introduction.presentation.request;

import com.dnd5.timoapi.domain.introduction.domain.model.Introduction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IntroductionCreateRequest(
        @NotNull Integer version,
        @NotNull @Positive Long sequence,
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String imageUrl
) {

    public Introduction toModel() {
        return Introduction.create(version, sequence, title, description, imageUrl);
    }
}
