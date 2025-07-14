package com.kindtail.adoptmate.comment.dto;

import com.kindtail.adoptmate.comment.domain.Comment;

public record CommentDto(Long parentId  ,String content ) {

}
