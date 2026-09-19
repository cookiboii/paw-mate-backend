package com.kindtail.adoptmate.post.dto;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.post.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "게시글 응답")
public record PostResponse(
        @Schema(description = "게시글 ID", example = "1") Long id,
        @Schema(description = "게시글 제목", example = "입양 후 한 달 후기") String title,
        @Schema(description = "게시글 본문", example = "새 가족과 건강하게 지내고 있어요.") String content,
        @Schema(description = "작성자 표시 이름. 탈퇴한 회원이면 '탈퇴한 사용자'로 표시됩니다.", example = "홍길동") String name,
        @Schema(description = "게시글 작성 시각", example = "2026-09-20T14:30:00") LocalDateTime createdAt,
        @Schema(description = "게시글 이미지 URL 또는 이미지 데이터", nullable = true,
                example = "https://example.com/images/post-1.jpg") String img,
        @Schema(description = "좋아요 수", example = "12") long likeCount,
        @Schema(description = "댓글 수", example = "3") long commentCount,
        @Schema(description = "현재 로그인 사용자의 좋아요 여부. 비로그인 사용자는 false입니다.", example = "true") boolean likedByMe,
        @Schema(description = "현재 로그인 사용자의 북마크 여부. 비로그인 사용자는 false입니다.", example = "false") boolean bookmarkedByMe
) {
    public PostResponse(Long id, String title, String content, String name, LocalDateTime createdAt, String img) {
        this(id, title, content, name, createdAt, img, 0L, 0L, false, false);
    }

    /** Compatibility constructor: the email argument is intentionally discarded. */
    public PostResponse(Long id, String title, String content, String ignoredEmail, String name, LocalDateTime createdAt, String img) {
        this(id, title, content, name, createdAt, img, 0L, 0L, false, false);
    }

    public static PostResponse from(Post post) {
        Member member = post.getMember();
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(),
                member == null ? null : member.getEmail(), member == null ? "탈퇴한 사용자" : member.getName(),
                post.getCreatedAt(), post.getImage());
    }

    public static PostResponse from(Post post, long likeCount, long commentCount, boolean likedByMe, boolean bookmarkedByMe) {
        PostResponse basic = from(post);
        return new PostResponse(basic.id(), basic.title(), basic.content(), basic.name(), basic.createdAt(),
                basic.img(), likeCount, commentCount, likedByMe, bookmarkedByMe);
    }
}
