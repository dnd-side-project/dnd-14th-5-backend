package com.dnd5.timoapi.domain.user.application.service;

import com.dnd5.timoapi.domain.auth.domain.repository.RefreshTokenRepository;
import com.dnd5.timoapi.domain.user.application.service.UserTestRecordService;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserTestRecordRepository;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.domain.user.exception.UserErrorCode;
import com.dnd5.timoapi.domain.user.presentation.request.UpdateMeRequest;
import com.dnd5.timoapi.domain.user.presentation.response.UserResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserTestRecordRepository userTestRecordRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final UserTestRecordService userTestRecordService;

    @Transactional(readOnly = true)
    public UserResponse getMe() {
        UserEntity user = getCurrentUserEntity();
        return UserResponse.from(user.toModel());
    }

    public void updateMe(UpdateMeRequest request) {
        UserEntity user = getCurrentUserEntity();
        user.update(request.name());
    }

    @Transactional
    public void deleteMe() {
        UserEntity userEntity = getCurrentUserEntity();

        List<UserTestRecordEntity> userTestRecordEntityList =
                userTestRecordRepository.findByUserIdAndDeletedAtIsNull(userEntity.getId());

        userTestRecordEntityList.forEach(
                userTestRecordEntity -> userTestRecordService.delete(userTestRecordEntity.getId())
        );

        userEntity.softDelete();
        refreshTokenRepository.deleteByUserId(userEntity.getId());
    }

    private UserEntity getCurrentUserEntity() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
