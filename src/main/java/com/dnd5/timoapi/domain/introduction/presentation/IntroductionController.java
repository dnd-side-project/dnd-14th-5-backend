package com.dnd5.timoapi.domain.introduction.presentation;

import com.dnd5.timoapi.domain.introduction.application.service.IntroductionService;
import com.dnd5.timoapi.domain.introduction.presentation.response.IntroductionResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/introductions")
@RequiredArgsConstructor
@Validated
public class IntroductionController {

    private final IntroductionService introductionService;

    @Operation(summary = "온보딩 콘텐츠 목록 조회")
    @GetMapping
    public List<IntroductionResponse> findAll(@RequestParam int version) {
        return introductionService.findAllByVersion(version);
    }

    @Operation(summary = "온보딩 콘텐츠 단건 조회")
    @GetMapping("/{introductionId}")
    public IntroductionResponse findById(@Positive @PathVariable Long introductionId) {
        return introductionService.findById(introductionId);
    }
}
