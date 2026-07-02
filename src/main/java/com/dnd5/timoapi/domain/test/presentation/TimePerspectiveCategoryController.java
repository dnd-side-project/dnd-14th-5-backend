package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.TimePerspectiveCategoryService;
import com.dnd5.timoapi.domain.test.presentation.response.TimePerspectiveCategoryDetailResponse;
import com.dnd5.timoapi.domain.test.presentation.response.TimePerspectiveCategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import java.util.List;
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

    @Operation(summary = "시간관 카테고리 목록 조회")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TimePerspectiveCategoryResponse> findAll() {
        return timePerspectiveCategoryService.findAll();
    }

    @Operation(summary = "시간관 카테고리 단건 조회")
    @GetMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public TimePerspectiveCategoryDetailResponse findById(@Positive @PathVariable Long categoryId) {
        return timePerspectiveCategoryService.findById(categoryId);
    }
}
