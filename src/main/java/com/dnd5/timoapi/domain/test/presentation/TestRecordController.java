package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.TestRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-records")
@RequiredArgsConstructor
@Validated
public class TestRecordController {

    private final TestRecordService testRecordService;

}
