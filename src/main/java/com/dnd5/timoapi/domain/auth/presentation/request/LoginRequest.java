package com.dnd5.timoapi.domain.auth.presentation.request;

import com.dnd5.timoapi.domain.user.domain.model.enums.OAuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank
        @Email
        String email,
        @NotNull
        OAuthProvider provider
) {
}
