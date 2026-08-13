package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberReflectionLikeEntity;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberReflectionLikeRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberReflectionPrivateRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberRepository;
import com.dnd5.timoapi.domain.group.exception.GroupErrorCode;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupReflectionLikeService {

    private final GroupMemberRepository groupMemberRepository;
    private final ReflectionRepository reflectionRepository;
    private final GroupMemberReflectionPrivateRepository groupMemberReflectionPrivateRepository;
    private final GroupMemberReflectionLikeRepository groupMemberReflectionLikeRepository;

    public void like(Long groupId, Long reflectionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        getAccessibleGroupReflection(groupId, reflectionId, userId);

        if (groupMemberReflectionLikeRepository.existsByGroupIdAndReflectionIdAndUserId(groupId, reflectionId, userId)) {
            throw new BusinessException(GroupErrorCode.GROUP_REFLECTION_LIKE_ALREADY_EXISTS);
        }

        groupMemberReflectionLikeRepository.save(
                new GroupMemberReflectionLikeEntity(groupId, reflectionId, userId));
    }

    public void unlike(Long groupId, Long reflectionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (!groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)) {
            throw new BusinessException(GroupErrorCode.GROUP_ACCESS_DENIED);
        }

        GroupMemberReflectionLikeEntity like = groupMemberReflectionLikeRepository
                .findByGroupIdAndReflectionIdAndUserId(groupId, reflectionId, userId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.GROUP_REFLECTION_LIKE_NOT_FOUND));

        groupMemberReflectionLikeRepository.delete(like);
    }

    private ReflectionEntity getAccessibleGroupReflection(Long groupId, Long reflectionId, Long viewerId) {
        if (!groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)) {
            throw new BusinessException(GroupErrorCode.GROUP_ACCESS_DENIED);
        }

        ReflectionEntity reflectionEntity = reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_NOT_FOUND));

        GroupMemberEntity authorMember = groupMemberRepository
                .findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, reflectionEntity.getUserId())
                .orElseThrow(() -> new BusinessException(ReflectionErrorCode.REFLECTION_NOT_FOUND));

        if (reflectionEntity.getDate().isBefore(authorMember.getCreatedAt().toLocalDate())) {
            throw new BusinessException(ReflectionErrorCode.REFLECTION_NOT_FOUND);
        }

        boolean isPrivate = groupMemberReflectionPrivateRepository.existsByGroupIdAndReflectionId(groupId, reflectionId);
        if (isPrivate && !reflectionEntity.getUserId().equals(viewerId)) {
            throw new BusinessException(GroupErrorCode.GROUP_ACCESS_DENIED);
        }

        return reflectionEntity;
    }
}
