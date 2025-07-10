package com.kindtail.adoptmate.post.service;

import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.controller.PostController;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequestDto;
import com.kindtail.adoptmate.post.dto.PostResponseDto;
import com.kindtail.adoptmate.post.repository.PostRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

  private  final PostRepository postRepository;
  private final MemberRepository memberRepository;
    public PostService(PostRepository postRepository, MemberRepository memberRepository) {
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
    }




    @Transactional
      public Post createPost(PostCreateRequestDto Dto) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        String email = userInfo.getEmail();

         Member member = memberRepository.findByEmail(email)
                 .orElseThrow(() -> new RuntimeException("Member not found"));
         Post post = Post.builder().
                      title(Dto.title())
                              .content(Dto.content())
                         .image(Dto.img())
                                 .member(member)
                 .build();

         return postRepository.save(post);

      }
     @Transactional(readOnly =true)
      public Page<PostResponseDto> getAllPosts(Pageable pageable) {
         Page<Post> posts = postRepository.findAll(pageable);
          return posts.map(PostResponseDto::from);
      }

      public void DeletePost(Long postId) {
        postRepository.deleteById(postId);
      }

  }




