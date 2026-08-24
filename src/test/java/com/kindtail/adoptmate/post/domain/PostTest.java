package com.kindtail.adoptmate.post.domain;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("Post 엔티티가 빌더 패턴으로 올바르게 생성된다")
    void createPostWithBuilder() {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .role(Role.USER)
                .build();
        // when
        Post post = Post.builder()
                .id(1L)
                .title("테스트 제목")
                .content("테스트 내용")
                .image("http://example.com/image.jpg")
                .member(member)
                .build();

        // then
        assertThat(post.getId()).isEqualTo(1L);
        assertThat(post.getTitle()).isEqualTo("테스트 제목");
        assertThat(post.getContent()).isEqualTo("테스트 내용");
        assertThat(post.getImage()).isEqualTo("http://example.com/image.jpg");
        assertThat(post.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("updatePost 메서드로 게시글의 제목, 내용, 이미지를 수정할 수 있다")
    void updatePost() {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .role(Role.USER)
                .build();

        Post post = Post.builder()
                .title("원래 제목")
                .content("원래 내용")
                .image("old_image.jpg")
                .member(member)
                .build();

        // when
        post.updatePost("수정된 제목", "수정된 내용", "new_image.jpg");

        // then
        assertThat(post.getTitle()).isEqualTo("수정된 제목");
        assertThat(post.getContent()).isEqualTo("수정된 내용");
        assertThat(post.getImage()).isEqualTo("new_image.jpg");
    }
}
