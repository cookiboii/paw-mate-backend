package com.kindtail.adoptmate.post.dto;

import com.kindtail.adoptmate.post.domain.Post;

import java.time.LocalDateTime;

public record PostResponseDto(
        Long id,
        String title,
        String content ,
        String name,
        LocalDateTime createAt,
        String img


) {
    public static PostResponseDto from(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getEmail(),
                post.getCreatedAt(),
                post.getImage()



        );
    }

}
