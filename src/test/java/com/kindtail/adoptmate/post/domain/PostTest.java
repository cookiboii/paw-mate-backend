package com.kindtail.adoptmate.post.domain;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("Post 엔티티 빌더로 생성된다")
    void 게시글_엔티티_빌더로_생성 () {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .role(Role.USER)
                .build();

        // when
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .member(member)
                .build();

        // then
        assertThat(post.getTitle()).isEqualTo("테스트 제목");
        assertThat(post.getContent()).isEqualTo("테스트 내용");
        assertThat(post.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("update 메서드로 게시글을 수정할 수 있다")
    void update_로_게시글_수정 () {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .role(Role.USER)
                .build();

        Post post = Post.builder()
                .title("원래 제목")
                .content("원래 내용")
                .member(member)
                .build();

        // when
        post.update("수정된 제목", "수정된 내용");

        // then
        assertThat(post.getTitle()).isEqualTo("수정된 제목");
        assertThat(post.getContent()).isEqualTo("수정된 내용");
    }
}
