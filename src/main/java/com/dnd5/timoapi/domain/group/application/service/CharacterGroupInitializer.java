package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupEntity;
import com.dnd5.timoapi.domain.group.domain.model.Group;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.group.domain.repository.GroupRepository;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Order(1)
@RequiredArgsConstructor
public class CharacterGroupInitializer implements ApplicationRunner {

    private final GroupRepository groupRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (ZtpiCategory category : ZtpiCategory.values()) {
            if (!groupRepository.existsByTypeAndCategoryAndDeletedAtIsNull(GroupType.CHARACTER, category)) {
                String code = generateUniqueCode();
                Group group = Group.create(code, category.getCharacter(), GroupType.CHARACTER, null, category);
                groupRepository.save(GroupEntity.from(group));
            }
        }
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (groupRepository.existsByCodeAndDeletedAtIsNull(code));
        return code;
    }
}
