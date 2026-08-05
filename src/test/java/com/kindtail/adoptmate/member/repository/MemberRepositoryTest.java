package com.kindtail.adoptmate.member.repository;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@example.com")
                .name("테스트 사용자")
                .password("password123")
                .role(Role.USER)
                .build();
        memberRepository.save(testMember);
    }

    @Test
    @DisplayName("이메일로 회원을 조회할 수 있다")
    void findByEmail_성공 () {
        // when
        Optional<Member> foundMember = memberRepository.findByEmail("test@example.com");

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName()).isEqualTo("테스트 사용자");
        assertThat(foundMember.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional 을 반환한다")
    void findByEmail_없음 () {
        // when
        Optional<Member> foundMember = memberRepository.findByEmail("notexist@example.com");

        // then
        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("socialProvider 와 socialId 로 회원을 조회할 수 있다")
    void findBySocialProviderAndSocialId_성공 () {
        // given
        Member socialMember = Member.builder()
                .email("social@example.com")
                .name("소셜 사용자")
                .socialId("1234567890")
                .socialProvider("GOOGLE")
                .role(Role.USER)
                .build();
        memberRepository.save(socialMember);

        // when
        Optional<Member> foundMember = memberRepository.findBySocialProviderAndSocialId("1234567890", "GOOGLE");

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName()).isEqualTo("소셜 사용자");
        assertThat(foundMember.get().getSocialId()).isEqualTo("1234567890");
        assertThat(foundMember.get().getSocialProvider()).isEqualTo("GOOGLE");
    }

    @Test
    @DisplayName("일치하는 소셜 정보가 없으면 빈 Optional 을 반환한다")
    void findBySocialProviderAndSocialId_없음 () {
        // when
        Optional<Member> foundMember = memberRepository.findBySocialProviderAndSocialId("invalid", "GOOGLE");

        // then
        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("회원을 저장할 수 있다")
    void save_성공 () {
        // given
        Member newMember = Member.builder()
                .email("new@example.com")
                .name("새 사용자")
                .password("newPassword")
                .role(Role.ADMIN)
                .build();

        // when
        Member savedMember = memberRepository.save(newMember);

        // then
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getEmail()).isEqualTo("new@example.com");
        assertThat(savedMember.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("회원을 삭제할 수 있다")
    void delete_성공 () {
        // given
        Member memberToDelete = Member.builder()
                .email("delete@example.com")
                .name("삭제될 사용자")
                .password("password")
                .role(Role.USER)
                .build();
        memberRepository.save(memberToDelete);

        // when
        memberRepository.delete(memberToDelete);

        // then
        Optional<Member> foundMember = memberRepository.findByEmail("delete@example.com");
        assertThat(foundMember).isEmpty();
    }
}
