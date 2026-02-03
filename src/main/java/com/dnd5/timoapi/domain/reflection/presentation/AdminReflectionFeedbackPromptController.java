package com.dnd5.timoapi.domain.reflection.presentation;

import com.dnd5.timoapi.domain.reflection.application.service.ReflectionFeedbackPromptService;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionFeedbackPromptCreateRequest;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackPromptDetailResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackPromptResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody ReflectionFeedbackPromptCreateRequest request) {
        reflectionFeedbackPromptService.create(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReflectionFeedbackPromptResponse> findAll() {
        return reflectionFeedbackPromptService.findAll();
    }

    @GetMapping("/{version}")
    public ReflectionFeedbackPromptDetailResponse findByVersion(
            @Positive @PathVariable int version
    ) {
        return reflectionFeedbackPromptService.findByVersion(version);
    }
}
