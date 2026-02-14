package com.dnd5.timoapi.domain.introduction.presentation.request;

public record IntroductionUpdateRequest(
        Integer version,
        Long sequence,
        String title,
        String description,
        String imageUrl
) {

}
