package com.kindtail.adoptmate.comment.controller;

import com.kindtail.adoptmate.comment.dto.CommentDto;
import com.kindtail.adoptmate.comment.dto.CommentResponseDto;
import com.kindtail.adoptmate.comment.dto.CommentUpdateDto;
import com.kindtail.adoptmate.comment.service.CommentService;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.common.dto.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController implements CommentControllerDocs {

    private final CommentService commentService;

    @Override
    @PostMapping("/{postId}")
    public ResponseEntity<CommonResDto<CommentResponseDto>> addComment(@PathVariable Long postId, @Valid @RequestBody CommentDto commentDto) {
        CommentResponseDto savedComment = commentService.addComment(postId, commentDto);
        return CommonResDto.toResponseEntity(SuccessCode.COMMENT_CREATE_SUCCESS, savedComment);
    }

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResDto<List<CommentResponseDto>>> getComments(@PathVariable Long postId) {
        List<CommentResponseDto> comments = commentService.getComments(postId);
        return CommonResDto.toResponseEntity(SuccessCode.COMMENT_LIST_SUCCESS, comments);
    }

    @Override
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommonResDto<Void>> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return CommonResDto.toResponseEntity(SuccessCode.COMMENT_DELETE_SUCCESS);
    }

    @Override
    @PutMapping(value = {"/{commentId}", "/update/{commentId}"})
    public ResponseEntity<CommonResDto<CommentResponseDto>> updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentUpdateDto dto) {
        CommentResponseDto comment = commentService.updateComment(commentId, dto);
        return CommonResDto.toResponseEntity(SuccessCode.COMMENT_UPDATE_SUCCESS, comment);
    }
}
