package com.kindtail.adoptmate.post.controller;

import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequestDto;
import com.kindtail.adoptmate.post.dto.PostResponseDto;
import com.kindtail.adoptmate.post.service.PostService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
public class PostController {


    private final PostService postService;
    public PostController(PostService postService) {
        this.postService = postService;
    }
    @PostMapping("/create")
    public ResponseEntity<CommonResDto> createPost (@RequestBody PostCreateRequestDto dto) {
           Post post    = postService.createPost(dto);
           CommonResDto commonResDto = new CommonResDto(HttpStatus.OK,"글쓰기완료",post );

           return ResponseEntity.ok(commonResDto);

    }

    @GetMapping("/list")
    public ResponseEntity<CommonResDto> getPostList (Pageable pageable) {
    Page <PostResponseDto>postPage = postService.getAllPosts(pageable);
    CommonResDto commonResDto = new CommonResDto(HttpStatus.OK,"조회완료",postPage);
    return ResponseEntity.ok(commonResDto);
    }





}
