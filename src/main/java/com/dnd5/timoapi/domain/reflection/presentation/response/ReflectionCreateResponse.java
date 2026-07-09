package com.dnd5.timoapi.domain.reflection.presentation.response;

import com.dnd5.timoapi.domain.customization.presentation.response.UnlockedCustomizationItemResponse;

import java.util.List;

public record ReflectionCreateResponse(
        Long id,
        List<UnlockedCustomizationItemResponse> unlockedCustomizations
) {

}
