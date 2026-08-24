package com.kindtail.adoptmate.post.service;

import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequestDto;
import com.kindtail.adoptmate.post.dto.PostResponseDto;
import com.kindtail.adoptmate.post.dto.PostUpdateRequestDto;
import com.kindtail.adoptmate.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Post createPost(PostCreateRequestDto dto) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        String email = userInfo.getEmail();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = Post.builder()
                .title(dto.title())
                .content(dto.content())
                .image(dto.img())
                .member(member)
                .build();

        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(PostResponseDto::from);
    }

    @Transactional
    public void deletePost(Long postId) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        boolean isAuthor = post.getMember().getEmail().equals(userInfo.getEmail());
        boolean isAdmin = userInfo.getRole() == Role.ADMIN || "ADMIN".equals(userInfo.getRole().name());

        if (!isAuthor && !isAdmin) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_AUTHOR);
        }

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return PostResponseDto.from(post);
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostUpdateRequestDto dto) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        boolean isAuthor = post.getMember().getEmail().equals(userInfo.getEmail());
        boolean isAdmin = userInfo.getRole() == Role.ADMIN || "ADMIN".equals(userInfo.getRole().name());

        if (!isAuthor && !isAdmin) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_AUTHOR);
        }

        post.updatePost(dto.title(), dto.content(), dto.img());
        return PostResponseDto.from(post);
    }
}
