package com.kindtail.adoptmate.post.domain;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("validateAuthor: 작성자 본인은 검증을 통과한다")
    void validateAuthor_작성자_성공() {
        Member member = Member.builder().id(1L).email("author@example.com").role(Role.USER).build();
        Post post = Post.builder().id(10L).member(member).build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        assertThatCode(() -> post.validateAuthor(userDetails)).doesNotThrowAnyException();
        assertThatCode(() -> post.validateAuthor(1L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateAuthor: 관리자(ADMIN)라도 작성자가 아니면 예외가 발생한다 (수정은 본인만 가능)")
    void validateAuthor_관리자_예외() {
        Member author = Member.builder().id(1L).email("author@example.com").role(Role.USER).build();
        Post post = Post.builder().id(10L).member(author).build();

        Member admin = Member.builder().id(99L).email("admin@example.com").role(Role.ADMIN).build();
        CustomUserDetails adminDetails = new CustomUserDetails(admin);

        assertThatThrownBy(() -> post.validateAuthor(adminDetails))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_AUTHOR_REQUIRED);
        assertThatThrownBy(() -> post.validateAuthor(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_AUTHOR_REQUIRED);
    }

    @Test
    @DisplayName("validateAuthorOrAdmin: 작성자 본인 및 관리자 모두 검증을 통과한다 (삭제는 관리자도 가능)")
    void validateAuthorOrAdmin_성공() {
        Member author = Member.builder().id(1L).email("author@example.com").role(Role.USER).build();
        Post post = Post.builder().id(10L).member(author).build();

        Member admin = Member.builder().id(99L).email("admin@example.com").role(Role.ADMIN).build();
        CustomUserDetails authorDetails = new CustomUserDetails(author);
        CustomUserDetails adminDetails = new CustomUserDetails(admin);

        assertThatCode(() -> post.validateAuthorOrAdmin(authorDetails)).doesNotThrowAnyException();
        assertThatCode(() -> post.validateAuthorOrAdmin(adminDetails)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateAuthorOrAdmin: 작성자도 아니고 관리자도 아니면 예외가 발생한다")
    void validateAuthorOrAdmin_권한없음_예외() {
        Member author = Member.builder().id(1L).email("author@example.com").role(Role.USER).build();
        Post post = Post.builder().id(10L).member(author).build();

        Member other = Member.builder().id(2L).email("other@example.com").role(Role.USER).build();
        CustomUserDetails otherDetails = new CustomUserDetails(other);

        assertThatThrownBy(() -> post.validateAuthorOrAdmin(otherDetails))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_AUTHOR);
    }
}
