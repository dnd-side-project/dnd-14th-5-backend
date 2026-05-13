package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupEntity;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.model.Group;
import com.dnd5.timoapi.domain.group.domain.model.GroupMember;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupRepository;
import com.dnd5.timoapi.domain.group.exception.GroupErrorCode;
import com.dnd5.timoapi.domain.group.presentation.request.GroupCreateRequest;
import com.dnd5.timoapi.domain.group.presentation.request.GroupUpdateRequest;
import com.dnd5.timoapi.domain.group.presentation.response.GroupCreateResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupSummaryResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupCreateResponse createGroup(GroupCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        String code = generateUniqueCode();
        Group group = Group.create(code, request.name(), request.type(), request.image());
        GroupEntity savedGroup = groupRepository.save(GroupEntity.from(group));
        GroupMember ownerMember = GroupMember.create(savedGroup.getId(), userId, GroupMemberRole.OWNER);
        groupMemberRepository.save(GroupMemberEntity.from(ownerMember));
        return GroupCreateResponse.from(savedGroup.toModel());
    }

    @Transactional(readOnly = true)
    public List<GroupSummaryResponse> getMyGroups() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<GroupMemberEntity> myMemberships = groupMemberRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        return myMemberships.stream()
                .map(member -> {
                    GroupEntity groupEntity = getGroupEntity(member.getGroupId());
                    int memberCount = (int) groupMemberRepository.countByGroupIdAndDeletedAtIsNull(groupEntity.getId());
                    return GroupSummaryResponse.of(groupEntity.toModel(), memberCount, member.getRole());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long groupId, String code) {
        Long userId = SecurityUtil.getCurrentUserId();
        GroupEntity groupEntity = getGroupEntity(groupId);

        boolean isMember = groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId);
        GroupMemberRole myRole = null;

        if (code != null) {
            if (!groupEntity.getCode().equals(code)) {
                throw new BusinessException(GroupErrorCode.GROUP_ACCESS_DENIED);
            }
            if (isMember) {
                myRole = groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)
                        .map(GroupMemberEntity::getRole)
                        .orElse(null);
            }
        } else {
            if (!isMember) {
                throw new BusinessException(GroupErrorCode.GROUP_ACCESS_DENIED);
            }
            myRole = groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)
                    .map(GroupMemberEntity::getRole)
                    .orElse(null);
        }

        int memberCount = (int) groupMemberRepository.countByGroupIdAndDeletedAtIsNull(groupId);
        return GroupResponse.of(groupEntity.toModel(), memberCount, isMember, myRole);
    }

    public void updateGroup(Long groupId, GroupUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        GroupMemberEntity member = getGroupMember(groupId, userId);
        assertOwner(member);
        GroupEntity groupEntity = getGroupEntity(groupId);
        groupEntity.update(request.name(), request.image());
    }

    public void deleteGroup(Long groupId) {
        Long userId = SecurityUtil.getCurrentUserId();
        GroupMemberEntity member = getGroupMember(groupId, userId);
        assertOwner(member);
        GroupEntity groupEntity = getGroupEntity(groupId);
        groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(groupId).forEach(GroupMemberEntity::softDelete);
        groupEntity.softDelete();
    }

    public void joinGroup(Long groupId) {
        Long userId = SecurityUtil.getCurrentUserId();
        getGroupEntity(groupId);

        if (groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)) {
            throw new BusinessException(GroupErrorCode.GROUP_ALREADY_JOINED);
        }

        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .ifPresentOrElse(
                        existing -> {
                            existing.restore();
                        },
                        () -> groupMemberRepository.save(
                                GroupMemberEntity.from(GroupMember.create(groupId, userId, GroupMemberRole.MEMBER))
                        )
                );
    }

    public void leaveGroup(Long groupId) {
        Long userId = SecurityUtil.getCurrentUserId();
        GroupMemberEntity member = getGroupMember(groupId, userId);

        if (member.getRole() == GroupMemberRole.OWNER) {
            handleOwnerLeave(groupId, member);
        } else {
            member.softDelete();
        }
    }

    private void handleOwnerLeave(Long groupId, GroupMemberEntity ownerMember) {
        long count = groupMemberRepository.countByGroupIdAndDeletedAtIsNull(groupId);
        if (count == 1) {
            getGroupEntity(groupId).softDelete();
            ownerMember.softDelete();
        } else {
            groupMemberRepository
                    .findTopByGroupIdAndRoleAndDeletedAtIsNullOrderByCreatedAtAsc(groupId, GroupMemberRole.MEMBER)
                    .ifPresent(GroupMemberEntity::promoteToOwner);
            ownerMember.softDelete();
        }
    }

    private GroupEntity getGroupEntity(Long groupId) {
        return groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.GROUP_NOT_FOUND));
    }

    private GroupMemberEntity getGroupMember(Long groupId, Long userId) {
        return groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.GROUP_MEMBER_NOT_FOUND));
    }

    private void assertOwner(GroupMemberEntity member) {
        if (member.getRole() != GroupMemberRole.OWNER) {
            throw new BusinessException(GroupErrorCode.GROUP_FORBIDDEN);
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
