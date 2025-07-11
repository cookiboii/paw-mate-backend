package com.kindtail.adoptmate.post.service;

import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;

import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequestDto;
import com.kindtail.adoptmate.post.dto.PostResponseDto;
import com.kindtail.adoptmate.post.dto.PostUpdateRequestDto;
import com.kindtail.adoptmate.post.repository.PostRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
                 .createdAt(Dto.dateTime())
                 .build();

         return postRepository.save(post);

      }
     @Transactional(readOnly =true)
      public Page<PostResponseDto> getAllPosts(Pageable pageable) {
         Page<Post> posts = postRepository.findAll(pageable);
          return posts.map(PostResponseDto::from);
      }
    @Transactional
      public void DeletePost(Long postId) {
        postRepository.deleteById(postId);
      }

     @Transactional
      public PostResponseDto getPost(Long postId) {
            Post post = postRepository.findById( postId).orElseThrow(() -> new IllegalArgumentException("존재하지않은 글"));
            return PostResponseDto.from(post);

      }
      @Transactional
      public PostResponseDto updatePost(Long postId, PostUpdateRequestDto Dto) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("업데이트 실패"));
           post.updatePost( Dto.title() ,Dto.content() ,Dto.img());
        return PostResponseDto.from(post);
    }





  }




