package com.dnd5.timoapi.domain.introduction.presentation;

import com.dnd5.timoapi.domain.introduction.application.service.IntroductionService;
import com.dnd5.timoapi.domain.introduction.presentation.request.IntroductionCreateRequest;
import com.dnd5.timoapi.domain.introduction.presentation.request.IntroductionUpdateRequest;
import com.dnd5.timoapi.domain.introduction.presentation.response.IntroductionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/introductions")
@RequiredArgsConstructor
@Validated
public class AdminIntroductionController {

    private final IntroductionService introductionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody IntroductionCreateRequest request) {
        introductionService.create(request);
    }

    @GetMapping
    public List<IntroductionResponse> findAll() {
        return introductionService.findAll();
    }

    @PatchMapping("/{introductionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @Positive @PathVariable Long introductionId,
            @Valid @RequestBody IntroductionUpdateRequest request
    ) {
        introductionService.update(introductionId, request);
    }

    @DeleteMapping("/{introductionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long introductionId) {
        introductionService.delete(introductionId);
    }
}
