package com.dnd5.timoapi.domain.test.application.service;

import com.dnd5.timoapi.domain.test.domain.repository.TimePerspectiveCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TimePerspectiveCategoryService {

    private final TimePerspectiveCategoryRepository timePerspectiveCategoryRepository;

}
