package com.dnd5.timoapi.domain.test.presentation.request;

public record TestUpdateRequest(
        String type,
        String name,
        String description
) {
}
