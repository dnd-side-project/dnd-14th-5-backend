package com.dnd5.timoapi.domain.group.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupReflectionCommentRequest(
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 1000, message = "댓글 내용은 1000자 이하여야 합니다.")
        String content
) {
}
