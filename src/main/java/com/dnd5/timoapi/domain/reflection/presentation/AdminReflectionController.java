package com.dnd5.timoapi.domain.reflection.presentation;

import com.dnd5.timoapi.domain.reflection.application.service.ReflectionService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reflections")
@RequiredArgsConstructor
@Validated
public class AdminReflectionController {

    private final ReflectionService reflectionService;

    @DeleteMapping("/{reflectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long reflectionId) {
        reflectionService.delete(reflectionId);
    }
}
