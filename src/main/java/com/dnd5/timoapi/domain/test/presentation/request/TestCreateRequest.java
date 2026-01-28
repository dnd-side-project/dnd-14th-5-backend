package com.dnd5.timoapi.domain.test.presentation.request;

import com.dnd5.timoapi.domain.test.domain.model.Test;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestType;
import jakarta.validation.constraints.NotBlank;

public record TestCreateRequest(
        @NotBlank
        TestType type,
        @NotBlank
        String name,
        String description
) {
    public Test toModel() {
        return Test.create(type, name, description);
    }
}
