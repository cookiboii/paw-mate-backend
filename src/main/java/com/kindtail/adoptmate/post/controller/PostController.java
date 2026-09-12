package com.kindtail.adoptmate.post.controller;

import com.kindtail.adoptmate.common.dto.CommonResponse;
import com.kindtail.adoptmate.common.dto.SuccessCode;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequest;
import com.kindtail.adoptmate.post.dto.PostResponse;
import com.kindtail.adoptmate.post.dto.PostUpdateRequest;
import com.kindtail.adoptmate.post.dto.LikeResponse;
import com.kindtail.adoptmate.post.dto.BookmarkResponse;
import com.kindtail.adoptmate.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping({"/api/v1/posts", "/post"})
@RequiredArgsConstructor
@Validated
public class PostController implements PostControllerDocs {

    private final PostService postService;

    @Override
    @PostMapping({"", "/create"})
    public ResponseEntity<CommonResponse<PostResponse>> createPost(@Valid @RequestBody PostCreateRequest dto) {
        PostResponse response = postService.createPost(dto);
        return CommonResponse.toResponseEntity(SuccessCode.POST_CREATE_SUCCESS, response);
    }

    @Override
    @GetMapping({"", "/list"})
    public ResponseEntity<CommonResponse<Page<PostResponse>>> getPostList(Pageable pageable) {
        Page<PostResponse> postPage = postService.getAllPosts(pageable);
        return CommonResponse.toResponseEntity(SuccessCode.POST_LIST_SUCCESS, postPage);
    }

    @Override
    @GetMapping("/cursor")
    public ResponseEntity<CommonResponse<Slice<PostResponse>>> getPostsByCursor(
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort
    ) {
        // Keep the legacy cursor call working for older clients that send none of
        // the new search parameters; all new cursor options use the richer query.
        Slice<PostResponse> postSlice = category == null && keyword == null && (sort == null || "latest".equalsIgnoreCase(sort))
                ? postService.getPostsByCursor(lastPostId, size)
                : postService.searchPosts(lastPostId, size, category, keyword, sort);
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
    public ResponseEntity<CommonResponse<Slice<PostResponse>>> getMyBookmarks(@RequestParam(defaultValue = "20") int size) {
        return CommonResponse.toResponseEntity(SuccessCode.POST_LIST_SUCCESS, postService.getMyBookmarks(size));
    }

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResponse<PostResponse>> getPostById(@PathVariable Long postId) {
        PostResponse post = postService.getPost(postId);
        return CommonResponse.toResponseEntity(SuccessCode.POST_DETAIL_SUCCESS, post);
    }

    @Override
    @DeleteMapping("/{postId}")
    public ResponseEntity<CommonResponse<Void>> deletePostById(@PathVariable Long postId) {
        postService.deletePost(postId);
        return CommonResponse.toResponseEntity(SuccessCode.POST_DELETE_SUCCESS);
    }

    @Override
    @PutMapping("/{postId}")
    public ResponseEntity<CommonResponse<PostResponse>> updatePost(@PathVariable Long postId, @Valid @RequestBody PostUpdateRequest dto) {
        PostResponse post = postService.updatePost(postId, dto);
        return CommonResponse.toResponseEntity(SuccessCode.POST_UPDATE_SUCCESS, post);
    }
}
