package com.dnd5.timoapi.domain.reflection.presentation;

import com.dnd5.timoapi.domain.reflection.application.service.ReflectionService;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionCreateRequest;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionDetailResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionQuestionDetailResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionResponse;
import com.dnd5.timoapi.global.common.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reflections")
@RequiredArgsConstructor
@Validated
public class ReflectionController {

    private final ReflectionService reflectionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody ReflectionCreateRequest request) {
        reflectionService.create(request);
    }

    @GetMapping("/today/question")
    public ReflectionQuestionDetailResponse findQuestionToday() {
        return reflectionService.findQuestionToday();
    }

    @GetMapping("/today")
    public ReflectionDetailResponse findReflectionToday() {
        return reflectionService.findReflectionToday();
    }

    @GetMapping("/me")
    public PageResponse<ReflectionResponse> findAllMy(Pageable pageable) {
        return reflectionService.findAllMy(pageable);
    }

    @GetMapping("/{reflectionId}")
    public ReflectionDetailResponse findById(@Positive @PathVariable Long reflectionId) {
        return reflectionService.findById(reflectionId);
    }
}
