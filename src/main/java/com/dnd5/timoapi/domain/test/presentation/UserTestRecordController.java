package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.UserTestRecordService;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test-records")
@RequiredArgsConstructor
@Validated
public class UserTestRecordController {

    private final UserTestRecordService userTestRecordService;

    @GetMapping
    public List<UserTestRecordResponse> findAll(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long testId
    ) {
        return userTestRecordService.findAll(userId, testId);
    }
}
