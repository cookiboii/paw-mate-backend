package com.kindtail.adoptmate.comment.service;

import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.comment.dto.CommentDto;
import com.kindtail.adoptmate.comment.dto.CommentResponseDto;
import com.kindtail.adoptmate.comment.dto.CommentUpdateDto;
import com.kindtail.adoptmate.comment.repository.CommentRepository;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public CommentResponseDto addComment(Long id, CommentDto commentDto) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String email = userInfo.getEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Comment parent = null;
        if (commentDto.parentId() != null) {
            parent = commentRepository.findById(commentDto.parentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        }

        Comment comment = Comment.builder()
                .content(commentDto.content())
                .parent(parent)
                .post(post)
                .member(member)
                .build();
        commentRepository.save(comment);
        return CommentResponseDto.fromComment(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        List<Comment> rootComments = commentRepository.findByPostAndParentIsNull(post);
        return rootComments.stream()
                .map(CommentResponseDto::fromComment)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long id) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        boolean isAuthor = comment.getMember().getEmail().equals(userInfo.getEmail());
        boolean isAdmin = userInfo.getRole() == Role.ADMIN || "ADMIN".equals(userInfo.getRole().name());

        if (!isAuthor && !isAdmin) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_AUTHOR);
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentUpdateDto dto) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        boolean isAuthor = comment.getMember().getEmail().equals(userInfo.getEmail());
        boolean isAdmin = userInfo.getRole() == Role.ADMIN || "ADMIN".equals(userInfo.getRole().name());

        if (!isAuthor && !isAdmin) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_AUTHOR);
        }

        comment.updateComment(dto.content());
        return CommentResponseDto.fromComment(comment);
    }
}
