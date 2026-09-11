package com.kindtail.adoptmate.post.service;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.auth.SecurityUtil;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequest;
import com.kindtail.adoptmate.post.dto.PostResponse;
import com.kindtail.adoptmate.post.dto.PostUpdateRequest;
import com.kindtail.adoptmate.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public PostResponse createPost(PostCreateRequest dto) {
        String email = SecurityUtil.getCurrentUserEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = Post.builder()
                .title(dto.title())
                .content(dto.content())
                .image(dto.img())
                .member(member)
                .build();

        Post saved = postRepository.save(post);
        return PostResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public Slice<PostResponse> getPostsByCursor(Long lastPostId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<Post> posts = postRepository.findPostsByCursor(lastPostId, pageable);
        return posts.map(PostResponse::from);
    }

    @Transactional
    public void deletePost(Long postId) {
        CustomUserDetails userDetails = SecurityUtil.getCurrentUserDetails();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        post.validateAuthorOrAdmin(userDetails);
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest dto) {
        CustomUserDetails userDetails = SecurityUtil.getCurrentUserDetails();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        post.validateAuthorOrAdmin(userDetails);
        post.updatePost(dto.title(), dto.content(), dto.img());
        return PostResponse.from(post);
    }
}
