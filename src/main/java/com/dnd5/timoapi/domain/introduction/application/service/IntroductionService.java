package com.dnd5.timoapi.domain.introduction.application.service;

import com.dnd5.timoapi.domain.introduction.domain.entity.IntroductionEntity;
import com.dnd5.timoapi.domain.introduction.domain.repository.IntroductionRepository;
import com.dnd5.timoapi.domain.introduction.exception.IntroductionErrorCode;
import com.dnd5.timoapi.domain.introduction.presentation.request.IntroductionCreateRequest;
import com.dnd5.timoapi.domain.introduction.presentation.request.IntroductionUpdateRequest;
import com.dnd5.timoapi.domain.introduction.presentation.response.IntroductionResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IntroductionService {

    private final IntroductionRepository introductionRepository;

    public void create(IntroductionCreateRequest request) {
        introductionRepository.save(IntroductionEntity.from(request.toModel()));
    }

    @Transactional(readOnly = true)
    public List<IntroductionResponse> findAll() {
        return introductionRepository.findAllByDeletedAtIsNull().stream()
                .map(IntroductionEntity::toModel)
                .map(IntroductionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public IntroductionResponse findById(Long introductionId) {
        return IntroductionResponse.from(getEntity(introductionId).toModel());
    }

    public void update(Long introductionId, IntroductionUpdateRequest request) {
        IntroductionEntity entity = getEntity(introductionId);
        entity.update(request.version(), request.content());
    }

    public void delete(Long introductionId) {
        IntroductionEntity entity = getEntity(introductionId);
        entity.setDeletedAt(LocalDateTime.now());
    }

    private IntroductionEntity getEntity(Long introductionId) {
        return introductionRepository.findByIdAndDeletedAtIsNull(introductionId)
                .orElseThrow(() -> new BusinessException(IntroductionErrorCode.INTRODUCTION_NOT_FOUND));
    }
}
