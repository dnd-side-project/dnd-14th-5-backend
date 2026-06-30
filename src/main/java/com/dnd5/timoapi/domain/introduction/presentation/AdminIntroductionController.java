package com.dnd5.timoapi.domain.introduction.presentation;

import com.dnd5.timoapi.domain.introduction.application.service.IntroductionService;
import com.dnd5.timoapi.domain.introduction.presentation.request.IntroductionCreateRequest;
import com.dnd5.timoapi.domain.introduction.presentation.request.IntroductionUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/introductions")
@RequiredArgsConstructor
@Validated
public class AdminIntroductionController {

    private final IntroductionService introductionService;

    @Operation(summary = "온보딩 콘텐츠 생성 (어드민)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody IntroductionCreateRequest request) {
        introductionService.create(request);
    }

    @Operation(summary = "온보딩 콘텐츠 수정 (어드민)")
    @PatchMapping("/{introductionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @Positive @PathVariable Long introductionId,
            @Valid @RequestBody IntroductionUpdateRequest request
    ) {
        introductionService.update(introductionId, request);
    }

    @Operation(summary = "온보딩 콘텐츠 삭제 (어드민)")
    @DeleteMapping("/{introductionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long introductionId) {
        introductionService.delete(introductionId);
    }
}
