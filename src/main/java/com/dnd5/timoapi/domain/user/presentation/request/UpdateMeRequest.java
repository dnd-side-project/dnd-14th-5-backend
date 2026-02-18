package com.dnd5.timoapi.domain.user.presentation.request;

import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
        @Size(max = 20) String name
) {

}
