package com.kindtail.adoptmate.member.service;

import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.dto.*;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .password("encodedPassword123")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("회원을 등록할 수 있다")
    void registerMember_성공 () {
        // given
        MemberRegisterRequestDto request = new MemberRegisterRequestDto(
                "테스트 사용자",
                "test@example.com",
                "password123",
                Role.USER
        );

        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword123");
        given(memberRepository.save(any(Member.class))).willReturn(testMember);

        // when
        Member result = memberService.registerMember(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 등록하면 예외가 발생한다")
    void registerMember_이메일_중복_예외 () {
        // given
        MemberRegisterRequestDto request = new MemberRegisterRequestDto(
                "테스트 사용자",
                "test@example.com",
                "password123",
                Role.USER
        );

        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(testMember));

        // when & then
        assertThatThrownBy(() -> memberService.registerMember(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("로그인 시 토큰을 발급받을 수 있다")
    void login_성공 () {
        // given
        MemberLoginRequestDto loginRequest = new MemberLoginRequestDto("test@example.com", "password123");
        String accessToken = "accessToken123";
        String refreshToken = "refreshToken123";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(testMember));
        given(passwordEncoder.matches("password123", "encodedPassword123")).willReturn(true);
        given(jwtTokenProvider.createToken("test@example.com", "USER")).willReturn(accessToken);
        given(jwtTokenProvider.createRefreshToken("test@example.com")).willReturn(refreshToken);

        // when
        MemberLoginResultDto result = memberService.login(loginRequest);

        // then
        assertThat(result.token()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);
        verify(redisTemplate.opsForValue()).set(eq("refreshToken:test@example.com"), eq(refreshToken), any(Duration.class));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 예외가 발생한다")
    void login_비밀번호_불일치_예외 () {
        // given
        MemberLoginRequestDto loginRequest = new MemberLoginRequestDto("test@example.com", "wrongPassword");

        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(testMember));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword123")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.login(loginRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인하면 예외가 발생한다")
    void login_사용자_없음_예외 () {
        // given
        MemberLoginRequestDto loginRequest = new MemberLoginRequestDto("notexist@example.com", "password123");

        given(memberRepository.findByEmail("notexist@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.login(loginRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("모든 회원을 조회할 수 있다")
    void getMembers_성공 () {
        // given
        List<Member> members = List.of(testMember);
        given(memberRepository.findAll()).willReturn(members);

        // when
        List<Member> result = memberService.getMembers();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("회원을 삭제할 수 있다")
    void deleteUser_성공 () {
        // given
        String email = "test@example.com";
        given(memberRepository.findByEmail(email)).willReturn(Optional.of(testMember));
        doNothing().when(memberRepository).delete(testMember);

        // when
        memberService.deleteUser(email);

        // then
        verify(memberRepository).delete(testMember);
    }

    @Test
    @DisplayName("비밀번호를 변경할 수 있다")
    void changePassword_성공 () {
        // given
        String email = "test@example.com";
        PasswordChangeRequestDto request = new PasswordChangeRequestDto("password123", "newPassword456");

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(testMember));
        given(passwordEncoder.matches("password123", "encodedPassword123")).willReturn(true);
        given(passwordEncoder.encode("newPassword456")).willReturn("encodedNewPassword456");

        // when
        memberService.changePassword(email, request);

        // then
        assertThat(testMember.getPassword()).isEqualTo("encodedNewPassword456");
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 비밀번호 변경에 실패한다")
    void changePassword_현재_비밀번호_불일치_예외 () {
        // given
        String email = "test@example.com";
        PasswordChangeRequestDto request = new PasswordChangeRequestDto("wrongPassword", "newPassword456");

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(testMember));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword123")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.changePassword(email, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);
    }

    @Test
    @DisplayName("이메일로 회원 ID 를 조회할 수 있다")
    void getMemberIdByEmail_성공 () {
        // given
        String email = "test@example.com";
        given(memberRepository.findByEmail(email)).willReturn(Optional.of(testMember));

        // when
        Long result = memberService.getMemberIdByEmail(email);

        // then
        assertThat(result).isEqualTo(1L);
    }
}
