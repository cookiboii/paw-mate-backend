package com.kindtail.adoptmate.post.controller;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.common.dto.CommonResponse;
import com.kindtail.adoptmate.common.dto.SuccessCode;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequest;
import com.kindtail.adoptmate.post.dto.PostResponse;
import com.kindtail.adoptmate.post.dto.PostUpdateRequest;
import com.kindtail.adoptmate.post.dto.LikeResponse;
import com.kindtail.adoptmate.post.dto.BookmarkResponse;
import com.kindtail.adoptmate.post.service.PostService;
import com.kindtail.adoptmate.post.service.PostQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping({"/api/v1/posts", "/post"})
@RequiredArgsConstructor
@Validated
public class PostController implements PostControllerDocs {

    private final PostService postService;
    private final PostQueryService postQueryService;

    @Override
    @PostMapping({"", "/create"})
    public ResponseEntity<CommonResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PostResponse response = postService.createPost(dto, userDetails);
        return CommonResponse.toResponseEntity(SuccessCode.POST_CREATE_SUCCESS, response);
    }

    @Override
    @GetMapping({"", "/list"})
    public ResponseEntity<CommonResponse<Page<PostResponse>>> getPostList(Pageable pageable) {
        Page<PostResponse> postPage = postQueryService.getAllPosts(pageable);
        return CommonResponse.toResponseEntity(SuccessCode.POST_LIST_SUCCESS, postPage);
    }

    @Override
    @GetMapping("/cursor")
    public ResponseEntity<CommonResponse<Slice<PostResponse>>> getPostsByCursor(
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort
    ) {
        // Keep the legacy cursor call working for older clients that send none of
        // the new search parameters; all new cursor options use the richer query.
        Slice<PostResponse> postSlice = keyword == null && (sort == null || "latest".equalsIgnoreCase(sort))
                ? postQueryService.getPostsByCursor(lastPostId, size)
                : postQueryService.searchPosts(lastPostId, size, keyword, sort);
        return CommonResponse.toResponseEntity(SuccessCode.POST_LIST_SUCCESS, postSlice);
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<CommonResponse<LikeResponse>> likePost(@PathVariable Long postId) {
        return CommonResponse.toResponseEntity(SuccessCode.OK, postService.likePost(postId));
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<CommonResponse<LikeResponse>> unlikePost(@PathVariable Long postId) {
        return CommonResponse.toResponseEntity(SuccessCode.OK, postService.unlikePost(postId));
    }

    @PostMapping("/{postId}/bookmarks")
    public ResponseEntity<CommonResponse<BookmarkResponse>> bookmarkPost(@PathVariable Long postId) {
        return CommonResponse.toResponseEntity(SuccessCode.OK, postService.bookmarkPost(postId));
    }

    @DeleteMapping("/{postId}/bookmarks")
    public ResponseEntity<CommonResponse<BookmarkResponse>> removeBookmark(@PathVariable Long postId) {
        return CommonResponse.toResponseEntity(SuccessCode.OK, postService.removeBookmark(postId));
    }

    @GetMapping("/bookmarks/me")
    public ResponseEntity<CommonResponse<Slice<PostResponse>>> getMyBookmarks(@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return CommonResponse.toResponseEntity(SuccessCode.POST_LIST_SUCCESS, postQueryService.getMyBookmarks(size));
    }

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResponse<PostResponse>> getPostById(@PathVariable Long postId) {
        PostResponse post = postQueryService.getPost(postId);
        return CommonResponse.toResponseEntity(SuccessCode.POST_DETAIL_SUCCESS, post);
    }

    @Override
    @DeleteMapping("/{postId}")
    public ResponseEntity<CommonResponse<Void>> deletePostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.deletePost(postId, userDetails);
        return CommonResponse.toResponseEntity(SuccessCode.POST_DELETE_SUCCESS);
    }

    @Override
    @PutMapping("/{postId}")
    public ResponseEntity<CommonResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PostResponse post = postService.updatePost(postId, dto, userDetails);
        return CommonResponse.toResponseEntity(SuccessCode.POST_UPDATE_SUCCESS, post);
    }
}
