package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupEntity;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.model.Group;
import com.dnd5.timoapi.domain.group.domain.model.GroupMember;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupReflectionSort;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupRepository;
import com.dnd5.timoapi.domain.group.exception.GroupErrorCode;
import com.dnd5.timoapi.domain.group.presentation.request.GroupCreateRequest;
import com.dnd5.timoapi.domain.group.presentation.request.GroupUpdateRequest;
import com.dnd5.timoapi.domain.group.presentation.response.GroupCreateResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupDetailResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupTodayReflectionItem;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ReflectionRepository reflectionRepository;
    private final ReflectionQuestionRepository reflectionQuestionRepository;
    private final UserRepository userRepository;

    public GroupCreateResponse createGroup(GroupCreateRequest request) {
        if (request.type() == GroupType.CHARACTER) {
            throw new BusinessException(GroupErrorCode.GROUP_INVALID_CATEGORY);
        }
        Long userId = SecurityUtil.getCurrentUserId();
        String code = generateUniqueCode();
        Group group = Group.create(code, request.name(), request.type(), request.image(), null);
        GroupEntity savedGroup = groupRepository.save(GroupEntity.from(group));
        GroupMember ownerMember = GroupMember.create(savedGroup.getId(), userId, GroupMemberRole.OWNER);
        groupMemberRepository.save(GroupMemberEntity.from(ownerMember));
        return GroupCreateResponse.from(savedGroup.toModel());
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<GroupMemberEntity> myMemberships = groupMemberRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        return myMemberships.stream()
                .map(member -> {
                    GroupEntity groupEntity = getGroupEntity(member.getGroupId());
                    int memberCount = (int) groupMemberRepository.countByGroupIdAndDeletedAtIsNull(groupEntity.getId());
                    return GroupResponse.of(groupEntity.toModel(), memberCount, member.getRole());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupDetailResponse getGroup(Long groupId, String code) {
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
        return GroupDetailResponse.of(groupEntity.toModel(), memberCount, isMember, myRole);
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

    @Transactional(readOnly = true)
    public List<GroupTodayReflectionItem> getTodayReflections(Long groupId, GroupReflectionSort sort) {
        Long userId = SecurityUtil.getCurrentUserId();
        GroupEntity groupEntity = getGroupEntity(groupId);
        LocalDate today = LocalDate.now();

        List<ReflectionEntity> reflections;

        if (groupEntity.getType() == GroupType.CHARACTER) {
            reflections = reflectionRepository.findAllByDateAndQuestionCategory(today, groupEntity.getCategory());
        } else {
            List<Long> memberUserIds = groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(groupId)
                    .stream()
                    .map(GroupMemberEntity::getUserId)
                    .toList();

            if (!groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)) {
                throw new BusinessException(GroupErrorCode.GROUP_ACCESS_DENIED);
            }

            if (memberUserIds.isEmpty()) {
                return List.of();
            }
            reflections = reflectionRepository.findAllByDateAndUserIdIn(today, memberUserIds);
        }

        if (reflections.isEmpty()) {
            return List.of();
        }

        List<Long> questionIds = reflections.stream().map(ReflectionEntity::getQuestionId).distinct().toList();
        List<Long> userIds = reflections.stream().map(ReflectionEntity::getUserId).distinct().toList();

        Map<Long, ReflectionQuestionEntity> questionMap = reflectionQuestionRepository.findAllById(questionIds)
                .stream().collect(Collectors.toMap(q -> q.getId(), q -> q));
        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds)
                .stream().collect(Collectors.toMap(u -> u.getId(), u -> u));

        Comparator<ReflectionEntity> comparator = switch (sort) {
            case STREAK -> Comparator.comparingInt((ReflectionEntity r) -> {
                UserEntity u = userMap.get(r.getUserId());
                return u != null ? u.getStreakDays() : 0;
            }).reversed();
            case TOTAL -> Comparator.comparingInt((ReflectionEntity r) -> {
                UserEntity u = userMap.get(r.getUserId());
                return u != null ? u.getTotalDays() : 0;
            }).reversed();
            default -> Comparator.comparing(ReflectionEntity::getCreatedAt, Comparator.reverseOrder());
        };

        return reflections.stream()
                .sorted(comparator)
                .map(r -> {
                    ReflectionQuestionEntity question = questionMap.get(r.getQuestionId());
                    UserEntity user = userMap.get(r.getUserId());
                    return new GroupTodayReflectionItem(
                            r.getUserId(),
                            user != null ? user.getNickname() : null,
                            user != null ? user.getCategory() : null,
                            question != null ? question.getContent() : null,
                            question != null ? question.getCategory() : null,
                            r.getAnswerText(),
                            user != null ? user.getStreakDays() : 0,
                            user != null ? user.getTotalDays() : 0
                    );
                })
                .toList();
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
