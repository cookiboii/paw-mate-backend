package com.kindtail.adoptmate.post.service;

import com.kindtail.adoptmate.auth.SecurityUtil;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequestDto;
import com.kindtail.adoptmate.post.dto.PostResponseDto;
import com.kindtail.adoptmate.post.dto.PostUpdateRequestDto;
import com.kindtail.adoptmate.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @CacheEvict(value = "posts", allEntries = true)
    @Transactional
    public Post createPost(PostCreateRequestDto dto) {
        String email = SecurityUtil.getCurrentUserEmail();

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

    @Cacheable(value = "posts", key = "'page-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(PostResponseDto::from);
    }

    @Cacheable(value = "posts", key = "'cursor-' + (#lastPostId == null ? 0 : #lastPostId) + '-' + #size")
    @Transactional(readOnly = true)
    public Slice<PostResponseDto> getPostsByCursor(Long lastPostId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<Post> posts = postRepository.findPostsByCursor(lastPostId, pageable);
        return posts.map(PostResponseDto::from);
    }

    @CacheEvict(value = "posts", allEntries = true)
    @Transactional
    public void deletePost(Long postId) {
        TokenUserInfo userInfo = SecurityUtil.getCurrentUserInfo();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        post.validateAuthorOrAdmin(userInfo);
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return PostResponseDto.from(post);
    }

    @CacheEvict(value = "posts", allEntries = true)
    @Transactional
    public PostResponseDto updatePost(Long postId, PostUpdateRequestDto dto) {
        TokenUserInfo userInfo = SecurityUtil.getCurrentUserInfo();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        post.validateAuthorOrAdmin(userInfo);
        post.updatePost(dto.title(), dto.content(), dto.img());
        return PostResponseDto.from(post);
    }
}
