package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.TestQuestionService;
import com.dnd5.timoapi.domain.test.presentation.request.TestQuestionCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.TestQuestionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/tests")
@RequiredArgsConstructor
@Validated
public class AdminTestQuestionController {

    private final TestQuestionService testQuestionService;

    @PostMapping("/{testId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public TestQuestionResponse create(
            @Positive @PathVariable Long testId,
            @Valid @RequestBody TestQuestionCreateRequest request
    ) {
        return TestQuestionResponse.from(
                testQuestionService.create(testId, request)
        );
    }

    @PatchMapping("/{testId}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @Positive @PathVariable Long testId,
            @Positive @PathVariable Long questionId,
            @Valid @RequestBody TestQuestionCreateRequest request
    ) {
        testQuestionService.update(questionId, testId, request);
    }

    @DeleteMapping("/{testId}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Positive @PathVariable Long testId,
            @Positive @PathVariable Long questionId
    ) {
        testQuestionService.delete(questionId);
    }
}
