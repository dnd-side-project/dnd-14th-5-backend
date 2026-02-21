package com.dnd5.timoapi.domain.user.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
        @NotBlank
        @NotEmpty
        @Size(max = 20)
        String name
) {

}
