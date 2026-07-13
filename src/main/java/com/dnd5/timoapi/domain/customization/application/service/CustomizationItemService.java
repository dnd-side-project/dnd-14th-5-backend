package com.dnd5.timoapi.domain.customization.application.service;

import com.dnd5.timoapi.domain.customization.domain.entity.CustomizationItemEntity;
import com.dnd5.timoapi.domain.customization.domain.entity.CustomizationUserItemEntity;
import com.dnd5.timoapi.domain.customization.domain.model.CustomizationItem;
import com.dnd5.timoapi.domain.customization.domain.model.CustomizationUserItem;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationUnlockConditionType;
import com.dnd5.timoapi.domain.customization.domain.repository.CustomizationItemRepository;
import com.dnd5.timoapi.domain.customization.domain.repository.CustomizationUserItemRepository;
import com.dnd5.timoapi.domain.customization.exception.CustomizationItemErrorCode;
import com.dnd5.timoapi.domain.customization.presentation.request.CustomizationItemCreateRequest;
import com.dnd5.timoapi.domain.customization.presentation.request.CustomizationItemUpdateRequest;
import com.dnd5.timoapi.domain.customization.presentation.response.CustomizationItemDetailResponse;
import com.dnd5.timoapi.domain.customization.presentation.response.CustomizationItemResponse;
import com.dnd5.timoapi.domain.customization.presentation.response.EquippedCustomizationResponse;
import com.dnd5.timoapi.domain.customization.presentation.response.UnlockedCustomizationItemResponse;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.domain.user.exception.UserErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomizationItemService {

    private final CustomizationItemRepository customizationItemRepository;
    private final CustomizationUserItemRepository customizationUserItemRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CustomizationItemResponse> findAll() {
        Long userId = SecurityUtil.getCurrentUserId();

        Map<Long, CustomizationUserItemEntity> userItemsByItemId = customizationUserItemRepository
                .findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .collect(Collectors.toMap(CustomizationUserItemEntity::getCustomizationItemId, Function.identity()));

        return customizationItemRepository.findAllByDeletedAtIsNull().stream()
                .map(itemEntity -> {
                    CustomizationUserItemEntity userItem = userItemsByItemId.get(itemEntity.getId());
                    boolean isUnlocked = userItem != null && userItem.isUnlocked();
                    boolean isEquipped = userItem != null && userItem.isEquipped();
                    return CustomizationItemResponse.from(itemEntity.toModel(), isUnlocked, isEquipped);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquippedCustomizationResponse> findEquippedItems(Long userId) {
        List<CustomizationUserItemEntity> equippedUserItems = customizationUserItemRepository
                .findAllByUserIdAndIsEquippedTrueAndDeletedAtIsNull(userId);

        if (equippedUserItems.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = equippedUserItems.stream()
                .map(CustomizationUserItemEntity::getCustomizationItemId)
                .toList();

        Map<Long, CustomizationItemEntity> itemsById = customizationItemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(CustomizationItemEntity::getId, Function.identity()));

        return equippedUserItems.stream()
                .map(userItem -> itemsById.get(userItem.getCustomizationItemId()))
                .filter(item -> item != null)
                .map(item -> EquippedCustomizationResponse.from(item.toModel()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomizationItemDetailResponse findById(Long customizationItemId) {
        CustomizationItemEntity entity = getCustomizationItemEntity(customizationItemId);
        return CustomizationItemDetailResponse.from(entity.toModel());
    }

    public void create(CustomizationItemCreateRequest request) {
        CustomizationItem model = request.toModel();
        customizationItemRepository.save(CustomizationItemEntity.from(model));
    }

    public void update(Long customizationItemId, CustomizationItemUpdateRequest request) {
        CustomizationItemEntity entity = getCustomizationItemEntity(customizationItemId);
        entity.update(
                request.name(),
                request.type(),
                request.description(),
                request.unlockConditionType(),
                request.unlockConditionCount(),
                request.image()
        );
    }

    public void delete(Long customizationItemId) {
        CustomizationItemEntity entity = getCustomizationItemEntity(customizationItemId);
        entity.softDelete();
    }

    public void equip(Long userId, Long customizationItemId) {
        CustomizationItemEntity item = getCustomizationItemEntity(customizationItemId);

        CustomizationUserItemEntity userItem = customizationUserItemRepository
                .findByUserIdAndCustomizationItemIdAndDeletedAtIsNull(userId, customizationItemId)
                .orElseThrow(() -> new BusinessException(
                        CustomizationItemErrorCode.CUSTOMIZATION_ITEM_NOT_UNLOCKED, customizationItemId));

        if (!userItem.isUnlocked()) {
            throw new BusinessException(CustomizationItemErrorCode.CUSTOMIZATION_ITEM_NOT_UNLOCKED, customizationItemId);
        }

        if (item.getType() == CustomizationItemType.THEME) {
            customizationUserItemRepository.findAllByUserIdAndIsEquippedTrueAndDeletedAtIsNull(userId).stream()
                    .filter(equipped -> !equipped.getCustomizationItemId().equals(customizationItemId))
                    .map(equipped -> customizationItemRepository.findById(equipped.getCustomizationItemId()))
                    .flatMap(Optional::stream)
                    .filter(equippedItem -> equippedItem.getType() == CustomizationItemType.THEME)
                    .findFirst()
                    .ifPresent(equippedItem -> {
                        throw new BusinessException(
                                CustomizationItemErrorCode.CUSTOMIZATION_THEME_ALREADY_EQUIPPED, equippedItem.getId());
                    });
        }

        userItem.equip();
    }

    public void unequip(Long userId, Long customizationItemId) {
        getCustomizationItemEntity(customizationItemId);

        CustomizationUserItemEntity userItem = customizationUserItemRepository
                .findByUserIdAndCustomizationItemIdAndDeletedAtIsNull(userId, customizationItemId)
                .orElseThrow(() -> new BusinessException(
                        CustomizationItemErrorCode.CUSTOMIZATION_ITEM_NOT_UNLOCKED, customizationItemId));

        userItem.unequip();
    }

    public List<UnlockedCustomizationItemResponse> unlockEligibleItems(Long userId) {
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Map<Long, CustomizationUserItemEntity> userItemsByItemId = customizationUserItemRepository
                .findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .collect(Collectors.toMap(CustomizationUserItemEntity::getCustomizationItemId, Function.identity()));

        List<UnlockedCustomizationItemResponse> newlyUnlocked = new ArrayList<>();

        for (CustomizationItemEntity item : customizationItemRepository.findAllByDeletedAtIsNull()) {
            if (!isUnlockConditionMet(item, user)) {
                continue;
            }

            CustomizationUserItemEntity userItem = userItemsByItemId.get(item.getId());
            if (userItem != null && userItem.isUnlocked()) {
                continue;
            }

            if (userItem == null) {
                CustomizationUserItemEntity newUserItem = CustomizationUserItemEntity.from(
                        CustomizationUserItem.create(userId, item.getId()));
                newUserItem.unlock();
                customizationUserItemRepository.save(newUserItem);
            } else {
                userItem.unlock();
            }

            newlyUnlocked.add(UnlockedCustomizationItemResponse.from(item.toModel()));
        }

        return newlyUnlocked;
    }

    @Transactional(readOnly = true)
    public List<UnlockedCustomizationItemResponse> findRecentlyUnlockedItems(Long userId) {
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        List<Long> unlockedItemIds = customizationUserItemRepository
                .findAllByUserIdAndIsUnlockedTrueAndDeletedAtIsNull(userId).stream()
                .map(CustomizationUserItemEntity::getCustomizationItemId)
                .toList();

        if (unlockedItemIds.isEmpty()) {
            return List.of();
        }

        return customizationItemRepository.findAllById(unlockedItemIds).stream()
                .filter(item -> isUnlockConditionJustMet(item, user))
                .map(item -> UnlockedCustomizationItemResponse.from(item.toModel()))
                .toList();
    }

    public void revokeStreakUnlocksFor(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return;
        }

        List<Long> streakItemIds = customizationItemRepository.findAllByDeletedAtIsNull().stream()
                .filter(item -> item.getUnlockConditionType() == CustomizationUnlockConditionType.STREAK_COUNT)
                .map(CustomizationItemEntity::getId)
                .toList();

        if (streakItemIds.isEmpty()) {
            return;
        }

        customizationUserItemRepository
                .findAllByUserIdInAndCustomizationItemIdInAndIsUnlockedTrueAndDeletedAtIsNull(userIds, streakItemIds)
                .forEach(userItem -> {
                    userItem.lock();
                    if (userItem.isEquipped()) {
                        userItem.unequip();
                    }
                });
    }

    private boolean isUnlockConditionMet(CustomizationItemEntity item, UserEntity user) {
        return switch (item.getUnlockConditionType()) {
            case TOTAL_COUNT -> item.getUnlockConditionCount() <= user.getTotalDays();
            case STREAK_COUNT -> item.getUnlockConditionCount() <= user.getStreakDays();
        };
    }

    private boolean isUnlockConditionJustMet(CustomizationItemEntity item, UserEntity user) {
        return switch (item.getUnlockConditionType()) {
            case TOTAL_COUNT -> item.getUnlockConditionCount().equals(user.getTotalDays());
            case STREAK_COUNT -> item.getUnlockConditionCount().equals(user.getStreakDays());
        };
    }

    private CustomizationItemEntity getCustomizationItemEntity(Long customizationItemId) {
        return customizationItemRepository.findByIdAndDeletedAtIsNull(customizationItemId)
                .orElseThrow(() -> new BusinessException(CustomizationItemErrorCode.CUSTOMIZATION_ITEM_NOT_FOUND));
    }
}
