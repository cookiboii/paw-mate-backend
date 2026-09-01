package com.kindtail.adoptmate.post.controller;

import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequestDto;
import com.kindtail.adoptmate.post.dto.PostResponseDto;
import com.kindtail.adoptmate.post.dto.PostUpdateRequestDto;
import com.kindtail.adoptmate.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController implements PostControllerDocs {

    private final PostService postService;

    @Override
    @PostMapping("/create")
    public ResponseEntity<CommonResDto> createPost(@Valid @RequestBody PostCreateRequestDto dto) {
        Post post = postService.createPost(dto);
        PostResponseDto responseDto = PostResponseDto.from(post);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "글쓰기완료", responseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(commonResDto);
    }

    @Override
    @GetMapping("/list")
    public ResponseEntity<CommonResDto> getPostList(Pageable pageable) {
        Page<PostResponseDto> postPage = postService.getAllPosts(pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "조회완료", postPage);
        return ResponseEntity.ok(commonResDto);
    }

    @Override
    @GetMapping("/cursor")
    public ResponseEntity<CommonResDto> getPostsByCursor(
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size
    ) {
        Slice<PostResponseDto> postSlice = postService.getPostsByCursor(lastPostId, size);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "조회완료", postSlice);
        return ResponseEntity.ok(commonResDto);
    }

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResDto> getPostById(@PathVariable Long postId) {
        PostResponseDto post = postService.getPost(postId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "조회완료", post);
        return ResponseEntity.ok(commonResDto);
    }

    @Override
    @DeleteMapping("/{postId}")
    public ResponseEntity<CommonResDto> deletePostById(@PathVariable Long postId) {
        postService.deletePost(postId);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "삭제완료", null);
        return ResponseEntity.ok(commonResDto);
    }

    @Override
    @PutMapping("/{postId}")
    public ResponseEntity<CommonResDto> updatePost(@PathVariable Long postId, @Valid @RequestBody PostUpdateRequestDto dto) {
        PostResponseDto post = postService.updatePost(postId, dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "글수정완료", post);
        return ResponseEntity.ok(commonResDto);
    }
}
