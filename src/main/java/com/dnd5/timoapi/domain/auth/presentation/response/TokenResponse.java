package com.dnd5.timoapi.domain.auth.presentation.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
