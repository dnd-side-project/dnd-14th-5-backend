package com.dnd5.timoapi.domain.reflection.presentation;

import com.dnd5.timoapi.domain.reflection.application.service.ReflectionFeedbackPromptService;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionFeedbackPromptCreateRequest;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionFeedbackPromptUpdateRequest;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackPromptDetailResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackPromptResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
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

@RestController
@RequestMapping("/feedback-prompts")
@RequiredArgsConstructor
@Validated
public class AdminReflectionFeedbackPromptController {

    private final ReflectionFeedbackPromptService reflectionFeedbackPromptService;

    @Operation(summary = "피드백 프롬프트 생성 (어드민)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody ReflectionFeedbackPromptCreateRequest request) {
        reflectionFeedbackPromptService.create(request);
    }

    @Operation(summary = "피드백 프롬프트 목록 조회 (어드민)")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReflectionFeedbackPromptResponse> findAll() {
        return reflectionFeedbackPromptService.findAll();
    }

    @Operation(summary = "피드백 프롬프트 버전 조회 (어드민)")
    @GetMapping("/{version}")
    public ReflectionFeedbackPromptDetailResponse findByVersion(
            @Positive @PathVariable int version
    ) {
        return reflectionFeedbackPromptService.findByVersion(version);
    }

    @Operation(summary = "피드백 프롬프트 수정 (어드민)")
    @PatchMapping("/{version}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@Positive @PathVariable int version, @Valid @RequestBody ReflectionFeedbackPromptUpdateRequest request) {
        reflectionFeedbackPromptService.update(version, request);
    }

    @Operation(summary = "피드백 프롬프트 삭제 (어드민)")
    @DeleteMapping("/{version}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable int version) {
        reflectionFeedbackPromptService.delete(version);
    }
}
