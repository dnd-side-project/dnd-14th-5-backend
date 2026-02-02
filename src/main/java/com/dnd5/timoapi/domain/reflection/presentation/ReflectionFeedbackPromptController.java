package com.dnd5.timoapi.domain.reflection.presentation;

import com.dnd5.timoapi.domain.reflection.application.service.ReflectionFeedbackPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedback-prompts")
@RequiredArgsConstructor
@Validated
public class ReflectionFeedbackPromptController {

    private final ReflectionFeedbackPromptService reflectionFeedbackPromptService;

}
