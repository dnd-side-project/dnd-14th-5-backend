package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.TestQuestionService;
import com.dnd5.timoapi.domain.test.presentation.response.TestQuestionDetailResponse;
import com.dnd5.timoapi.domain.test.presentation.response.TestQuestionResponse;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tests/{testId}/questions")
@RequiredArgsConstructor
@Validated
public class TestQuestionController {

    private final TestQuestionService testQuestionService;

    @GetMapping
    public List<TestQuestionResponse> findAll(
            @Positive @PathVariable Long testId
    ) {
        return testQuestionService.findAll(testId);
    }

    @GetMapping("/{questionId}")
    public TestQuestionDetailResponse findById(
            @Positive @PathVariable Long testId,
            @Positive @PathVariable Long questionId
    ) {
        return testQuestionService.findById(testId, questionId);
    }
}
