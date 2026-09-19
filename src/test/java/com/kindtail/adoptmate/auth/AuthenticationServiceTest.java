package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.dto.MemberLoginRequest;
import com.kindtail.adoptmate.member.dto.MemberLoginResponse;
import com.kindtail.adoptmate.member.dto.TokenRefreshResponse;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private TokenSessionService tokenSessionService;
    @InjectMocks private AuthenticationService authenticationService;

    @Test
    void loginIssuesAndStoresBothTokens() {
        Member member = member();
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(passwordEncoder.matches("password", member.getPassword())).willReturn(true);
        given(tokenSessionService.tokenVersion(member.getEmail())).willReturn(2L);
        given(jwtTokenProvider.createToken(1L, member.getEmail(), "USER", 2L)).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(member.getEmail())).willReturn("refresh-token");
        given(jwtTokenProvider.getExpirationRt()).willReturn(3600);

        MemberLoginResponse response = authenticationService.login(new MemberLoginRequest(member.getEmail(), "password"));

        assertThat(response.token()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(tokenSessionService).saveRefreshToken(member.getEmail(), "refresh-token", 3600);
    }

    @Test
    void refreshRotatesStoredRefreshToken() throws Exception {
        Member member = member();
        given(jwtTokenProvider.validateRefreshToken("old-refresh")).willReturn(member.getEmail());
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(tokenSessionService.tokenVersion(member.getEmail())).willReturn(0L);
        given(jwtTokenProvider.createToken(1L, member.getEmail(), "USER", 0L)).willReturn("new-access");
        given(jwtTokenProvider.createRefreshToken(member.getEmail())).willReturn("new-refresh");
        given(jwtTokenProvider.getExpirationRt()).willReturn(3600);
        given(tokenSessionService.rotateRefreshToken(member.getEmail(), "old-refresh", "new-refresh", 3600))
                .willReturn(true);

        TokenRefreshResponse response = authenticationService.refresh("old-refresh");

        assertThat(response.token()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        verify(tokenSessionService).rotateRefreshToken(member.getEmail(), "old-refresh", "new-refresh", 3600);
    }

    @Test
    void loginRejectsInvalidPassword() {
        Member member = member();
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrong", member.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authenticationService.login(new MemberLoginRequest(member.getEmail(), "wrong")))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void loginUsesTheSameErrorForAnUnknownEmail() {
        given(memberRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(new MemberLoginRequest("unknown@example.com", "password")))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);
    }

    private Member member() {
        return Member.builder().id(1L).email("member@example.com").password("encoded").role(Role.USER).build();
    }
}
