package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.TestQuestionService;
import com.dnd5.timoapi.domain.test.presentation.request.TestQuestionCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.request.TestQuestionUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/tests/{testId}/questions")
@RequiredArgsConstructor
@Validated
public class AdminTestQuestionController {

    private final TestQuestionService testQuestionService;

    @Operation(summary = "테스트 문항 생성 (어드민)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(
            @Positive @PathVariable Long testId,
            @Valid @RequestBody TestQuestionCreateRequest request
    ) {
        testQuestionService.create(testId, request);
    }

    @Operation(summary = "테스트 문항 수정 (어드민)")
    @PatchMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @Positive @PathVariable Long testId,
            @Positive @PathVariable Long questionId,
            @Valid @RequestBody TestQuestionUpdateRequest request
    ) {
        testQuestionService.update(questionId, testId, request);
    }

    @Operation(summary = "테스트 문항 삭제 (어드민)")
    @DeleteMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Positive @PathVariable Long testId,
            @Positive @PathVariable Long questionId
    ) {
        testQuestionService.delete(questionId, testId);
    }
}
