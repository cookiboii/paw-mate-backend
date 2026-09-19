package com.kindtail.adoptmate.comment.service;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.auth.CurrentUserProvider;
import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.comment.dto.CommentCreateRequest;
import com.kindtail.adoptmate.comment.dto.CommentResponse;
import com.kindtail.adoptmate.comment.dto.CommentUpdateRequest;
import com.kindtail.adoptmate.comment.repository.CommentRepository;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request) {
        String email = currentUserProvider.currentUserEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "Parent comment belongs to a different post.");
            }
            if (parent.getParent() != null) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "대댓글에는 답글을 작성할 수 없습니다.");
            }
            if (parent.isSecret()) {
                CustomUserDetails viewer = currentUserProvider.currentUser();
                Long viewerId = viewer.getId();
                boolean parentAuthor = parent.getMember() != null && Objects.equals(viewerId, parent.getMember().getId());
                boolean postAuthor = post.getMember() != null && Objects.equals(viewerId, post.getMember().getId());
                if (!viewer.isAdmin() && !parentAuthor && !postAuthor) {
                    throw new CustomException(ErrorCode.UNAUTHORIZED_AUTHOR);
                }
            }
        }

        Comment comment = Comment.builder()
                .content(request.content())
                .secret((parent != null && parent.isSecret()) || Boolean.TRUE.equals(request.secret()))
                .parent(parent)
                .post(post)
                .member(member)
                .build();
        commentRepository.save(comment);
        return CommentResponse.fromComment(comment, currentUserProvider.currentUser());
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long id, Pageable pageable) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        CustomUserDetails viewer = currentUserProvider.optionalCurrentUser().orElse(null);
        return commentRepository.findByPostAndParentIsNull(post, pageable)
                .map(comment -> CommentResponse.fromComment(comment, viewer));
    }

    @Transactional
    public void deleteComment(Long id) {
        CustomUserDetails userDetails = currentUserProvider.currentUser();

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        comment.validateAuthorOrAdmin(userDetails);
        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest dto) {
        CustomUserDetails userDetails = currentUserProvider.currentUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        comment.validateAuthor(userDetails);
        comment.updateComment(dto.content());
        return CommentResponse.fromComment(comment, userDetails);
    }
}
