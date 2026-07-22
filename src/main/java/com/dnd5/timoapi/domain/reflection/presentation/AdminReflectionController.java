package com.dnd5.timoapi.domain.reflection.presentation;

import com.dnd5.timoapi.domain.reflection.application.service.ReflectionService;
import com.dnd5.timoapi.domain.reflection.domain.model.Reflection;
import com.dnd5.timoapi.domain.reflection.presentation.response.ReflectionResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/reflections")
@RequiredArgsConstructor
@Validated
public class AdminReflectionController {

    private final ReflectionService reflectionService;

    @Operation(summary = "오늘 전체 회고 조회")
    @GetMapping("/today")
    public List<ReflectionResponse> getAllReflectionsToday() {
        return reflectionService.findAllToday();
    }

    @Operation(summary = "회고 삭제 (어드민)")
    @DeleteMapping("/{reflectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long reflectionId) {
        reflectionService.delete(reflectionId);
    }
}
