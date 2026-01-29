package com.dnd5.timoapi.domain.reflection.presentation;

import com.dnd5.timoapi.domain.reflection.application.service.ReflectionQuestionService;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionQuestionDetailResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Validated
public class ReflectionQuestionController {

    private final ReflectionQuestionService reflectionQuestionService;

    @GetMapping("/{questionId}")
    public ReflectionQuestionDetailResponse findById(@Positive @PathVariable Long questionId) {
        return reflectionQuestionService.findById(questionId);
    }
}
