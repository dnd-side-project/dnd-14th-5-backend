package com.dnd5.timoapi.domain.test.domain.model;

import com.dnd5.timoapi.domain.test.domain.model.enums.TestType;
import java.time.LocalDateTime;

public record Test(
        Long id,
        TestType type,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static Test create(TestType type, String name, String description) {
        return new Test(null, type, name, description, null);
    }
}
