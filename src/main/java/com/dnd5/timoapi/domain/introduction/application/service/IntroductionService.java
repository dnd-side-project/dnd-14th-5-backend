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
    public IntroductionResponse findByVersion(int version) {
        return IntroductionResponse.from(getEntity(version).toModel());
    }

    public void update(int version, IntroductionUpdateRequest request) {
        IntroductionEntity entity = getEntity(version);
        entity.update(request.version(), request.content());
    }

    public void delete(int version) {
        IntroductionEntity entity = getEntity(version);
        entity.setDeletedAt(LocalDateTime.now());
    }

    private IntroductionEntity getEntity(int version) {
        return introductionRepository.findByVersionAndDeletedAtIsNull(version)
                .orElseThrow(() -> new BusinessException(IntroductionErrorCode.INTRODUCTION_NOT_FOUND));
    }
}
