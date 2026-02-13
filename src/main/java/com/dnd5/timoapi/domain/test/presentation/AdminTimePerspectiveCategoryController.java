package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.TimePerspectiveCategoryService;
import com.dnd5.timoapi.domain.test.presentation.request.TimePerspectiveCategoryCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.TimePerspectiveCategoryResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/time-perspective-categories")
@RequiredArgsConstructor
@Validated
public class AdminTimePerspectiveCategoryController {

    private final TimePerspectiveCategoryService timePerspectiveCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody TimePerspectiveCategoryCreateRequest request) {
        timePerspectiveCategoryService.create(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TimePerspectiveCategoryResponse> findAll() {
        return timePerspectiveCategoryService.findAll();
    }

}
