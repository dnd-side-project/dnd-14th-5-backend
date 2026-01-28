package com.dnd5.timoapi.domain.test.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.Test;

import com.dnd5.timoapi.domain.test.domain.model.enums.TestType;
import java.time.LocalDateTime;
import java.util.List;

public record TestListResponse(
        List<TestResponse> tests
) {
    public static TestListResponse from(List<Test> models) {
        return new TestListResponse(
                models.stream().map(TestResponse::from).toList()
        );
    }

    public record TestResponse(
            Long id,
            TestType type,
            String name,
            String description,
            LocalDateTime createdAt
    ) {
        public static TestResponse from(Test model) {
            return new TestResponse(
                    model.id(),
                    model.type(),
                    model.name(),
                    model.description(),
                    model.createdAt()
            );
        }
    }
}
