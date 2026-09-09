package com.kindtail.adoptmate.post.controller;

import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.common.dto.SuccessCode;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequestDto;
import com.kindtail.adoptmate.post.dto.PostResponseDto;
import com.kindtail.adoptmate.post.dto.PostUpdateRequestDto;
import com.kindtail.adoptmate.post.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Validated
public class PostController implements PostControllerDocs {

    private final PostService postService;

    @Override
    @PostMapping("/create")
    public ResponseEntity<CommonResDto<PostResponseDto>> createPost(@Valid @RequestBody PostCreateRequestDto dto) {
        PostResponseDto responseDto = postService.createPost(dto);
        return CommonResDto.toResponseEntity(SuccessCode.POST_CREATE_SUCCESS, responseDto);
    }

    @Override
    @GetMapping("/list")
    public ResponseEntity<CommonResDto<Page<PostResponseDto>>> getPostList(Pageable pageable) {
        Page<PostResponseDto> postPage = postService.getAllPosts(pageable);
        return CommonResDto.toResponseEntity(SuccessCode.POST_LIST_SUCCESS, postPage);
    }

    @Override
    @GetMapping("/cursor")
    public ResponseEntity<CommonResDto<Slice<PostResponseDto>>> getPostsByCursor(
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        Slice<PostResponseDto> postSlice = postService.getPostsByCursor(lastPostId, size);
        return CommonResDto.toResponseEntity(SuccessCode.POST_LIST_SUCCESS, postSlice);
    }

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResDto<PostResponseDto>> getPostById(@PathVariable Long postId) {
        PostResponseDto post = postService.getPost(postId);
        return CommonResDto.toResponseEntity(SuccessCode.POST_DETAIL_SUCCESS, post);
    }

    @Override
    @DeleteMapping("/{postId}")
    public ResponseEntity<CommonResDto<Void>> deletePostById(@PathVariable Long postId) {
        postService.deletePost(postId);
        return CommonResDto.toResponseEntity(SuccessCode.POST_DELETE_SUCCESS);
    }

    @Override
    @PutMapping("/{postId}")
    public ResponseEntity<CommonResDto<PostResponseDto>> updatePost(@PathVariable Long postId, @Valid @RequestBody PostUpdateRequestDto dto) {
        PostResponseDto post = postService.updatePost(postId, dto);
        return CommonResDto.toResponseEntity(SuccessCode.POST_UPDATE_SUCCESS, post);
    }
}
