package com.dnd5.timoapi.domain.test.domain.model;

import java.time.LocalDateTime;

public record Test(
        Long id,
        String type,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static Test create(String type, String name, String description) {
        return new Test(null, type, name, description, null);
    }
}
