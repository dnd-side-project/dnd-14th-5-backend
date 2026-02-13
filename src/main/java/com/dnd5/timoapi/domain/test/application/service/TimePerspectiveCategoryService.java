package com.dnd5.timoapi.domain.test.application.service;

import com.dnd5.timoapi.domain.test.domain.entity.TimePerspectiveCategoryEntity;
import com.dnd5.timoapi.domain.test.domain.model.TimePerspectiveCategory;
import com.dnd5.timoapi.domain.test.domain.repository.TimePerspectiveCategoryRepository;
import com.dnd5.timoapi.domain.test.presentation.request.TimePerspectiveCategoryCreateRequest;
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

}
