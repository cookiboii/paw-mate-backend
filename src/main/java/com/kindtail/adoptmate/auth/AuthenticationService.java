package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.MemberLoginRequest;
import com.kindtail.adoptmate.member.dto.MemberLoginResponse;
import com.kindtail.adoptmate.member.dto.TokenRefreshResponse;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Authentication use cases: login, refresh-token rotation, and logout. */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenSessionService tokenSessionService;

    public MemberLoginResponse login(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        String accessToken = issueAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());
        tokenSessionService.saveRefreshToken(member.getEmail(), refreshToken, jwtTokenProvider.getExpirationRt());
        return new MemberLoginResponse(accessToken, refreshToken, member.getEmail(), member.getRole());
    }

    public TokenRefreshResponse refresh(String refreshToken) {
        String email;
        try { email = jwtTokenProvider.validateRefreshToken(refreshToken); }
        catch (Exception e) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        if (!tokenSessionService.matchesRefreshToken(email, refreshToken)) throw new CustomException(ErrorCode.UNAUTHORIZED);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        String nextRefresh = jwtTokenProvider.createRefreshToken(email);
        tokenSessionService.saveRefreshToken(email, nextRefresh, jwtTokenProvider.getExpirationRt());
        return new TokenRefreshResponse(issueAccessToken(member), nextRefresh);
    }

    public void logout(String accessToken) {
        try { tokenSessionService.removeRefreshToken(jwtTokenProvider.getEmailFromToken(accessToken)); } catch (Exception ignored) { }
        tokenSessionService.blacklist(accessToken, jwtTokenProvider.getRemainingExpirationMillis(accessToken));
    }

    public void saveRefreshToken(String email, String refreshToken) {
        tokenSessionService.saveRefreshToken(email, refreshToken, jwtTokenProvider.getExpirationRt());
    }

    private String issueAccessToken(Member member) {
        return jwtTokenProvider.createToken(member.getId(), member.getEmail(), member.getRole().toString(), tokenSessionService.tokenVersion(member.getEmail()));
    }
}
