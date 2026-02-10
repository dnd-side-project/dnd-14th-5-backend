package com.dnd5.timoapi.domain.fcm.presentation;

import com.dnd5.timoapi.domain.fcm.application.service.FcmService;
import com.dnd5.timoapi.domain.fcm.presentation.request.DeleteDeviceTokenRequest;
import com.dnd5.timoapi.domain.fcm.presentation.request.RegisterDeviceTokenRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/tokens")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerDeviceToken(@Valid @RequestBody RegisterDeviceTokenRequest request) {
        fcmService.registerDeviceToken(request.token(), request.deviceType());
    }

    @DeleteMapping("/tokens")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeviceToken(@Valid @RequestBody DeleteDeviceTokenRequest request) {
        fcmService.deleteDeviceToken(request.token());
    }
}
