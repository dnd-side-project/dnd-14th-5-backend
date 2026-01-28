package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.TestService;
import com.dnd5.timoapi.domain.test.presentation.request.TestCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.request.TestUpdateRequest;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody TestCreateRequest request) {
        testService.create(request);
    }

    @PatchMapping("/{testId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@Positive @PathVariable Long testId, @Valid @RequestBody TestUpdateRequest request) {
        testService.update(testId, request);
    }

    @DeleteMapping("/{testId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long testId) {
        testService.delete(testId);
    }
}
