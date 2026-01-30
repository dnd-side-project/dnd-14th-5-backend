package com.dnd5.timoapi.domain.introduction.presentation;

import com.dnd5.timoapi.domain.introduction.application.service.IntroductionService;
import com.dnd5.timoapi.domain.introduction.presentation.response.IntroductionResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/introductions")
@RequiredArgsConstructor
@Validated
public class IntroductionController {

    private final IntroductionService introductionService;

    @GetMapping("/{introductionId}")
    public IntroductionResponse findById(@Positive @PathVariable Long introductionId) {
        return introductionService.findById(introductionId);
    }
}
