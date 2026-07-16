package com.dnd5.timoapi.domain.customization.application.service;

import com.dnd5.timoapi.domain.customization.domain.entity.CustomizationItemEntity;
import com.dnd5.timoapi.domain.customization.domain.entity.CustomizationItemImageEntity;
import com.dnd5.timoapi.domain.customization.domain.entity.CustomizationUserItemEntity;
import com.dnd5.timoapi.domain.customization.domain.model.CustomizationItem;
import com.dnd5.timoapi.domain.customization.domain.model.CustomizationItemImage;
import com.dnd5.timoapi.domain.customization.domain.model.CustomizationUserItem;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationUnlockConditionType;
import com.dnd5.timoapi.domain.customization.domain.repository.CustomizationItemImageRepository;
import com.dnd5.timoapi.domain.customization.domain.repository.CustomizationItemRepository;
import com.dnd5.timoapi.domain.customization.domain.repository.CustomizationUserItemRepository;
import com.dnd5.timoapi.domain.customization.exception.CustomizationItemErrorCode;
import com.dnd5.timoapi.domain.customization.presentation.request.CustomizationItemCreateRequest;
import com.dnd5.timoapi.domain.customization.presentation.request.CustomizationItemImageRequest;
import com.dnd5.timoapi.domain.customization.presentation.request.CustomizationItemUpdateRequest;
import com.dnd5.timoapi.domain.customization.presentation.response.CustomizationItemDetailResponse;
import com.dnd5.timoapi.domain.customization.presentation.response.CustomizationItemResponse;
import com.dnd5.timoapi.domain.customization.presentation.response.EquippedCustomizationResponse;
import com.dnd5.timoapi.domain.customization.presentation.response.UnlockedCustomizationItemResponse;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.test.domain.repository.TimePerspectiveCategoryRepository;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.domain.user.exception.UserErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
    private final CustomizationItemImageRepository customizationItemImageRepository;
    private final CustomizationUserItemRepository customizationUserItemRepository;
    private final UserRepository userRepository;
    private final TimePerspectiveCategoryRepository timePerspectiveCategoryRepository;

    @Transactional(readOnly = true)
    public List<CustomizationItemResponse> findAll() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Map<Long, CustomizationUserItemEntity> userItemsByItemId = customizationUserItemRepository
                .findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .collect(Collectors.toMap(CustomizationUserItemEntity::getCustomizationItemId, Function.identity()));

        List<CustomizationItemEntity> items = customizationItemRepository.findAllByDeletedAtIsNull();
        Map<Long, CustomizationItemImage> imagesByItemId = resolveImages(items, user.getCategory());

        return items.stream()
                .map(itemEntity -> {
                    CustomizationUserItemEntity userItem = userItemsByItemId.get(itemEntity.getId());
                    boolean isUnlocked = userItem != null && userItem.isUnlocked();
                    boolean isEquipped = userItem != null && userItem.isEquipped();
                    CustomizationItemImage image = imagesByItemId.get(itemEntity.getId());
                    return CustomizationItemResponse.from(
                            itemEntity.toModel(), isUnlocked, isEquipped,
                            image != null ? image.image() : null,
                            image != null ? image.imageWithoutBackground() : null);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquippedCustomizationResponse> findEquippedItems(Long userId) {
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

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
        Map<Long, CustomizationItemImage> imagesByItemId = resolveImages(itemsById.values(), user.getCategory());

        return equippedUserItems.stream()
                .map(userItem -> itemsById.get(userItem.getCustomizationItemId()))
                .filter(item -> item != null)
                .map(item -> {
                    CustomizationItemImage image = imagesByItemId.get(item.getId());
                    return EquippedCustomizationResponse.from(
                            item.toModel(),
                            image != null ? image.image() : null,
                            image != null ? image.imageWithoutBackground() : null);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomizationItemDetailResponse findById(Long userId, Long customizationItemId) {
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        CustomizationItemEntity entity = getCustomizationItemEntity(customizationItemId);
        CustomizationItemImage image = resolveImage(entity, user.getCategory());
        return CustomizationItemDetailResponse.from(
                entity.toModel(),
                image != null ? image.image() : null,
                image != null ? image.imageWithoutBackground() : null);
    }

    public void create(CustomizationItemCreateRequest request) {
        CustomizationItem model = request.toModel();
        CustomizationItemEntity savedItem = customizationItemRepository.save(CustomizationItemEntity.from(model));

        List<CustomizationItemImageEntity> images = request.images().stream()
                .map(imageRequest -> CustomizationItemImageEntity.from(
                        CustomizationItemImage.create(
                                savedItem.getId(), imageRequest.category(),
                                imageRequest.image(), imageRequest.imageWithoutBackground())))
                .toList();
        customizationItemImageRepository.saveAll(images);
    }

    public void update(Long customizationItemId, CustomizationItemUpdateRequest request) {
        CustomizationItemEntity entity = getCustomizationItemEntity(customizationItemId);
        entity.update(
                request.name(),
                request.type(),
                request.description(),
                request.unlockConditionType(),
                request.unlockConditionCount()
        );

        if (request.images() != null) {
            for (CustomizationItemImageRequest imageRequest : request.images()) {
                CustomizationItemImageEntity existing = customizationItemImageRepository
                        .findByCustomizationItemIdAndCategoryAndDeletedAtIsNull(customizationItemId, imageRequest.category())
                        .orElse(null);
                if (existing == null) {
                    customizationItemImageRepository.save(CustomizationItemImageEntity.from(
                            CustomizationItemImage.create(
                                    customizationItemId, imageRequest.category(),
                                    imageRequest.image(), imageRequest.imageWithoutBackground())));
                } else {
                    existing.updateImage(imageRequest.image(), imageRequest.imageWithoutBackground());
                }
            }
        }
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

            CustomizationItemImage image = resolveImage(item, user.getCategory());
            newlyUnlocked.add(UnlockedCustomizationItemResponse.from(
                    item.toModel(),
                    image != null ? image.image() : null,
                    image != null ? image.imageWithoutBackground() : null));
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

        List<CustomizationItemEntity> justMetItems = customizationItemRepository.findAllById(unlockedItemIds).stream()
                .filter(item -> isUnlockConditionJustMet(item, user))
                .toList();

        Map<Long, CustomizationItemImage> imagesByItemId = resolveImages(justMetItems, user.getCategory());

        return justMetItems.stream()
                .map(item -> {
                    CustomizationItemImage image = imagesByItemId.get(item.getId());
                    return UnlockedCustomizationItemResponse.from(
                            item.toModel(),
                            image != null ? image.image() : null,
                            image != null ? image.imageWithoutBackground() : null);
                })
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

    private static final String CHARACTER_PET_ITEM_NAME = "나의 캐릭터 펫";

    private boolean usesCharacterImage(CustomizationItemEntity item) {
        return CHARACTER_PET_ITEM_NAME.equals(item.getName());
    }

    private CustomizationItemImage resolveImage(CustomizationItemEntity item, ZtpiCategory category) {
        if (category == null) {
            return null;
        }
        if (usesCharacterImage(item)) {
            return resolveCharacterImage(item.getId(), category);
        }
        return customizationItemImageRepository
                .findByCustomizationItemIdAndCategoryAndDeletedAtIsNull(item.getId(), category)
                .map(CustomizationItemImageEntity::toModel)
                .orElse(null);
    }

    private Map<Long, CustomizationItemImage> resolveImages(Collection<CustomizationItemEntity> items, ZtpiCategory category) {
        if (category == null || items.isEmpty()) {
            return Map.of();
        }

        Map<Long, CustomizationItemImage> result = new HashMap<>();

        List<Long> uploadedImageItemIds = items.stream()
                .filter(item -> !usesCharacterImage(item))
                .map(CustomizationItemEntity::getId)
                .toList();
        if (!uploadedImageItemIds.isEmpty()) {
            customizationItemImageRepository
                    .findAllByCustomizationItemIdInAndCategoryAndDeletedAtIsNull(uploadedImageItemIds, category)
                    .forEach(entity -> result.put(entity.getCustomizationItemId(), entity.toModel()));
        }

        List<CustomizationItemEntity> characterImageItems = items.stream()
                .filter(this::usesCharacterImage)
                .toList();
        if (!characterImageItems.isEmpty()) {
            CustomizationItemImage characterImage = resolveCharacterImage(null, category);
            if (characterImage != null) {
                characterImageItems.forEach(item -> result.put(item.getId(), characterImage));
            }
        }

        return result;
    }

    private CustomizationItemImage resolveCharacterImage(Long customizationItemId, ZtpiCategory category) {
        return timePerspectiveCategoryRepository
                .findAllByEnglishNameAndDeletedAtIsNull(category.name())
                .stream()
                .findFirst()
                .map(tpc -> new CustomizationItemImage(
                        null, customizationItemId, category, tpc.getImage(), null,
                        tpc.getCreatedAt(), tpc.getUpdatedAt(), null))
                .orElse(null);
    }

    private CustomizationItemEntity getCustomizationItemEntity(Long customizationItemId) {
        return customizationItemRepository.findByIdAndDeletedAtIsNull(customizationItemId)
                .orElseThrow(() -> new BusinessException(CustomizationItemErrorCode.CUSTOMIZATION_ITEM_NOT_FOUND));
    }
}
