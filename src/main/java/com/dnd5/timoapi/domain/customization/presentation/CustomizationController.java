package com.dnd5.timoapi.domain.customization.presentation;

import com.dnd5.timoapi.domain.customization.application.service.CustomizationItemService;
import com.dnd5.timoapi.domain.customization.presentation.response.CustomizationItemDetailResponse;
import com.dnd5.timoapi.domain.customization.presentation.response.CustomizationItemResponse;
import com.dnd5.timoapi.domain.customization.presentation.response.UnlockedCustomizationItemResponse;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customizations")
@RequiredArgsConstructor
@Validated
public class CustomizationController {

    private final CustomizationItemService customizationItemService;

    @Operation(summary = "커스터마이징 아이템 목록 조회")
    @GetMapping
    public List<CustomizationItemResponse> getCustomizations() {
        return customizationItemService.findAll();
    }

    @Operation(summary = "커스터마이징 아이템 단건 조회")
    @GetMapping("/{customizationItemId}")
    public CustomizationItemDetailResponse getCustomization(@Positive @PathVariable Long customizationItemId) {
        return customizationItemService.findById(customizationItemId);
    }

    @Operation(summary = "가장 최근 활동으로 해금된 커스터마이징 아이템 조회")
    @GetMapping("/recently-unlocked")
    public List<UnlockedCustomizationItemResponse> getRecentlyUnlocked() {
        return customizationItemService.findRecentlyUnlockedItems(SecurityUtil.getCurrentUserId());
    }

    @Operation(summary = "커스터마이징 아이템 장착")
    @PostMapping("/{customizationItemId}/equip")
    @ResponseStatus(HttpStatus.OK)
    public void equip(@Positive @PathVariable Long customizationItemId) {
        customizationItemService.equip(SecurityUtil.getCurrentUserId(), customizationItemId);
    }

    @Operation(summary = "커스터마이징 아이템 장착 해제")
    @DeleteMapping("/{customizationItemId}/equip")
    @ResponseStatus(HttpStatus.OK)
    public void unequip(@Positive @PathVariable Long customizationItemId) {
        customizationItemService.unequip(SecurityUtil.getCurrentUserId(), customizationItemId);
    }
}
