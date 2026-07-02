package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.TestService;
import com.dnd5.timoapi.domain.test.presentation.request.TestCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.request.TestUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
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
public class AdminTestController {

    private final TestService testService;

    @Operation(summary = "테스트 생성 (어드민)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody TestCreateRequest request) {
        testService.create(request);
    }

    @Operation(summary = "테스트 수정 (어드민)")
    @PatchMapping("/{testId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@Positive @PathVariable Long testId, @Valid @RequestBody TestUpdateRequest request) {
        testService.update(testId, request);
    }

    @Operation(summary = "테스트 삭제 (어드민)")
    @DeleteMapping("/{testId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long testId) {
        testService.delete(testId);
    }
}
