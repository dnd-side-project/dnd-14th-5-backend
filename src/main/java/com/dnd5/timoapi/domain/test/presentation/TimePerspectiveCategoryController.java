package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.TimePerspectiveCategoryService;
import com.dnd5.timoapi.domain.test.presentation.response.TimePerspectiveCategoryDetailResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/time-perspective-categories")
@RequiredArgsConstructor
@Validated
public class TimePerspectiveCategoryController {

    private final TimePerspectiveCategoryService timePerspectiveCategoryService;

    @GetMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public TimePerspectiveCategoryDetailResponse findById(@Positive @PathVariable Long categoryId) {
        return timePerspectiveCategoryService.findById(categoryId);
    }

}
