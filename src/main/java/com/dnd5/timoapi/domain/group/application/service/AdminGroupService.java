package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupEntity;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.model.Group;
import com.dnd5.timoapi.domain.group.domain.model.GroupMember;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupRepository;
import com.dnd5.timoapi.domain.group.exception.GroupErrorCode;
import com.dnd5.timoapi.domain.group.presentation.request.GroupCreateRequest;
import com.dnd5.timoapi.domain.group.presentation.response.GroupCreateResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminGroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupCreateResponse createGroup(GroupCreateRequest request) {
        String code = generateUniqueCode();
        Group group = Group.create(code, request.name(), request.type(), request.image(), null);
        GroupEntity savedGroup = groupRepository.save(GroupEntity.from(group));
        return GroupCreateResponse.from(savedGroup.toModel());
    }

    public void seedDummyGroups() {
        List<Long> userIds = List.of(1L, 2L, 3L, 4L, 5L);

        for (int i = 1; i <= 10; i++) {
            String code = generateUniqueCode();
            Group group = Group.create(code, "테스트 그룹 " + i, GroupType.FRIEND, null, null);
            GroupEntity savedGroup = groupRepository.save(GroupEntity.from(group));

            for (int j = 0; j < userIds.size(); j++) {
                GroupMemberRole role = (j == 0) ? GroupMemberRole.OWNER : GroupMemberRole.MEMBER;
                groupMemberRepository.save(GroupMemberEntity.from(
                        GroupMember.create(savedGroup.getId(), userIds.get(j), role)
                ));
            }
        }
    }

    public void deleteGroup(Long groupId) {
        GroupEntity groupEntity = getGroupEntity(groupId);
        groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(groupId).forEach(GroupMemberEntity::softDelete);
        groupEntity.softDelete();
    }

    private GroupEntity getGroupEntity(Long groupId) {
        return groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.GROUP_NOT_FOUND));
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (groupRepository.existsByCodeAndDeletedAtIsNull(code));
        return code;
    }
}
