package com.dnd5.timoapi.domain.test.application.service;

import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.domain.entity.TimePerspectiveCategoryEntity;
import com.dnd5.timoapi.domain.test.domain.model.TimePerspectiveCategory;
import com.dnd5.timoapi.domain.test.domain.repository.TimePerspectiveCategoryRepository;
import com.dnd5.timoapi.domain.test.exception.TimePerspectiveCategoryErrorCode;
import com.dnd5.timoapi.domain.test.presentation.request.TestUpdateRequest;
import com.dnd5.timoapi.domain.test.presentation.request.TimePerspectiveCategoryCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.request.TimePerspectiveCategoryUpdateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.TimePerspectiveCategoryResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TimePerspectiveCategoryService {

    private final TimePerspectiveCategoryRepository timePerspectiveCategoryRepository;

    public void create(TimePerspectiveCategoryCreateRequest request) {
        TimePerspectiveCategory model = request.toModel();
        timePerspectiveCategoryRepository.save(TimePerspectiveCategoryEntity.from(model));
    }

    public List<TimePerspectiveCategoryResponse> findAll() {
        return timePerspectiveCategoryRepository.findAll().stream()
                .map(TimePerspectiveCategoryEntity::toModel)
                .map(TimePerspectiveCategoryResponse::from)
                .toList();
    }

    public void update(@Positive Long categoryId, @Valid TimePerspectiveCategoryUpdateRequest request) {
        TimePerspectiveCategoryEntity timePerspectiveCategoryEntity = getTimePerspectiveCategoryEntity(categoryId);
        timePerspectiveCategoryEntity.update(request.name(), request.characterName(), request.personality(), request.description());
    }

    private TimePerspectiveCategoryEntity getTimePerspectiveCategoryEntity(@Positive Long categoryId) {
        return timePerspectiveCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(TimePerspectiveCategoryErrorCode.TIME_PERSPECTIVE_CATEGORY_NOT_FOUND));
    }

}
