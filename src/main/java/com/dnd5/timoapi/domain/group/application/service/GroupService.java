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
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
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
import java.util.Set;
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
    public GroupResponse getGroupByCode(String code) {
        Long userId = SecurityUtil.getCurrentUserId();
        GroupEntity groupEntity = groupRepository.findByCodeAndDeletedAtIsNull(code)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.GROUP_NOT_FOUND));

        Long groupId = groupEntity.getId();
        GroupMemberRole myRole = groupMemberRepository
                .findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)
                .map(GroupMemberEntity::getRole)
                .orElse(null);

        int memberCount = (int) groupMemberRepository.countByGroupIdAndDeletedAtIsNull(groupId);
        return GroupResponse.of(groupEntity.toModel(), memberCount, myRole);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<GroupMemberEntity> myMemberships = groupMemberRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        Set<Long> myGroupIds = myMemberships.stream().map(GroupMemberEntity::getGroupId).collect(Collectors.toSet());

        List<GroupResponse> myGroups = myMemberships.stream()
                .map(member -> {
                    GroupEntity groupEntity = getGroupEntity(member.getGroupId());
                    int memberCount = (int) groupMemberRepository.countByGroupIdAndDeletedAtIsNull(groupEntity.getId());
                    return GroupResponse.of(groupEntity.toModel(), memberCount, member.getRole());
                })
                .collect(Collectors.toList());

        groupRepository.findAllByTypeAndDeletedAtIsNull(GroupType.CHARACTER).stream()
                .filter(g -> !myGroupIds.contains(g.getId()))
                .map(g -> {
                    int memberCount = (int) groupMemberRepository.countByGroupIdAndDeletedAtIsNull(g.getId());
                    return GroupResponse.of(g.toModel(), memberCount, null);
                })
                .forEach(myGroups::add);

        return myGroups;
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
        GroupEntity groupEntity = getGroupEntity(groupId);
        if (groupEntity.getType() == GroupType.CHARACTER) {
            throw new BusinessException(GroupErrorCode.GROUP_CHARACTER_IMMUTABLE);
        }
        GroupMemberEntity member = getGroupMember(groupId, userId);
        assertOwner(member);
        groupEntity.update(request.name(), request.image());
    }

    public void deleteGroup(Long groupId) {
        Long userId = SecurityUtil.getCurrentUserId();
        GroupEntity groupEntity = getGroupEntity(groupId);
        if (groupEntity.getType() == GroupType.CHARACTER) {
            throw new BusinessException(GroupErrorCode.GROUP_CHARACTER_IMMUTABLE);
        }
        GroupMemberEntity member = getGroupMember(groupId, userId);
        assertOwner(member);
        groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(groupId).forEach(GroupMemberEntity::softDelete);
        groupEntity.softDelete();
    }

    public void joinGroupByCode(String code) {
        Long userId = SecurityUtil.getCurrentUserId();
        GroupEntity groupEntity = groupRepository.findByCodeAndDeletedAtIsNull(code)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.GROUP_NOT_FOUND));
        if (groupEntity.getType() == GroupType.CHARACTER) {
            throw new BusinessException(GroupErrorCode.GROUP_INVALID_CATEGORY);
        }
        Long groupId = groupEntity.getId();
        if (groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)) {
            throw new BusinessException(GroupErrorCode.GROUP_ALREADY_JOINED);
        }
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .ifPresentOrElse(
                        GroupMemberEntity::restoreAsMember,
                        () -> groupMemberRepository.save(
                                GroupMemberEntity.from(GroupMember.create(groupId, userId, GroupMemberRole.MEMBER))
                        )
                );
    }

    public void joinCharacterGroup() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.GROUP_MEMBER_NOT_FOUND));
        ZtpiCategory category = user.getCategory();
        if (category == null) {
            throw new BusinessException(GroupErrorCode.GROUP_CATEGORY_NOT_SET);
        }
        GroupEntity groupEntity = groupRepository.findByTypeAndCategoryAndDeletedAtIsNull(GroupType.CHARACTER, category)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.GROUP_NOT_FOUND));
        Long groupId = groupEntity.getId();
        if (groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)) {
            throw new BusinessException(GroupErrorCode.GROUP_ALREADY_JOINED);
        }
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .ifPresentOrElse(
                        GroupMemberEntity::restoreAsMember,
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

        if (groupEntity.getType() == GroupType.CHARACTER) {
            return getTodayReflectionsForCharacterGroup(groupEntity, today, sort);
        }
        return getTodayReflectionsForFriendGroup(groupId, userId, today, sort);
    }

    private List<GroupTodayReflectionItem> getTodayReflectionsForCharacterGroup(
            GroupEntity groupEntity, LocalDate today, GroupReflectionSort sort) {
        List<Long> memberUserIds = groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(groupEntity.getId())
                .stream().map(GroupMemberEntity::getUserId).toList();

        if (memberUserIds.isEmpty()) {
            return List.of();
        }

        Map<Long, UserEntity> userMap = userRepository.findAllById(memberUserIds)
                .stream().collect(Collectors.toMap(UserEntity::getId, u -> u));

        Map<Long, ReflectionEntity> reflectionByUserId = reflectionRepository
                .findAllByDateAndUserIdIn(today, memberUserIds)
                .stream().collect(Collectors.toMap(ReflectionEntity::getUserId, r -> r));

        List<Long> questionIds = reflectionByUserId.values().stream()
                .map(ReflectionEntity::getQuestionId).distinct().toList();
        Map<Long, ReflectionQuestionEntity> questionMap = questionIds.isEmpty() ? Map.of() :
                reflectionQuestionRepository.findAllById(questionIds)
                        .stream().collect(Collectors.toMap(ReflectionQuestionEntity::getId, q -> q));

        Comparator<Long> comparator = switch (sort) {
            case STREAK -> Comparator.comparingInt((Long uid) -> {
                UserEntity u = userMap.get(uid);
                return u != null ? u.getStreakDays() : 0;
            }).reversed();
            case TOTAL -> Comparator.comparingInt((Long uid) -> {
                UserEntity u = userMap.get(uid);
                return u != null ? u.getTotalDays() : 0;
            }).reversed();
            default -> (a, b) -> {
                ReflectionEntity ra = reflectionByUserId.get(a);
                ReflectionEntity rb = reflectionByUserId.get(b);
                if (ra == null && rb == null) return 0;
                if (ra == null) return 1;
                if (rb == null) return -1;
                return rb.getCreatedAt().compareTo(ra.getCreatedAt());
            };
        };

        return memberUserIds.stream()
                .sorted(comparator)
                .map(uid -> {
                    UserEntity user = userMap.get(uid);
                    ReflectionEntity reflection = reflectionByUserId.get(uid);
                    ReflectionQuestionEntity question = reflection != null ? questionMap.get(reflection.getQuestionId()) : null;
                    return new GroupTodayReflectionItem(
                            uid,
                            user != null ? user.getNickname() : null,
                            user != null ? user.getCategory() : null,
                            question != null ? question.getContent() : null,
                            question != null ? question.getCategory() : null,
                            reflection != null ? reflection.getAnswerText() : null,
                            user != null ? user.getStreakDays() : 0,
                            user != null ? user.getTotalDays() : 0
                    );
                })
                .toList();
    }

    private List<GroupTodayReflectionItem> getTodayReflectionsForFriendGroup(
            Long groupId, Long userId, LocalDate today, GroupReflectionSort sort) {
        if (!groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)) {
            throw new BusinessException(GroupErrorCode.GROUP_ACCESS_DENIED);
        }

        List<Long> memberUserIds = groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(groupId)
                .stream()
                .map(GroupMemberEntity::getUserId)
                .toList();

        if (memberUserIds.isEmpty()) {
            return List.of();
        }

        Map<Long, UserEntity> userMap = userRepository.findAllById(memberUserIds)
                .stream().collect(Collectors.toMap(UserEntity::getId, u -> u));

        Map<Long, ReflectionEntity> reflectionByUserId = reflectionRepository.findAllByDateAndUserIdIn(today, memberUserIds)
                .stream().collect(Collectors.toMap(ReflectionEntity::getUserId, r -> r));

        List<Long> questionIds = reflectionByUserId.values().stream()
                .map(ReflectionEntity::getQuestionId).distinct().toList();
        Map<Long, ReflectionQuestionEntity> questionMap = questionIds.isEmpty() ? Map.of() :
                reflectionQuestionRepository.findAllById(questionIds)
                        .stream().collect(Collectors.toMap(ReflectionQuestionEntity::getId, q -> q));

        Comparator<Long> comparator = switch (sort) {
            case STREAK -> Comparator.comparingInt((Long uid) -> {
                UserEntity u = userMap.get(uid);
                return u != null ? u.getStreakDays() : 0;
            }).reversed();
            case TOTAL -> Comparator.comparingInt((Long uid) -> {
                UserEntity u = userMap.get(uid);
                return u != null ? u.getTotalDays() : 0;
            }).reversed();
            default -> (a, b) -> {
                ReflectionEntity ra = reflectionByUserId.get(a);
                ReflectionEntity rb = reflectionByUserId.get(b);
                if (ra == null && rb == null) return 0;
                if (ra == null) return 1;
                if (rb == null) return -1;
                return rb.getCreatedAt().compareTo(ra.getCreatedAt());
            };
        };

        return memberUserIds.stream()
                .sorted(comparator)
                .map(uid -> {
                    UserEntity user = userMap.get(uid);
                    ReflectionEntity reflection = reflectionByUserId.get(uid);
                    ReflectionQuestionEntity question = reflection != null ? questionMap.get(reflection.getQuestionId()) : null;
                    return new GroupTodayReflectionItem(
                            uid,
                            user != null ? user.getNickname() : null,
                            user != null ? user.getCategory() : null,
                            question != null ? question.getContent() : null,
                            question != null ? question.getCategory() : null,
                            reflection != null ? reflection.getAnswerText() : null,
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
