package com.dnd5.timoapi.domain.reflection.presentation;

import com.dnd5.timoapi.domain.reflection.application.service.ReflectionQuestionService;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionQuestionCreateRequest;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionQuestionUpdateRequest;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionQuestionResponse;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.common.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/questions")
@RequiredArgsConstructor
@Validated
public class AdminReflectionQuestionController {

    private final ReflectionQuestionService reflectionQuestionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody ReflectionQuestionCreateRequest request) {
        reflectionQuestionService.create(request);
    }

    @GetMapping
    public PageResponse<ReflectionQuestionResponse> findAll(
            Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ZtpiCategory category
    ) {
        return reflectionQuestionService.findAll(pageable, keyword, category);
    }

    @PatchMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @Positive @PathVariable Long questionId,
            @Valid @RequestBody ReflectionQuestionUpdateRequest request
    ) {
        reflectionQuestionService.update(questionId, request);
    }

    @DeleteMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long questionId) {
        reflectionQuestionService.delete(questionId);
    }
}
