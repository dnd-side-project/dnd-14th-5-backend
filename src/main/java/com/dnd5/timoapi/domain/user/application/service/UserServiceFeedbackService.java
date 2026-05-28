package com.dnd5.timoapi.domain.user.application.service;

import com.dnd5.timoapi.domain.user.domain.entity.*;
import com.dnd5.timoapi.domain.user.domain.model.UserServiceFeedback;
import com.dnd5.timoapi.domain.user.domain.repository.*;
import com.dnd5.timoapi.domain.user.exception.UserErrorCode;
import com.dnd5.timoapi.domain.user.exception.UserServiceFeedbackErrorCode;
import com.dnd5.timoapi.domain.user.presentation.request.UserServiceFeedbackCreateRequest;
import com.dnd5.timoapi.domain.user.presentation.response.UserServiceFeedbackDetailResponse;
import com.dnd5.timoapi.domain.user.presentation.response.UserServiceFeedbackResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.dnd5.timoapi.global.security.context.SecurityUtil.getCurrentUserId;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceFeedbackService {

    private final UserRepository userRepository;
    private final UserServiceFeedbackRepository userServiceFeedbackRepository;

    public void create(UserServiceFeedbackCreateRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        UserEntity userEntity = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (userServiceFeedbackRepository.existsByUserIdAndDeletedAtIsNull(userId)) {
            throw new BusinessException(UserServiceFeedbackErrorCode.ALREADY_EXISTS);
        }

        UserServiceFeedback model = request.toModel();

        userServiceFeedbackRepository.save(UserServiceFeedbackEntity.from(userEntity, model.serviceRating(), model.serviceFeedback()));
    }

    public List<UserServiceFeedbackResponse> findAll() {
        return userServiceFeedbackRepository.findByDeletedAtIsNull().stream()
                .map(UserServiceFeedbackEntity::toModel)
                .map(UserServiceFeedbackResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserServiceFeedbackDetailResponse findById(Long feedbackId) {
        UserServiceFeedbackEntity userServiceFeedbackEntity = userServiceFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new BusinessException(UserServiceFeedbackErrorCode.USER_SERVICE_FEEDBACK_NOT_FOUND));

        return UserServiceFeedbackDetailResponse.of(
                userServiceFeedbackEntity.toModel()
        );
    }

    @Transactional
    public void delete(Long feedbackId) {
        UserServiceFeedbackEntity userServiceFeedbackEntity = userServiceFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new BusinessException(UserServiceFeedbackErrorCode.USER_SERVICE_FEEDBACK_NOT_FOUND));

        userServiceFeedbackEntity.softDelete();
    }

}
