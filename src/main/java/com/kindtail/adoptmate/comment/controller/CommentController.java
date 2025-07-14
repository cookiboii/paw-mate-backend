package com.kindtail.adoptmate.comment.controller;

import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.comment.dto.CommentDto;
import com.kindtail.adoptmate.comment.dto.CommentResponseDto;
import com.kindtail.adoptmate.comment.dto.CommentUpdateDto;
import com.kindtail.adoptmate.comment.service.CommentService;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{postId}")
    public ResponseEntity<CommonResDto> addComment(@PathVariable Long postId, @RequestBody CommentDto commentDto) {
        CommentResponseDto savedComment = commentService.addComment(postId, commentDto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.ACCEPTED,"댓글등록성공",savedComment);
        return ResponseEntity.ok(commonResDto);
    }
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResDto>  getComments(@PathVariable Long postId) {
        List<CommentResponseDto> comments = commentService.getComments(postId);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.ACCEPTED,"보기성공",comments));
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }



    @PutMapping("/update/{commentId}")
    public ResponseEntity<CommonResDto> updateComment(@PathVariable Long commentId, @RequestBody CommentUpdateDto Dto) {
    CommentResponseDto comment    =commentService.updateComment(commentId, Dto);
    Dto.commentId();
     CommonResDto commonResDto = new CommonResDto(HttpStatus.ACCEPTED,"수정성공",comment);
    return ResponseEntity.ok(commonResDto);

    }


}
