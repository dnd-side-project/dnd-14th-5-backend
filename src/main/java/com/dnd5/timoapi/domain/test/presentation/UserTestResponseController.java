package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.UserTestResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-records/{testRecordId}/responses")
@RequiredArgsConstructor
@Validated
public class UserTestResponseController {

    private final UserTestResponseService userTestResponseService;

}
