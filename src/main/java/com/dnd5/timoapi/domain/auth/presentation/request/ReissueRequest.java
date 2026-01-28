package com.dnd5.timoapi.domain.auth.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(
        @NotBlank
        String refreshToken
) {
}
