package com.dnd5.timoapi.domain.reflection.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReflectionCreateRequest(
        @NotBlank(message = "회고 내용은 필수입니다.")
        @Size(max = 10000, message = "회고 내용은 10000자 이하여야 합니다.")
        String content
) {

}
