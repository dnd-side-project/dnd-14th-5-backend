package com.dnd5.timoapi.domain.introduction.presentation.request;

public record IntroductionUpdateRequest(
        Integer version,
        String content
) {}
