package com.kindtail.adoptmate.comment.controller;

import com.kindtail.adoptmate.comment.dto.CommentCreateRequest;
import com.kindtail.adoptmate.comment.dto.CommentResponse;
import com.kindtail.adoptmate.comment.dto.CommentUpdateRequest;
import com.kindtail.adoptmate.comment.service.CommentService;
import com.kindtail.adoptmate.common.dto.CommonResponse;
import com.kindtail.adoptmate.common.dto.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController implements CommentControllerDocs {

    private final CommentService commentService;

    @Override
    @PostMapping("/{postId}")
    public ResponseEntity<CommonResponse<CommentResponse>> createComment(@PathVariable Long postId, @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse savedComment = commentService.createComment(postId, request);
        return CommonResponse.toResponseEntity(SuccessCode.COMMENT_CREATE_SUCCESS, savedComment);
    }

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<CommentResponse> comments = commentService.getComments(postId, pageable);
        return CommonResponse.toResponseEntity(SuccessCode.COMMENT_LIST_SUCCESS, comments);
    }

    @Override
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommonResponse<Void>> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return CommonResponse.toResponseEntity(SuccessCode.COMMENT_DELETE_SUCCESS);
    }

    @Override
    @PutMapping(value = {"/{commentId}", "/update/{commentId}"})
    public ResponseEntity<CommonResponse<CommentResponse>> updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentUpdateRequest dto) {
        CommentResponse comment = commentService.updateComment(commentId, dto);
        return CommonResponse.toResponseEntity(SuccessCode.COMMENT_UPDATE_SUCCESS, comment);
    }
}
