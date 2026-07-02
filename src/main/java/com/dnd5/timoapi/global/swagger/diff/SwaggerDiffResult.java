package com.dnd5.timoapi.global.swagger.diff;

import java.util.List;

public record SwaggerDiffResult(
        List<EndpointDetail> added,
        List<EndpointDetail> removed,
        List<EndpointDetail> changed
) {
    public record EndpointDetail(
            String key,
            String summary,
            String requestExample,
            String responseExample,
            String previousRequestExample,
            String previousResponseExample
    ) {}

    public boolean hasChanges() {
        return !added.isEmpty() || !removed.isEmpty() || !changed.isEmpty();
    }
}
