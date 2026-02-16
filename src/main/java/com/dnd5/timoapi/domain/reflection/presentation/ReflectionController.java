package com.dnd5.timoapi.domain.reflection.presentation;

import com.dnd5.timoapi.domain.reflection.application.service.ReflectionService;
import com.dnd5.timoapi.domain.reflection.presentation.request.ReflectionCreateRequest;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionCreateResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionDetailResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionQuestionDetailResponse;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ReflectionCreateResponse create(@Valid @RequestBody ReflectionCreateRequest request) {
        return reflectionService.create(request);
    }

    @GetMapping("/today/question")
    public ReflectionQuestionDetailResponse findQuestionToday() {
        return reflectionService.findQuestionToday();
    }

    @PostMapping("/today/question/change")
    public ReflectionQuestionDetailResponse changeQuestionToday() {
        return reflectionService.changeQuestionToday();
    }

    @GetMapping("/today")
    public ReflectionDetailResponse findReflectionToday() {
        return reflectionService.findReflectionToday();
    }

    @GetMapping("/me")
    public List<ReflectionResponse> findAllMy(@RequestParam YearMonth month) {
        return reflectionService.findAllMy(month);
    }

    @GetMapping("/{reflectionId}")
    public ReflectionDetailResponse findById(@Positive @PathVariable Long reflectionId) {
        return reflectionService.findById(reflectionId);
    }
}
