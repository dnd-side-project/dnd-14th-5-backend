package com.dnd5.timoapi.domain.user.presentation;

import com.dnd5.timoapi.domain.user.application.service.UserService;
import com.dnd5.timoapi.domain.user.presentation.request.UpdateMeRequest;
import com.dnd5.timoapi.domain.user.presentation.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMe() {
        return userService.getMe();
    }

    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMe(@RequestBody UpdateMeRequest request) {
        userService.updateMe(request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe() {
        userService.deleteMe();
    }
}
