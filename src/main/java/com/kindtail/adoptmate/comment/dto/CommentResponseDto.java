package com.kindtail.adoptmate.comment.dto;

import com.kindtail.adoptmate.comment.domain.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponseDto(     Long id,
                                      String authorName,
                                      String authorEmail,
                                      String content,
                                      LocalDateTime createdAt,

                                      List<CommentResponseDto>children) {

    public static CommentResponseDto fromComment(Comment comment) {
            return new CommentResponseDto(
                    comment.getId(),
                    comment.getMember().getName(),
                    comment.getMember().getEmail(),
                    comment.getContent(),
                    comment.getCreationDate(),
                    comment.getChildren().stream()
                            .map(CommentResponseDto::fromComment)
                            .toList()






            );



          }
}
