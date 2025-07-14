package com.kindtail.adoptmate.comment.service;

import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.comment.dto.CommentDto;
import com.kindtail.adoptmate.comment.dto.CommentResponseDto;
import com.kindtail.adoptmate.comment.dto.CommentUpdateDto;
import com.kindtail.adoptmate.comment.repository.CommentRepository;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.repository.PostRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {


    private  final CommentRepository commentRepository;
    private  final MemberRepository memberRepository;
    private  final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, MemberRepository memberRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
    }
     @Transactional
    public CommentResponseDto addComment(Long Id, CommentDto commentDto) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String email = userInfo.getEmail();
     Member member = memberRepository.findByEmail(email).orElseThrow(()->new IllegalArgumentException("Member not found"));
     Post post = postRepository.findById(Id).orElseThrow(()->new IllegalArgumentException("Post not found"));
    
     Comment parent = null;

        if (commentDto.parentId() != null) {
             parent = commentRepository.findById(commentDto.parentId()).orElseThrow(()->new IllegalArgumentException("Parent not found"));

        }

         Comment comment =Comment.builder()
                 .content(commentDto.content())
                 .parent(parent)
                 .post(post)
                 .member(member)
                 .creationDate(LocalDateTime.now())
                 .build();
         commentRepository.save(comment);
         return CommentResponseDto.fromComment(comment);

    }
  @Transactional
    public List<CommentResponseDto> getComments(Long Id) {
       Post post = postRepository.findById(Id).orElseThrow(()->new IllegalArgumentException("Post not found"));
       List<Comment>   allComments = commentRepository.findByPost(post);
        return allComments.stream()
                .filter(comment -> comment.getParent() == null)
                .map(CommentResponseDto::fromComment)
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        // 🔐 현재 로그인한 사용자 정보 가져오기



         commentRepository.deleteById(id);
    }


    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentUpdateDto dto) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getMember().getEmail().equals(userInfo.getEmail()) &&
                !userInfo.getRole().equals("ADMIN")) {
            throw new SecurityException("본인 또는 관리자만 댓글을 수정할 수 있습니다.");
        }

        comment.updateComment(dto.content());
        return CommentResponseDto.fromComment(comment);
    }


}
