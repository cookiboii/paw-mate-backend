package com.kindtail.adoptmate.comment.domain;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.post.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    @Test
    @DisplayName("Comment 엔티티가 빌더 패턴으로 올바르게 생성된다")
    void createCommentWithBuilder() {
        // given
        Member member = Member.builder()
                .email("commenter@example.com")
                .name("댓글작성자")
                .role(Role.USER)
                .build();

        Post post = Post.builder()
                .title("게시글 제목")
                .content("게시글 내용")
                .member(member)
                .build();

        // when
        Comment comment = Comment.builder()
                .id(1L)
                .content("댓글 내용입니다.")
                .member(member)
                .post(post)
                .build();

        // then
        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getContent()).isEqualTo("댓글 내용입니다.");
        assertThat(comment.getMember()).isEqualTo(member);
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getParent()).isNull();
    }

    @Test
    @DisplayName("부모 댓글(Parent)을 지정하여 대댓글 계층 구조를 생성할 수 있다")
    void createChildComment() {
        // given
        Member member = Member.builder().email("test@example.com").build();
        Post post = Post.builder().title("제목").build();

        Comment parentComment = Comment.builder()
                .id(1L)
                .content("부모 댓글")
                .member(member)
                .post(post)
                .build();

        // when
        Comment childComment = Comment.builder()
                .id(2L)
                .content("대댓글입니다.")
                .parent(parentComment)
                .member(member)
                .post(post)
                .build();

        // then
        assertThat(childComment.getParent()).isEqualTo(parentComment);
        assertThat(childComment.getParent().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("updateComment 메서드로 댓글 내용을 수정할 수 있다")
    void updateComment() {
        // given
        Comment comment = Comment.builder()
                .id(1L)
                .content("원래 댓글 내용")
                .build();

        // when
        comment.updateComment("수정된 댓글 내용");

        // then
        assertThat(comment.getContent()).isEqualTo("수정된 댓글 내용");
    }
}
