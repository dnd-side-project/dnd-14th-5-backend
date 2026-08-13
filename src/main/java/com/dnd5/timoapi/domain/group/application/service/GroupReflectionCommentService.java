package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberReflectionCommentEntity;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberReflectionCommentRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberReflectionPrivateRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberRepository;
import com.dnd5.timoapi.domain.group.exception.GroupErrorCode;
import com.dnd5.timoapi.domain.group.presentation.request.GroupReflectionCommentRequest;
import com.dnd5.timoapi.domain.group.presentation.response.GroupReflectionCommentResponse;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupReflectionCommentService {

    private final GroupMemberRepository groupMemberRepository;
    private final ReflectionRepository reflectionRepository;
    private final GroupMemberReflectionPrivateRepository groupMemberReflectionPrivateRepository;
    private final GroupMemberReflectionCommentRepository groupMemberReflectionCommentRepository;
    private final UserRepository userRepository;

    public void create(Long groupId, Long reflectionId, GroupReflectionCommentRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        getAccessibleGroupReflection(groupId, reflectionId, userId);

        groupMemberReflectionCommentRepository.save(
                new GroupMemberReflectionCommentEntity(groupId, reflectionId, userId, request.content()));
    }

    @Transactional(readOnly = true)
    public List<GroupReflectionCommentResponse> findAll(Long groupId, Long reflectionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        getAccessibleGroupReflection(groupId, reflectionId, userId);

        List<GroupMemberReflectionCommentEntity> comments = groupMemberReflectionCommentRepository
                .findAllByGroupIdAndReflectionIdAndDeletedAtIsNullOrderByCreatedAtAsc(groupId, reflectionId);

        if (comments.isEmpty()) {
            return List.of();
        }

        List<Long> commenterIds = comments.stream()
                .map(GroupMemberReflectionCommentEntity::getUserId).distinct().toList();
        Map<Long, UserEntity> userMap = userRepository.findAllById(commenterIds)
                .stream().collect(Collectors.toMap(UserEntity::getId, u -> u));

        return comments.stream()
                .map(comment -> {
                    UserEntity commenter = userMap.get(comment.getUserId());
                    return new GroupReflectionCommentResponse(
                            comment.getId(),
                            comment.getUserId(),
                            commenter != null ? commenter.getNickname() : null,
                            commenter != null ? commenter.getCategory() : null,
                            comment.getContent(),
                            comment.getCreatedAt()
                    );
                })
                .toList();
    }

    public void update(Long groupId, Long reflectionId, Long commentId, GroupReflectionCommentRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        GroupMemberReflectionCommentEntity comment = getOwnedComment(groupId, reflectionId, commentId, userId);
        comment.update(request.content());
    }

    public void delete(Long groupId, Long reflectionId, Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        GroupMemberReflectionCommentEntity comment = getOwnedComment(groupId, reflectionId, commentId, userId);
        comment.softDelete();
    }

    private GroupMemberReflectionCommentEntity getOwnedComment(
            Long groupId, Long reflectionId, Long commentId, Long userId) {
        GroupMemberReflectionCommentEntity comment = groupMemberReflectionCommentRepository
                .findByIdAndGroupIdAndReflectionIdAndDeletedAtIsNull(commentId, groupId, reflectionId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.GROUP_REFLECTION_COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(
                    GroupErrorCode.GROUP_REFLECTION_COMMENT_NOT_OWNER, commentId, comment.getUserId(), userId);
        }
        return comment;
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
