package com.dnd5.timoapi.domain.test.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/time-perspective-categories")
@RequiredArgsConstructor
@Validated
public class AdminTimePerspectiveCategoryController {

}
