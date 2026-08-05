package com.kindtail.adoptmate.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    @DisplayName("Member 엔티티 빌더로 생성된다")
    void 멤버_엔티티_빌더로_생성 () {
        // given & when
        Member member = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .password("password123")
                .role(Role.USER)
                .build();

        // then
        assertThat(member.getEmail()).isEqualTo("test@example.com");
        assertThat(member.getName()).isEqualTo("테스트 사용자");
        assertThat(member.getPassword()).isEqualTo("password123");
        assertThat(member.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("Member 의 기본 역할은 USER 이다")
    void 멤버_엔티티_기본_역할은_USER () {
        // given & when
        Member member = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .build();

        // then
        assertThat(member.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("updatePassword 로 비밀번호를 변경할 수 있다")
    void updatePassword_로_비밀번호를_변경할_수_있다 () {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .password("oldPassword")
                .build();

        String newPassword = "newPassword123";

        // when
        member.updatePassword(newPassword);

        // then
        assertThat(member.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("toDto 로 MemberResponseDto 를 생성할 수 있다")
    void toDto_로_DTO_생성 () {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .profileImage("http://example.com/profile.jpg")
                .socialProvider("GOOGLE")
                .role(Role.ADMIN)
                .build();

        // when
        var dto = member.toDto();

        // then
        assertThat(dto.email()).isEqualTo("test@example.com");
        assertThat(dto.name()).isEqualTo("테스트 사용자");
        assertThat(dto.profileImage()).isEqualTo("http://example.com/profile.jpg");
        assertThat(dto.socialProvider()).isEqualTo("GOOGLE");
        assertThat(dto.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("생성자로 Member 를 생성할 수 있다")
    void 생성자로_멤버_생성 () {
        // given & when
        Member member = new Member("test@example.com", "password123", "테스트 사용자", Role.USER);

        // then
        assertThat(member.getEmail()).isEqualTo("test@example.com");
        assertThat(member.getPassword()).isEqualTo("password123");
        assertThat(member.getName()).isEqualTo("테스트 사용자");
        assertThat(member.getRole()).isEqualTo(Role.USER);
    }
}
