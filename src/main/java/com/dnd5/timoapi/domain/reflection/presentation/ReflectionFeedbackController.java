package com.dnd5.timoapi.domain.reflection.presentation;

import com.dnd5.timoapi.domain.reflection.application.service.ReflectionFeedbackService;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionFeedbackDetailResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reflections/{reflectionId}/feedback")
@RequiredArgsConstructor
@Validated
public class ReflectionFeedbackController {

    private final ReflectionFeedbackService reflectionFeedbackService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReflectionFeedbackDetailResponse create(@Positive @PathVariable Long reflectionId) {
        return reflectionFeedbackService.create(reflectionId);
    }
}
