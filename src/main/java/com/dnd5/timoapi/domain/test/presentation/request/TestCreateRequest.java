package com.dnd5.timoapi.domain.test.presentation.request;

import com.dnd5.timoapi.domain.test.domain.model.Test;
import jakarta.validation.constraints.NotBlank;

public record TestCreateRequest(
        @NotBlank
        String type,
        @NotBlank
        String name,
        String description
) {
    public Test toModel() {
        return Test.create(type, name, description);
    }
}
