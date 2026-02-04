package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.TestService;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestType;
import com.dnd5.timoapi.domain.test.presentation.response.TestDetailResponse;
import com.dnd5.timoapi.domain.test.presentation.response.TestResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tests")
@RequiredArgsConstructor
@Validated
public class TestController {

    private final TestService testService;

    @GetMapping
    public List<TestResponse> findAll() {
        return testService.findAll();
    }

    @GetMapping("/{testId}")
    public TestDetailResponse findById(@Positive @PathVariable Long testId) {
        return testService.findById(testId);
    }

    @GetMapping("/type/{testType}")
    public TestDetailResponse findByType(@PathVariable TestType testType) {
        return testService.findByType(testType);
    }

}
